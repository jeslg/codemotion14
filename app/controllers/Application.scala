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

  object Dictionary {

    private def dictionary = Cache.getOrElse[Dictionary]("dictionary")(Map())

    def contains(word: String) = dictionary isDefinedAt word 

    def get(word: String) = dictionary get word

    def set(word: String, definition: String) =
      Cache.set("dictionary", dictionary + (word -> definition))
  }

   // TODO: web service invocation
  def ws(word: String): Future[Option[String]] = Future {
    val wsState = Map(
      "code" -> "a collection of laws or rules",
      "emotion" -> "a (strong) feeling of any kind")
    wsState.get(word)
  }

  class WordRequest[A](
    val word: String,
    val definition: String,
    request: Request[A]) extends WrappedRequest[A](request)

  def WordTransducer(word: String) = new ActionRefiner[Request, WordRequest] {
    def refine[A](request: Request[A]) = for {
      owr <- Future(Dictionary.get(word).map(new WordRequest(word, _, request)))
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
    if (Dictionary contains word)
      Forbidden(s"the word '$word' does already exist")
    else {
      Dictionary.set(word, definition)
      Ok
    }

  def AddTransformer(word: String) = new ActionTransformer[Request, WordRequest] {
    def transform[A](request: Request[A]) = Future {
      new WordRequest(word, request.body.toString, request)
    }
  }

  case class Logging[A](action: Action[A]) extends Action[A] {

    def apply(request: Request[A]): Future[Result] = {
      Logger.info(s"Incoming request: $request")
      val result = action(request)
      Logger.info(s"Returning result: $result")
      result
    }

    lazy val parser = action.parser
  }

  def add(word: String) = Logging {
    (Action
      andThen AddTransformer(word)
      andThen ParentalFilter)(parse.text) { request =>
      addResult(word, request.body) // TODO: what do we do with `addResult`?
    }
  }

  def jsToWord(jsv: JsValue): (String, String) =
    (jsv \ "word").as[String] -> (jsv \ "definition").as[String]

  val jsWordBodyParser: BodyParser[(String, String)] = parse.json.map(jsToWord)

  def addPost = Action(jsWordBodyParser) { request =>
    val (word, definition) = request.body
    addResult(word, definition)
  }

  import play.api.mvc._
  import play.api.libs.iteratee._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def asJson: Enumeratee[String, JsValue] = Enumeratee.map(Json.parse)

  def asWord = Enumeratee.map(jsToWord)

  def wordFilter = 
    Enumeratee.filter[(String, String)](wd => ! (Dictionary.contains(wd._1)))

  def toCache = Iteratee.foreach[(String, String)] { case (word, definition) =>
    Logger.info(s"Adding word '$word' to dictionary")
    Dictionary.set(word, definition)
  }

  def socket = WebSocket.using[String] { request =>
    val in = asJson ><> asWord ><> wordFilter &>> toCache
    val out = Enumerator("Welcome to the Dictionary WebSocket")
    (in, out)
  }
}

object Application extends Controller with Application
