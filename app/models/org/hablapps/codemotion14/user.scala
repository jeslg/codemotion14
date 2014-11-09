package models.org.hablapps.codemotion14

import play.api.cache.Cache
import play.api.Play.current
import play.api.Logger

case class User(
    name: String, 
    last: String, 
    permission: Option[Permission] = None) {

  def nick = s"${name.toLowerCase}_${last.toLowerCase}"
}

class UserService(repository: UserRepository) {

  def getUser(nick: String) = repository.getUser(nick)

  def addUser(user: User) = repository.addUser(user)

  def resetUsers(users: User*) = repository.resetUsers(users: _*)
}

trait UserRepository {

  def getUser(nick: String): Option[User]

  def addUser(user: User): Unit

  def resetUsers(users: User*): Unit
}

class CacheUserRepository extends UserRepository {

  type Users = Map[String, User]

  private def users = Cache.getOrElse[Users]("users")(Map())

  def getUser(nick: String): Option[User] = users get nick

  def addUser(user: User): Unit = {
    Logger.info(s"Adding '${user.nick}' to user list.")
    Cache.set("users", users + (user.nick -> user))
  }

  def resetUsers(users: User*): Unit =
    Cache.set("users", users.map(u => u.nick -> u).toMap)
}
