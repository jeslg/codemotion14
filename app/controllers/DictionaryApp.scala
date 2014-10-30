package controllers

import play.api._
import play.api.cache.Cache
import play.api.mvc._
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait DictionaryApp { this: Controller =>

  // TODO: Should we move this to "models"?
  type Dictionary = Map[String, String] // entry map

  object Dictionary {

    private def dictionary = Cache.getOrElse[Dictionary]("dictionary")(Map())

    def contains(word: String) = dictionary isDefinedAt word 

    def get(word: String) = dictionary get word

    def set(entry: (String, String)) = Cache.set("dictionary", dictionary + entry)
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
      new WordRequest(word.toLowerCase, Dictionary.get(word.toLowerCase), request)
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
      (Action 
       andThen WordTransformer(word)
       andThen NonExistingFilter) { wrequest =>
	Ok(wrequest.definition.get)
      }
    }

  object ExistingFilter extends ActionFilter[WordRequest] {
    def filter[A](wrequest: WordRequest[A]): Future[Option[Result]] = Future {
      wrequest.definition match {
	case Some(definition) => Option(Forbidden(s"The word '${wrequest.word}' does already exist"))
	case _ => None
      }
    }
  }

  def add(word: String) =
    Logging {
      (Action andThen WordTransformer(word) andThen ExistingFilter)(parse.text) { wrequest =>
	Dictionary.set(wrequest.word -> wrequest.body)
	Ok(s"The word '$word' has been added successfully")
      }
    }
}

object DictionaryApp extends Controller with DictionaryApp
