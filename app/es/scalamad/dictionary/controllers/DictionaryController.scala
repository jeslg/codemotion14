package es.scalamad.dictionary.controllers

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import play.api.Play.current

import es.scalamad.dictionary.models._
import es.scalamad.dictionary.services._
import Effect.composeK

object DictionaryController extends DictionaryController
  with CacheDictionaryServices
  
trait DictionaryController extends Controller
  with DictionaryUtils
  with DictionaryServices
  with UserServices 
  with WordServices {

  // GET /

  def helloDictionary: Action[AnyContent] = Action {
    Ok("Welcome to the ScalaMAD Dictionary!")
  }

  // GET /:word

  def search(word: String): Action[AnyContent] =
    ActionBuilder(getEntry, parse.anyContent)
      .withTranslator(_ => word)
      .withInterpreter(impure _)
      .withResult {
        _.map(_.fold(NotFound("Could not find the requested word"))(Ok(_)))
      }.toAction

  // POST /

  def add: Action[(String, String)] =
    ActionBuilder(setEntry, jsToWordParser)
      .withTranslator(_.body)
      .withInterpreter(impure _)
      .withResult {
        _.map(_ => Created("The word has been added successfully"))
          // FIXME: where can I get this location?
          // .withHeaders((LOCATION -> url))
      }.toAction
  
  val jsToWordParser: BodyParser[(String,String)] = parse.json map jsToWord
}

trait DictionaryUtils { this: DictionaryController =>

  implicit class OptionExtensions[T](option: Option[T]) {

    def unless(c: => Boolean): Option[T] = 
      option.unless(_ => c)

    def unless(condition: T => Boolean): Option[T] = 
      option.filter(condition andThen (!_)) // ! condition(_)
  }

  def jsToWord(jsv: JsValue): (String, String) =
    (jsv \ "word").as[String] -> (jsv \ "definition").as[String]

  class ActionBuilder[In, Out, Body](
    service: In => Effect[Out],
    parser: BodyParser[Body],
    translator: Option[Request[Body] => In] = None,
    interpreter: Option[Effect[Out] => Future[Out]] = None,
    result: Option[Future[Out] => Future[Result]] = None) {

    def withTranslator(translator: Request[Body] => In) = 
      new ActionBuilder(
        service, parser, Option(translator), interpreter, result)

    def withInterpreter(interpreter: Effect[Out] => Future[Out]) =
      new ActionBuilder(
        service, parser, translator, Option(interpreter), result)

    def withResult(result: Future[Out] => Future[Result]) =
      new ActionBuilder(
        service, parser, translator, interpreter, Option(result))

    def toAction: Action[Body] = {
      Action.async(parser)(
        translator.get
          andThen service
          andThen (interpreter.get)
          andThen (result.get))
    }
  }

  object ActionBuilder {

    def apply[In, Out, Body](
        service: In => Effect[Out], 
        parser: BodyParser[Body]) =
      new ActionBuilder[In, Out, Body](service, parser)
  }
}
