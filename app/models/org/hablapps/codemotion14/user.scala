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

object Users {

  type Users = Map[String, User]

  private def users = Cache.getOrElse[Users]("users")(Map())

  def get(nick: String) = users get nick

  def add(user: User) = {
    Logger.info(s"Adding '${user.nick}' to user list.")
    Cache.set("users", users + (user.nick -> user))
  }

  def reset(users: User*) =
    Cache.set("users", users.map(u => u.nick -> u).toMap)
}
