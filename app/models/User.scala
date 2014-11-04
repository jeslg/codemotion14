package models

trait Permission
case object READ extends Permission
case object WRITE extends Permission

case class User(
    name: String, 
    last: String, 
    permission: Option[Permission] = None) {

  def nick = s"${name.toLowerCase}_${last.toLowerCase}"
}

object Permission {

  val canRead: User => Boolean = _.permission match {
    case Some(WRITE) => true
    case Some(READ ) => true
    case _           => false    
  }

  val canWrite: User => Boolean = _.permission match {
    case Some(WRITE) => true
    case _           => false
  }
}
