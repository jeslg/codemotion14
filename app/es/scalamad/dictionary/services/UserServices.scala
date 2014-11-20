package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait UserServices extends DictionaryServices {

  val getUser: String => Effect[Option[User]] = 
    GetUser(_, Return(_))

  val setUser: User => Effect[Unit] = 
    SetUser(_, Return(()))

  val removeUser: String => Effect[Unit] = 
    RemoveUser(_, Return(()))

  val resetUsers: Map[String, User] => Effect[Unit] = 
    ResetUsers(_, Return(()))
}
