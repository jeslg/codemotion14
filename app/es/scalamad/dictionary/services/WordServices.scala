package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait WordServices {

  val getEntry: String => Repo[Option[String]] = 
    GetEntry(_, Return(_))

  val setEntry: ((String, String)) => Repo[Unit] = 
    SetEntry(_, Return(()))

  val removeEntry: String => Repo[Unit] = 
    RemoveEntry(_, Return(()))

  val removeEntry2: String => Repo[Option[String]] = { word =>
    for {
      os <- getEntry(word)
      _  <- removeEntry(word)
    } yield os
  }
}
