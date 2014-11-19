package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

sealed trait Effect[A] {

  def map[B](f: A => B): Effect[B] = MapM(this, f)

  def flatMap[B](f: A => Effect[B]): Effect[B] = FlatMap(this, f)
}


/* Combinators */

case class FlatMap[A, B](ra: Effect[A], f: A => Effect[B]) extends Effect[B]

case class MapM[A, B](ra: Effect[A], f: A => B) extends Effect[B]


/* Atomic Effects */

// words

case class GetEntry(word: String) extends Effect[Option[String]]

case class SetEntry(entry: (String, String)) extends Effect[Unit]

case class RemoveEntry(word: String) extends Effect[Unit]

case class ResetEntries(state: Map[String, String]) extends Effect[Unit]

// users

case class GetUser(nick: String) extends Effect[Option[User]]

case class SetUser(user: User) extends Effect[Unit]

case class RemoveUser(nick: String) extends Effect[Unit]

case class ResetUsers(state: Map[String, User]) extends Effect[Unit]
