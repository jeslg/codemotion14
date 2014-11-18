package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait UserServices extends DictionaryServices { 

  def getUser(nick: String): Service[Option[User]] = { state =>
    (state.users.get(nick), state)
  }

  def addUser(user: User): Service[Unit] = { state =>
    ((), state.copy(users = state.users + (user.nick -> user)))
  }

  def resetUsers(users: User*): Service[Unit] = { state =>
    val m = users.map(u => u.nick -> u).toMap
    ((), state.copy(users = m))
  }
}
