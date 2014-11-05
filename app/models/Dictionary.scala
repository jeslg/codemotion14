package models

import play.api.cache.Cache
import play.api.Play.current
import play.api.Logger

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
