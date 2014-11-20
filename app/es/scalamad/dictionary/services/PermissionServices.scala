package es.scalamad.dictionary.services

import es.scalamad.dictionary.models._

trait PermissionServices extends DictionaryServices {

  val canRead: User => Effect[Boolean] = 
    CanRead(_, Return(_))

  val canWrite: User => Effect[Boolean] = 
    CanWrite(_, Return(_))
}
