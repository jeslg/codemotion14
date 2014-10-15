package controllers

import play.api._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  var state = Map(
    "code" -> "a collection of laws or rules",
    "emotion" -> "a feeling of any kind")

  def ws(word: String): Future[Option[String]] = Future {
    Option("{ ws definition here }") // TODO: web service invocation
  }

  def search(word: String) = Action.async {
    state.get(word)
      .map(definition => Future(Ok(definition)))
      .getOrElse(ws(word) map {
        // FIXME: consider including a monad transformer here
        case Some(definition) => Ok(definition)
        case _ => NotFound("unknown word!")
      })
  }

  def WordFilter(word: String) = 
    new ActionFilter[Request] with ActionBuilder[Request] {
      def filter[A](request: Request[A]) = Future {
        if (state.exists(kv => kv._1 == word))
          Some(Forbidden(s"The word '$word' already exists"))
        else
          None
      }
  }

  def add(word: String) = WordFilter(word)(parse.text) { request =>
    // FIXME: non-atomic operation => concurrency issues
    state = state + (word -> request.body)
    Ok
  }

}
