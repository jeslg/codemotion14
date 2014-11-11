// package es.scalamad.dictionary.models

// import play.api.cache.Cache
// import play.api.Play.current
// import play.api.Logger

// trait WordRepository {

//   def contains(word: String): Boolean

//   def get(word: String): Option[String]

//   def set(entry: (String, String)): Unit

//   def reset(entries: (String, String)*): Unit
// }

// class CacheWordRepository extends WordRepository {

//   type Words = Map[String, String]

//   private def words = Cache.getOrElse[Words]("words")(Map())

//   def contains(word: String) = words isDefinedAt word 

//   def get(word: String) = words get word

//   def set(entry: (String, String)) = {
//     Logger.info(s"Adding word '${entry._1}' to dictionary")
//     Cache.set("words", words + entry)
//   }

//   def reset(entries: (String, String)*) = 
//     Cache.set("words", entries.toMap)
// }
