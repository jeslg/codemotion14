package controllers

import play.api._
import play.api.cache.Cache
import play.api.mvc._
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait DictionaryApp { this: Controller =>

  // TODO: Should we move this to "models"?
  type Entry = (String, String)         // (word, definition)
  type Dictionary = Map[String, String] // entry map

  /* Dictionary inner service.
   *
   * Deals with the inner state. It hides the user the cache interactions.
   */
  object Dictionary {

    private def dictionary = Cache.getOrElse[Dictionary]("dictionary")(Map())

    def contains(word: String) = dictionary isDefinedAt word 

    def get(word: String) = dictionary get word

    def set(entry: Entry) = Cache.set("dictionary", dictionary + entry)
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

  def search(word: String) = 
    Logging {
      Action {
	Dictionary.get(word).map(Ok(_)).getOrElse {
	  NotFound(s"could not find '$word'")
	}
      }
    }

  def add(word: String) =
    Logging {
      Action(parse.text) { request =>
	Dictionary.set(word -> request.body)
	Ok(s"The word '$word' has been added successfully")
      }
    }
}

object DictionaryApp extends Controller with DictionaryApp
