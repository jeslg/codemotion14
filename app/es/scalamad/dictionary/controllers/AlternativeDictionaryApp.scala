package es.scalamad.dictionary.controllers

import play.api._
import play.api.mvc._

object AlternativeDictionaryApp extends Controller {
  
  val TOKEN = "1234567890"

  def wsToken = Action(Ok(TOKEN))

  def wsSearch(word: String) = Action(parse.text) { request =>
    val tk = request.body
    if (tk == TOKEN) {
      word match {
	case "code" => Ok("a set of rules")
	case "emotion" => Ok("a feeling of any kind")
	case _ => NotFound(s"could not find '$word' in alt dictionary")
      }
    } else Forbidden(s"invalid token '$tk'")
  }
}
