package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

sealed trait Effect[A] {

  def map[B](f: A => B) = flatMap[B](a => Return(f(a)))

  def flatMap[B](f: A => Effect[B]): Effect[B] = this match {
    case GetEntry(word, next) => GetEntry(word, next(_) flatMap f)
    case SetEntry(entry, next) => SetEntry(entry, next flatMap f)
    case RemoveEntry(word, next) => RemoveEntry(word, next flatMap f)
    case ResetEntries(state, next) => ResetEntries(state, next flatMap f)
    case GetUser(nick, next) => GetUser(nick, next(_) flatMap f)
    case SetUser(user, next) => SetUser(user, next flatMap f)
    case RemoveUser(nick, next) => RemoveUser(nick, next flatMap f)
    case ResetUsers(state, next) => ResetUsers(state, next flatMap f)
    case CanRead(user, next) => CanRead(user, next(_) flatMap f)
    case CanWrite(user, next) => CanWrite(user, next(_) flatMap f)
    case Return(value) => f(value)
  }
}

object Effect {

  def composeK[A, B, C](
    g: B => Effect[C],
    f: A => Effect[B]): A => Effect[C] = f(_) flatMap g
}

case class Return[A](value: A) extends Effect[A]

// words

case class GetEntry[A](word: String, next: Option[String] => Effect[A]) 
    extends Effect[A]

case class SetEntry[A](entry: (String, String), next: Effect[A]) 
    extends Effect[A]

case class RemoveEntry[A](word: String, next: Effect[A]) 
    extends Effect[A]

case class ResetEntries[A](state: Map[String, String], next: Effect[A]) 
    extends Effect[A]

// users

case class GetUser[A](nick: String, next: Option[User] => Effect[A]) 
    extends Effect[A]

case class SetUser[A](user: User, next: Effect[A]) 
    extends Effect[A]

case class RemoveUser[A](nick: String, next: Effect[A]) 
    extends Effect[A]

case class ResetUsers[A](state: Map[String, User], next: Effect[A]) 
    extends Effect[A]

// permission

case class CanRead[A](user: User, next: Boolean => Effect[A]) 
    extends Effect[A]

case class CanWrite[A](user: User, next: Boolean => Effect[A]) 
    extends Effect[A]
