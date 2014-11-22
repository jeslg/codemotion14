package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._
import Repo._

trait PermissionServices { this: UserServices =>

  val canRead: User => Repo[Boolean] = CanRead(_, Return(_))

  val canWrite: User => Repo[Boolean] = CanWrite(_, Return(_))

  val nickCanRead: String => Repo[Option[Boolean]] = 
    optComposeK(canRead, getUser)

  val nickCanWrite: String => Repo[Option[Boolean]] =
    optComposeK(canWrite, getUser)
}
