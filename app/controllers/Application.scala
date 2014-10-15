package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  var state = Map(
    "code" -> "a collection of laws or rules",
    "emotion" -> "a feeling of any kind")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def search(word: String) = Action {
    Ok(state(word))
  }

}
