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

  Users.add(User("Mr", "Proper", 30))
  Users.add(User("Don", "Limpio", 15))

  val USER_HEADER_NAME = "user"

  case class Logging[A](action: Action[A]) extends Action[A] {

    def apply(request: Request[A]): Future[Result] = {
      val user = request.headers.get(USER_HEADER_NAME).map(Users.get(_))
      if (user.isDefined) {
	Logger.info(s"@$user requests ${request.toString}")
	action(request)
      } else {
	Future(Forbidden(s"Invalid '$USER_HEADER_NAME' header"))
      }
    }

    lazy val parser = action.parser
  }

  case class FilterInfant[A](action: Action[A]) extends Action[A] {
    def apply(request: Request[A]): Future[Result] = {
      val user = request.headers.get(USER_HEADER_NAME)
	.map(Users.get(_))
        .flatten
	.filter(_.age >= 18)
      if (user.isDefined) {
	action(request)
      } else {
	Future(Forbidden("You should be an adult to add a word"))
      }
    }

    lazy val parser = action.parser
  }

  def helloDictionary = 
    Logging {
      FilterInfant {
	Action { request =>
	  Ok("Welcome to the CodeMotion14 Dictionary!")
	}
      }
    }

  class WordRequest[A](
    val word: String, 
    val definition: Option[String], 
    request: Request[A]) extends WrappedRequest[A](request)

  def WordTransformer(word: String) = new ActionTransformer[Request, WordRequest] {
    def transform[A](request: Request[A]): Future[WordRequest[A]] = Future {
      new WordRequest(word, Dictionary.get(word), request)
    }
  }

  object NonExistingFilter extends ActionFilter[WordRequest] {
    def filter[A](wrequest: WordRequest[A]): Future[Option[Result]] = Future {
      wrequest.definition match {
	case Some(definition) => None
	case _ => Option(NotFound(s"The word '${wrequest.word}' does not exist"))
      }
    }
  }

  def search(word: String) = 
    Logging {
      (Action andThen WordTransformer(word) andThen NonExistingFilter) { wrequest =>
	Ok(wrequest.definition.get)
      }
    }

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

  def furtherSearch(word: String) =
    Logging {
      (Action andThen WordTransformer(word)).async { wrequest =>
	if (wrequest.definition.isDefined) {
	  Future(Ok(wrequest.definition.get))
	} else {
	  (for {
	    token <- wsToken
	    odef  <- wsSearch(word, token)
	  } yield odef) map (_ match { 
	    case Some(d) => Ok(d)
	    case _ => NotFound(s"The word '$word' does not exist")
	  })
	}
      }
    }

  object ExistingFilter extends ActionFilter[WordRequest] {
    def filter[A](wrequest: WordRequest[A]): Future[Option[Result]] = Future {
      wrequest.definition match {
	case Some(definition) => 
	  Option(Forbidden(s"The word '${wrequest.word}' does already exist"))
	case _ => None
      }
    }
  }

  def jsToWord(jsv: JsValue): (String, String) =
    (jsv \ "word").as[String] -> (jsv \ "definition").as[String]

  val jsToWordParser = parse.json map jsToWord

  val checkWordParser = parse.using { request =>
    jsToWordParser.validate { case wd@(word, _) =>
      if (request.path.tail == word)
	Right(wd)
      else
	Left(BadRequest(s"'${request.path.tail}' was not equal to '${word}'"))
    }
  }

  def add(word: String) =
    Logging {
      (Action 
       andThen WordTransformer(word)
       andThen ExistingFilter)(checkWordParser) { wrequest =>
	Dictionary.set(wrequest.word -> wrequest.body._2)
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
