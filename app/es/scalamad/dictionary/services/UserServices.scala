package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait UserServices extends DictionaryServices {

  val getUser: String => Repo[Option[User]] = 
    GetUser(_, Return(_))

  val setUser: User => Repo[Unit] = 
    SetUser(_, Return(()))

  val removeUser: String => Repo[Unit] = 
    RemoveUser(_, Return(()))

  val resetUsers: Map[String, User] => Repo[Unit] = 
    ResetUsers(_, Return(()))
}
