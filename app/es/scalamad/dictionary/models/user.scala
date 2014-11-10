package es.scalamad.dictionary.models

import play.api.cache.Cache
import play.api.Play.current
import play.api.Logger

case class User(
    name: String, 
    last: String, 
    permission: Option[Permission] = None) {

  def nick = s"${name.toLowerCase}_${last.toLowerCase}"
}

trait UserService {

  val userRepository: UserRepository

  def getUser(nick: String) = userRepository.get(nick)

  def addUser(user: User) = userRepository.add(user)

  def resetUsers(users: User*) = userRepository.reset(users: _*)
}

trait CacheUserService extends UserService {
  override val userRepository: UserRepository = new CacheUserRepository
}

trait UserRepository {

  def get(nick: String): Option[User]

  def add(user: User): Unit

  def reset(users: User*): Unit
}

class CacheUserRepository extends UserRepository {

  type Users = Map[String, User]

  private def users = Cache.getOrElse[Users]("users")(Map())

  def get(nick: String): Option[User] = users get nick

  def add(user: User): Unit = {
    Logger.info(s"Adding '${user.nick}' to user list.")
    Cache.set("users", users + (user.nick -> user))
  }

  def reset(users: User*): Unit =
    Cache.set("users", users.map(u => u.nick -> u).toMap)
}
