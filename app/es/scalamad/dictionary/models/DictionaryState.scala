package es.scalamad.dictionary.models

case class DictionaryState(
  users: Map[String, User], 
  words: Map[String, String])
