package controllers

import models._

import play.api._
import play.api.cache.Cache
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait DictionaryApp { this: Controller =>

  object Dictionary {

    type Dictionary = Map[String, String]

    private def dictionary = Cache.getOrElse[Dictionary]("dictionary")(Map())

    def contains(word: String) = dictionary isDefinedAt word 

    def get(word: String) = dictionary get word

    def set(entry: (String, String)) = {
      Logger.info(s"Adding word '${entry._1}' to dictionary")
      Cache.set("dictionary", dictionary + entry)
    }
  }

  object Users {

    type Users = Map[String, User]

    private def users = Cache.getOrElse[Users]("users")(Map())

    def get(nick: String) = users get nick

    def add(user: User) = {
      Logger.info(s"Adding '${user.nick}' to user list.")
      Cache.set("users", users + (user.nick -> user))
    }
  }
  
  /* Disable these lines for testing */

  // Users.add(User("Mr", "Proper", Option(WRITE)))
  // Users.add(User("Don", "Limpio", Option(READ)))
  // Users.add(User("Wipp", "Express"))

  val USER_HEADER_NAME = "user"

  /* This is what we want to avoid */

  // case class Logging[A](action: Action[A]) extends Action[A] {

  //   def apply(request: Request[A]): Future[Result] = {
  //     val user = request.headers.get(USER_HEADER_NAME)
  // 	.map(Users.get(_))
  // 	.flatten
  //     if (user.isDefined) {
  // 	Logger.info(s"@${user.get.nick} requests ${request.toString}")
  // 	action(request)
  //     } else {
  // 	Future(Forbidden(s"Invalid '$USER_HEADER_NAME' header"))
  //     }
  //   }

  //   lazy val parser = action.parser
  // }

  class UserRequest[A](
    val user: User, 
    request: Request[A]) extends WrappedRequest[A](request)

  object UserRefiner extends ActionRefiner[Request, UserRequest] {
    def refine[A](request: Request[A]) = Future {
      val user = request.headers.get(USER_HEADER_NAME)
	.map(Users.get(_))
        .flatten
      if (user.isDefined)
	Right(new UserRequest(user.get, request))
      else
	Left(Forbidden(s"Invalid '$USER_HEADER_NAME' header"))
    } 
  }

  object UserLogging extends ActionTransformer[UserRequest, UserRequest] {
    def transform[A](urequest: UserRequest[A]) = Future {
      Logger.info(s"@${urequest.user.nick} requests ${urequest.toString}")
      urequest
    }
  }

  class PermissionFilter(
      permitted: User => Boolean, 
      err: String) extends ActionFilter[UserRequest] {

    def filter[A](urequest: UserRequest[A]) = Future {
      if (permitted(urequest.user))
	None
      else
	Option(Forbidden(err))
    }
  }

  object ReadFilter extends PermissionFilter(
    Permission.canRead,
    "You are not allowed to read")

  object WriteFilter extends PermissionFilter(
    Permission.canWrite, 
    "You are not allowed to write")

  def wsToken: Future[String] = {
    val rel = controllers.routes.AlternativeDictionaryApp.wsToken.url
    val holder = WS.url(s"http://localhost:9000$rel")
    val response = holder.get
    response map (_.body)
  }

  def wsSearch(word: String, token: String): Future[Option[String]] = {
    val rel = controllers.routes.AlternativeDictionaryApp.wsSearch(word)
    val holder = WS.url(s"http://localhost:9000$rel").withBody(token)
    val response = holder.get
    response map { wsr =>
      wsr.status match {
	case OK => Option(wsr.body)
	case _ => None
      }
    }
  }

  def jsToWord(jsv: JsValue): (String, String) =
    (jsv \ "word").as[String] -> (jsv \ "definition").as[String]

  val jsToWordParser = parse.json map jsToWord

  def helloDictionary = 
    (Action andThen UserRefiner andThen UserLogging) {
      Ok("Welcome to the CodeMotion14 Dictionary!")
    }

  def search(word: String) = 
    (Action andThen UserRefiner andThen ReadFilter andThen UserLogging) { urequest =>
      Dictionary.get(word).map(Ok(_)).getOrElse {
	NotFound(s"The word '$word' does not exist")
      }
    }

  def furtherSearch(word: String) =
    (Action andThen UserRefiner andThen ReadFilter andThen UserLogging).async { urequest =>
      Dictionary.get(word).map(d => Future(Ok(d))).getOrElse {
	(for {
	  token <- wsToken
	  odef  <- wsSearch(word, token)
	} yield odef).map(_ match { 
	  case Some(d) => Ok(d)
	  case _ => NotFound(s"The word '$word' does not exist")
	})
      }
    }

  def add = {
    (Action andThen UserRefiner andThen WriteFilter andThen UserLogging)(jsToWordParser) { urequest =>
      val entry@(word, _) = urequest.body
      Dictionary.set(entry)
      Ok(s"The word '$word' has been added successfully")
    }
  }

  def asJson: Enumeratee[String, JsValue] = Enumeratee.map(Json.parse)

  def asEntry = Enumeratee.map(jsToWord)

  def existingFilter = Enumeratee.filter[(String, String)] { case (word, _) => 
    ! (Dictionary.contains(word))
  }

  def toDictionary = Iteratee.foreach[(String, String)] { case (word, definition) =>
    Dictionary.set(word, definition)
  }

  def socketAdd = WebSocket.using[String] { request =>
    val in = asJson ><> asEntry ><> existingFilter &>> toDictionary
    val out = Enumerator("You're using the Dictionary WebSocket Service")
    (in, out)
  }
}

object DictionaryApp extends Controller with DictionaryApp
