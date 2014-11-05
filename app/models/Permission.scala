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

  // buen ejemplo de fold, y como ejercicio

  val canRead2: User => Boolean = 
    _.permission.fold(false){ _ => true }

  val canWrite2: User => Boolean = 
    _.permission.fold(false){ _ == READ_WRITE }
    
}

// también se pueden definir como extractores. y utilizarse: user match { case CanRead => ... }

object CanRead{
  def unapply(user: User): Boolean = 
    user.permission.fold(false){ _ => true }    
}

object CanWrite{
  def unapply(user: User): Boolean = 
    user.permission.fold(false){ _ == READ_WRITE }
}
