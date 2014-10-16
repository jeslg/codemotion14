package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.Play.current

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
          Some(Forbidden(s"The word '$word' does already exists"))
        else
          None
      }
  }

  def naughtyList: Future[List[String]] = {
    val holder: WSRequestHolder =
      WS.url(s"http://localhost:9000/assets/bad_words.txt")
    holder.get map (_.body.lines.toList)
  }

  def add(word: String) = WordFilter(word).async(parse.text) { request =>
    naughtyList map { list =>
      list find (request.body.split(" +") contains _) match {
        case None => {
          // FIXME: non-atomic operation => concurrency issues
          state = state + (word -> request.body)
          Ok
        }
        case Some(bad) => Forbidden(s"We think '$bad' doesn't fit here")
      }
    }
  }

}
