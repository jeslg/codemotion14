package controllers

import play.api._
import play.api.cache.Cache
import play.api.mvc._
import play.api.Play.current

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

  def helloDictionary = Action {
    Ok("Welcome to the CodeMotion14 Dictionary!")
  }
}

object DictionaryApp extends Controller with DictionaryApp
