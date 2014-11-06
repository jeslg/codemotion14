package controllers

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import play.api.Play.current

import models._
import org.hablapps.codemotion14._

trait DictionaryApp { this: Controller =>

  val USER_HEADER_NAME = "user"
  val FURTHER_QUERY_NAME = "further"

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

  def wsTokenAndSearch(word: String): Future[Option[String]] = {
    for {
      token <- wsToken
      odef  <- wsSearch(word, token)
    } yield odef
  }

  def jsToWord(jsv: JsValue): (String, String) =
    (jsv \ "word").as[String] -> (jsv \ "definition").as[String]

  val jsToWordParser = parse.json map jsToWord

  def helloDictionary = 
    (Action andThen UserRefiner andThen UserLogging) {
      Ok("Welcome to the CodeMotion14 Dictionary!")
    }

  def isFurtherSearch[A](request: Request[A]): Boolean =
    request.queryString.contains(FURTHER_QUERY_NAME) &&
      (request.queryString(FURTHER_QUERY_NAME).size > 0) && 
      request.queryString(FURTHER_QUERY_NAME).head.toBoolean

  def search(word: String) =
    (Action 
     andThen UserRefiner 
     andThen ReadFilter 
     andThen UserLogging).async { urequest =>
      Dictionary.get(word).map(d => Future(Ok(d))).getOrElse {
	if (isFurtherSearch(urequest)) {
	  wsTokenAndSearch(word).map { odef =>
	    odef.map(Ok(_)).getOrElse(NotFound(s"The word '$word' does not exist"))
	  }
	} else {
	  Future(NotFound(s"The word '$word' does not exist"))
	}
      }
    }

  def add = {
    (Action 
     andThen UserRefiner 
     andThen WriteFilter 
     andThen UserLogging)(jsToWordParser) { urequest =>
       val entry@(word, _) = urequest.body
       Dictionary.set(entry)
       val url = controllers.routes.DictionaryApp.search(word).url
       Created(s"The word '$word' has been added successfully")
         .withHeaders((LOCATION -> url))
    }
  }

  def asJson: Enumeratee[String, JsValue] = Enumeratee.map(Json.parse)

  def asEntry = Enumeratee.map(jsToWord)

  def existingFilter = Enumeratee.filter[(String, String)] { case (word, _) => 
    ! (Dictionary.contains(word))
  }

  def toDictionary = {
    Iteratee.foreach[(String, String)] { case (word, definition) =>
      Dictionary.set(word, definition)
    }
  }

  /*
   * This can be tested by using: http://websocket.org/echo.html
   */
  def socketAdd = WebSocket.using[String] { request =>
    val in = asJson ><> asEntry ><> existingFilter &>> toDictionary
    val out = Enumerator("You're using the Dictionary WebSocket Service")
    (in, out)
  }
}

object DictionaryApp extends Controller with DictionaryApp
