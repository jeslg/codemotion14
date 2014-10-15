package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  val state = Map(
    "code" -> "a collection of laws or rules",
    "emotion" -> "a feeling of any kind")

  def search(word: String) = Action {
    state.get(word)
      .map(Ok(_))
      .getOrElse(NotFound("unknown word!"))
  }

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}
