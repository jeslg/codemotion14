package models.org.hablapps.codemotion14

trait Permission
case object READ extends Permission
case object READ_WRITE extends Permission

object Permission {

  val canRead: User => Boolean = _.permission.fold(false)(_ => true)

  val canWrite: User => Boolean = _.permission.fold(false)(_ == READ_WRITE)
}
