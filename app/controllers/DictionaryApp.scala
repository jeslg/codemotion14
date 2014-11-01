package controllers

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

  type Dictionary = Map[String, String] // entry map

  object Dictionary {

    private def dictionary = Cache.getOrElse[Dictionary]("dictionary")(Map())

    def contains(word: String) = dictionary isDefinedAt word 

    def get(word: String) = dictionary get word

    def set(entry: (String, String)) = {
      Logger.info(s"Adding word '${entry._1}' to dictionary")
      Cache.set("dictionary", dictionary + entry)
    }
  }

  case class Logging[A](action: Action[A]) extends Action[A] {

    def apply(request: Request[A]): Future[Result] = {
      Logger.info(request.toString)
      action(request)
    }

    lazy val parser = action.parser
  }

  def helloDictionary = 
    Logging {
      Action { request =>
	Ok("Welcome to the CodeMotion14 Dictionary!")
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

  def wsToken = Future("1234567890")

  def wsSearch(word: String, token: String) = Future(Option("word definition here"))

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

  def wsAdd = WebSocket.using[String] { request =>
    val in = asJson ><> asEntry ><> existingFilter ><> Enumeratee.take(3) &>> toDictionary
    val out = Enumerator("You're using the Dictionary WebSocket")
    (in, out)
  }
}

object DictionaryApp extends Controller with DictionaryApp
