package es.scalamad.dictionary.models

case class ApplicationState(
  users: Map[String, User], 
  words: Map[String, String])
