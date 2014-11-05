package models

import play.api.cache.Cache
import play.api.Play.current
import play.api.Logger

object Users {

  type Users = Map[String, User]

  private def users = Cache.getOrElse[Users]("users")(Map())

  def get(nick: String) = users get nick

  def add(user: User) = {
    Logger.info(s"Adding '${user.nick}' to user list.")
    Cache.set("users", users + (user.nick -> user))
  }

  // adds some initial users, this could be moved to an external service
  add(User("Mr", "Proper", Option(READ_WRITE)))
  add(User("Don", "Limpio", Option(READ)))
  add(User("Wipp", "Express"))
}
