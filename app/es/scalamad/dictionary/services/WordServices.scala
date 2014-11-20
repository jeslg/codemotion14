package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait WordServices extends DictionaryServices {

  val getEntry: String => Repo[Option[String]] = 
    GetEntry(_, Return(_))

  val setEntry: ((String, String)) => Repo[Unit] = 
    SetEntry(_, Return(()))

  val removeEntry: String => Repo[Unit] = 
    RemoveEntry(_, Return(()))

  val resetEntries: Map[String, String] => Repo[Unit] = 
    ResetEntries(_, Return(()))
}
