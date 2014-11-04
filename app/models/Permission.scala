package models

trait Permission
case object READ extends Permission
case object READ_WRITE extends Permission

object Permission {

  val canRead: User => Boolean = _.permission match {
    case Some(READ ) => true
    case Some(READ_WRITE) => true
    case _           => false    
  }

  val canWrite: User => Boolean = _.permission match {
    case Some(READ_WRITE) => true
    case _           => false
  }
}
