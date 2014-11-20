package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait PermissionServices extends DictionaryServices {

  val canRead: User => Repo[Boolean] = CanRead(_, Return(_))

  val canWrite: User => Repo[Boolean] = CanWrite(_, Return(_))
}
