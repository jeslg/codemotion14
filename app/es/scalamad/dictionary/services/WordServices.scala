package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait WordServices extends DictionaryServices {

  val getEntry: String => Effect[Option[String]] = 
    GetEntry(_, Return(_))

  val setEntry: ((String, String)) => Effect[Unit] = 
    SetEntry(_, Return(()))

  val removeEntry: String => Effect[Unit] = 
    RemoveEntry(_, Return(()))

  val resetEntries: Map[String, String] => Effect[Unit] = 
    ResetEntries(_, Return(()))
}
