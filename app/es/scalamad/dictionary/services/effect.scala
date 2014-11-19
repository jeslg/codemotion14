package es.scalamad.dictionary.services

sealed trait Effect[A] {

  def map[B](f: A => B): Effect[B] = MapM(this, f)

  def flatMap[B](f: A => Effect[B]): Effect[B] = FlatMap(this, f)
}

case class GetEntry(word: String) extends Effect[Option[String]]

case class SetEntry(entry: (String, String)) extends Effect[Unit]

case class RemoveEntry(word: String) extends Effect[Unit]

case class ResetEntries(state: Map[String, String]) extends Effect[Unit]

case class FlatMap[A, B](ra: Effect[A], f: A => Effect[B]) extends Effect[B]

case class MapM[A, B](ra: Effect[A], f: A => B) extends Effect[B]

object Effect {

  def getEntry(word: String): Effect[Option[String]] = GetEntry(word)

  def setEntry(entry: (String, String)): Effect[Unit] = SetEntry(entry)

  def removeEntry(word: String): Effect[Unit] = RemoveEntry(word)

  def resetEntries(state: Map[String, String]): Effect[Unit] = 
    ResetEntries(state)

  def modifyEntry(word: String, f: Option[String] => String): Effect[Unit] = 
    for {
      d <- getEntry(word)
      _ <- setEntry(word, f(d))
    } yield ()
}
