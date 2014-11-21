package es.scalamad.dictionary.controllers

import scala.concurrent.Future
import scala.language.implicitConversions

import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current

import es.scalamad.dictionary.models._
import es.scalamad.dictionary.services._
import Repo._

object DictionaryController extends DictionaryController 
  with CacheRepoInterpreter {

  interpreter(
    for {
      _ <- setUser(User("Mr", "Proper", Option(READ_WRITE)))
      _ <- setUser(User("Don", "Limpio", Option(READ)))
      _ <- setUser(User("Wipp", "Express", None))
      _ <- setEntry("code" -> "a system of words, letters, or signs")
      _ <- setEntry("emotion" -> "a feeling of any kind")
    } yield (), getState)
}

trait DictionaryController extends Controller
  with DictionaryUtils
  with RepoInterpreter
  with UserServices 
  with WordServices
  with PermissionServices {

  // GET /

  def helloDictionary: Action[AnyContent] = Action {
    Ok("Welcome to the ScalaMAD Dictionary!")
  }

  // GET /:word

  def search(word: String): Action[AnyContent] = searchBuilder.toAction(getState)

  def searchBuilder =
    ActionBuilder(authorizedSearch, parse.anyContent)
      .withTranslator(r => r.headers("user") -> r.path.tail)
      .withInterpreter(interpreter _)
      .withResult(_.fold(NotFound("Could not find the requested word"))(Ok(_)))

  def authorizedSearch: Tuple2[String, String] => Repo[Option[String]] = 
    if_K(
      cond = optComposeK(canRead, getUser), 
      then_K = getEntry, 
      else_K = _ => Return(None))

  // POST /

  def add: Action[(String, String)] = addBuilder.toAction(getState)

  def addBuilder =
    ActionBuilder(authorizedAdd, jsToWordParser)
      .withTranslator(r => r.headers.get("user").getOrElse("") -> r.body)
      .withInterpreter(interpreter _)
      .withResult {
	_.map(_ => Created("The word has been added successfully")).getOrElse {
	  Forbidden("Could not add the new word")
	}
      }

  def authorizedAdd: Tuple2[String, Tuple2[String, String]] => Repo[Option[Unit]] =
    if_K(
      cond = optComposeK(canWrite, getUser),
      then_K = setEntry,
      else_K = _ => Return(None))
  
  val jsToWordParser: BodyParser[(String,String)] = parse.json map jsToWord
}

trait DictionaryTestableActions { this: DictionaryController =>

  def testSearch: State => Request[AnyContent] => Future[Tuple2[Result, State]] =
    searchBuilder.toTestableAction _

  def testAdd: State => Request[Tuple2[String, String]] => Future[Tuple2[Result, State]] =
    addBuilder.toTestableAction _
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
    service: In => Repo[Out],
    parser: BodyParser[Body],
    translator: Option[Request[Body] => In] = None,
    interpreter: Option[(Repo[Out], State) => Future[(Out, State)]] = None,
    result: Option[Out => Result] = None) {

    def withTranslator(translator: Request[Body] => In) = 
      new ActionBuilder(
        service, parser, Option(translator), interpreter, result)

    def withInterpreter(interpreter: (Repo[Out], State) => Future[(Out, State)]) =
      new ActionBuilder(
        service, parser, translator, Option(interpreter), result)

    def withResult(result: Out => Result) =
      new ActionBuilder(
        service, parser, translator, interpreter, Option(result))

    def toAction(state: State): Action[Body] =
      Action.async(parser)(
        translator.get
          andThen service
          andThen (repo => interpreter.get(repo, state))
	  andThen (_.map(t => { 
	    setState(t._2)
	    result.get(t._1) 
	  })))

    def toTestableAction(state: State): Request[Body] => Future[Tuple2[Result, State]] =
      (translator.get
          andThen service
          andThen (repo => interpreter.get(repo, state))
	  andThen (_.map(t => (result.get(t._1), t._2))))
  }

  object ActionBuilder {

    def apply[In, Out, Body](
        service: In => Repo[Out], 
        parser: BodyParser[Body]) =
      new ActionBuilder[In, Out, Body](service, parser)
  }
}
