package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait WordServices {

  val getEntry: String => Repo[Option[String]] = 
    GetEntry(_, Return(_))

  val setEntry: ((String, String)) => Repo[Unit] = 
    SetEntry(_, Return(()))

  val removeEntry: String => Repo[Unit] = 
    RemoveEntry(_, Return(()))
}
