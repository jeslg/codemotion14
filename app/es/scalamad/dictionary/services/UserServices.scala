package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait UserServices extends DictionaryServices {

  val getUser: String => Effect[Option[User]] = GetUser(_)

  val setUser: User => Effect[Unit] = SetUser(_)

  val removeUser: String => Effect[Unit] = RemoveUser(_)

  val resetUsers: Map[String, User] => Effect[Unit] = ResetUsers(_)
}
