package models

case class User(name: String, last: String, age: Int) {
  def nick = s"${name.toLowerCase}_${last.toLowerCase}"
}
