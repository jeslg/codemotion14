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
import Effect._

object DictionaryController extends DictionaryController
  with CacheDictionaryServices
  
trait DictionaryController extends Controller
  with DictionaryFunctions
  with DictionaryUtils
  with DictionaryServices
  with UserServices 
  with WordServices {

  // GET /

  def helloDictionary: Action[AnyContent] = Action {
    Ok("Welcome to the ScalaMAD Dictionary!")
  }

  // GET /:word

  case class FromEffect[A, B](effect: A => Effect[B])(
      translator: Request[_] => A,
      interpreter: Effect[B] => Option[B],
      result: Option[B] => Result) {

    def build: Request[_] => Result =
      result compose interpreter compose effect compose translator
  }

  val searchEffect: String => Effect[Option[String]] = getEntry(_)

  def searchResult(word: String): Option[Option[String]] => Result =
    _.flatten.map(Ok(_)).getOrElse {
      NotFound(s"The word '$word' does not exist")
    }

  def search(word: String): Action[AnyContent] = 
    (Action
     andThen UserRefiner
     andThen ReadFilter
     andThen UserLogging) {
      FromEffect(searchEffect)(_ => word, orun _, searchResult(word)).build
    }

  // def search(word: String): Action[AnyContent] =
  //   (Action 
  //    andThen UserRefiner 
  //    andThen ReadFilter 
  //    andThen UserLogging) {
  //     irun(getEntry(word)).map(d => Ok(d)).getOrElse {
  //       NotFound(s"The word '$word' does not exist")
  //     }
  //   }

  // POST /

  def add: Action[(String,String)] = {
    (Action 
     andThen UserRefiner 
     andThen WriteFilter 
     andThen UserLogging)(jsToWordParser) { urequest =>
       val entry@(word, _) = urequest.body
       irun(setEntry(entry))
       val url = routes.DictionaryController.search(word).url
       Created(s"The word '$word' has been added successfully")
         .withHeaders((LOCATION -> url))
    }
  }
  
  val jsToWordParser: BodyParser[(String,String)] = parse.json map jsToWord
}

trait DictionaryUtils {

  implicit class OptionExtensions[T](option: Option[T]) {

    def unless(c: => Boolean): Option[T] = 
      option.unless(_ => c)

    def unless(condition: T => Boolean): Option[T] = 
      option.filter(condition andThen (!_)) // ! condition(_)
  }

  def jsToWord(jsv: JsValue): (String, String) =
    (jsv \ "word").as[String] -> (jsv \ "definition").as[String]
}

trait DictionaryFunctions { this: Controller 
    with DictionaryUtils 
    with DictionaryServices 
    with UserServices =>

  val USER_HEADER_NAME = "user"

  class UserRequest[A](
    val user: User, 
    request: Request[A]) extends WrappedRequest[A](request)

  object UserRefiner extends ActionRefiner[Request, UserRequest] {

    def refine[A](request: Request[A]): Future[Either[Result,UserRequest[A]]] =
      Future {
        request.headers
          .get(USER_HEADER_NAME)
          .map(nick => run(getUser(nick)))
          .flatten
          .map(new UserRequest(_, request))
          .toRight(Unauthorized(s"Invalid '$USER_HEADER_NAME' header"))
      }
  }

  object UserLogging extends ActionTransformer[UserRequest, UserRequest] {

    def transform[A](urequest: UserRequest[A]): Future[UserRequest[A]] = 
      Future {
        Logger.info(s"@${urequest.user.nick} requests ${urequest.toString}")
        urequest
      }
  }

  class PermissionFilter(
      permitted: User => Boolean, 
      err: String) extends ActionFilter[UserRequest] {

    def filter[A](urequest: UserRequest[A]): Future[Option[Result]] = 
      Future {
        Option(Forbidden(err))
          .unless(permitted(urequest.user))
      }
  }

  object ReadFilter extends PermissionFilter(
    Permission.canRead,
    "You are not allowed to read")

  object WriteFilter extends PermissionFilter(
    Permission.canWrite, 
    "You are not allowed to write")
}
