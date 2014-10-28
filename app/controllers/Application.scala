package controllers

import play.api._
import play.api.cache.Cache
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Application { this: Controller =>

  type Dictionary = Map[String, String]

  def dictionary = Cache.getOrElse[Dictionary]("dictionary")(Map())

   // TODO: web service invocation
  def ws(word: String): Future[Option[String]] = Future {
    val wsState = Map(
      "programming" -> "the process of writing computer programs",
      "subroutine" -> "a set of instructions designed to perform a frequently used operation within a program")
    wsState.get(word)
  }

  class WordRequest[A](
    val word: String,
    val definition: String,
    request: Request[A]) extends WrappedRequest[A](request)

  def WordTransducer(word: String) = new ActionRefiner[Request, WordRequest] {
    def refine[A](request: Request[A]) = for {
      owr <- Future(dictionary.get(word).map(new WordRequest(word, _, request)))
      owr2 <- owr match {
	case None => ws(word).map(_.map(new WordRequest(word, _, request)))
	case _ => Future.successful(owr)
      }
    } yield owr2.toRight(NotFound(s"could not find '$word'"))
  }

  implicit class WordsOp(s: String) {
    def words = s.split("[ \n]+")
  }

  def naughtyList: Future[List[String]] = {
    val holder: WSRequestHolder =
      WS.url(s"http://localhost:9000/assets/bad_words.txt")
    holder.get map (_.body.words.toList)
  }

  def ParentalFilter = new ActionFilter[WordRequest] {
    def filter[A](wrequest: WordRequest[A]) = naughtyList map { list =>
      list
        .find(wrequest.definition.words contains _)
        .map(bad => Forbidden(s"parental filter blocked a request"))
    }
  }

  def UpperTransformer = new ActionTransformer[WordRequest, WordRequest] {
    def transform[A](wrequest: WordRequest[A]) = Future {
      /* The `WrappedRequest` does already have a `copy` method, so the case
       * class won't override it. Therefore, we have to use the next ugly
       * syntax, instead of `wrequest.copy(definition=wr.definition.toUpper)`.
       */
      new WordRequest(
        wrequest.word, 
        wrequest.definition.toUpperCase,
        wrequest)
    }
  }

  def search(word: String) =
    (Action
     andThen WordTransducer(word)
     andThen ParentalFilter
     andThen UpperTransformer) { wrequest =>
    Ok(wrequest.definition)
  }

  def addResult(word: String, definition: String) =
    if (dictionary.isDefinedAt(word))
      Forbidden(s"the word '$word' does already exist")
    else {
      Cache.set("dictionary", dictionary + (word -> definition))
      Ok
    }

  def AddTransformer(word: String) = new ActionTransformer[Request, WordRequest] {
    def transform[A](request: Request[A]) = Future {
      new WordRequest(word, request.body.toString, request)
    }
  }

  def add(word: String) =
    (Action
     andThen AddTransformer(word)
     andThen ParentalFilter)(parse.text) { request =>
      addResult(word, request.body) // TODO: what do we do with `addResult`?
    }

  val jsWordBodyParser: BodyParser[(String, String)] = parse.json map { jsv => 
    ((jsv \ "word").as[String] -> ((jsv \ "definition").as[String]))
  }

  def addPost = Action(jsWordBodyParser) { request =>
    val (word, definition) = request.body
    addResult(word, definition)
  }

}

object Application extends Controller with Application
