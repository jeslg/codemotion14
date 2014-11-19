package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait WordServices extends DictionaryServices {

  val getEntry: String => Effect[Option[String]] = GetEntry(_)

  val setEntry: ((String, String)) => Effect[Unit] = SetEntry(_)

  val removeEntry: String => Effect[Unit] = RemoveEntry(_)

  val resetEntries: Map[String, String] => Effect[Unit] = ResetEntries(_)

  val containsEntry: String => Effect[Boolean] = getEntry(_).map(_.isDefined)
}
