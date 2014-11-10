package models.org.hablapps.codemotion14

import play.api.cache.Cache
import play.api.Play.current
import play.api.Logger

trait DictionaryService {

  val dictionaryRepository: DictionaryRepository

  def containsEntry(word: String) = 
    dictionaryRepository.contains(word)

  def getEntry(word: String) = 
    dictionaryRepository.get(word)

  def setEntry(entry: (String, String)) = 
    dictionaryRepository.set(entry)

  def resetEntries(entries: (String, String)*) = 
    dictionaryRepository.reset(entries: _*)

}

trait CacheDictionaryService extends DictionaryService {
  val dictionaryRepository: DictionaryRepository = 
    new CacheDictionaryRepository
}

trait DictionaryRepository {

  def contains(word: String): Boolean

  def get(word: String): Option[String]

  def set(entry: (String, String)): Unit

  def reset(entries: (String, String)*): Unit
}

class CacheDictionaryRepository extends DictionaryRepository {

  type Dictionary = Map[String, String]

  private def dictionary = Cache.getOrElse[Dictionary]("dictionary")(Map())

  def contains(word: String) = dictionary isDefinedAt word 

  def get(word: String) = dictionary get word

  def set(entry: (String, String)) = {
    Logger.info(s"Adding word '${entry._1}' to dictionary")
    Cache.set("dictionary", dictionary + entry)
  }

  def reset(entries: (String, String)*) = 
    Cache.set("dictionary", entries.toMap)
}
