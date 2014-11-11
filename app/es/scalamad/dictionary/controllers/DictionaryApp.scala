package es.scalamad.dictionary.controllers

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.cache.Cache
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import play.api.Play.current

import es.scalamad.dictionary.models._
import es.scalamad.dictionary.services._

trait DictionaryApp extends Controller
  with DictionaryActions
  with DictionaryFunctions
  with DictionaryStateServices
  with DictionaryUtils
  with DictionaryWebServices
  with DictionaryWebSockets
  with UserServices
  with WordServices

object DictionaryApp extends DictionaryApp
  with CacheDictionaryStateServices

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

trait DictionaryStateServices { this: Controller =>

  def getState: DictionaryState

  def setState(state: DictionaryState): DictionaryStateServices

  def invoke[A](service: Service[A]): A = {
    val (ret, state) = service(getState)
    setState(state)
    ret
  }
}

trait DictionaryFunctions { 

  this: Controller 
    with DictionaryUtils 
    with DictionaryStateServices 
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
          .map(nick => invoke(getUser(nick)))
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

trait DictionaryWebServices { 

  this: Controller with DictionaryFunctions =>

  def wsToken: Future[String] = {
    val rel = routes.AlternativeDictionaryApp.wsToken.url
    val holder = WS.url(s"http://localhost:9000$rel")
    val response = holder.get
    response map (_.body)
  }

  def wsSearch(word: String, token: String): Future[Option[String]] = {
    val rel = routes.AlternativeDictionaryApp.wsSearch(word)
    val holder = WS.url(s"http://localhost:9000$rel").withBody(token)
    val response = holder.get
    response map { wsr =>
      wsr.status match {
        case OK => Option(wsr.body)
        case _ => None
      }
    }
  }

  def wsTokenAndSearch(word: String): Future[Option[String]] = {
    for {
      token <- wsToken
      odef  <- wsSearch(word, token)
    } yield odef
  }
}

trait DictionaryActions {

  this: Controller
    with DictionaryFunctions
    with DictionaryStateServices
    with DictionaryUtils
    with DictionaryWebServices
    with UserServices 
    with WordServices =>
    
  def helloDictionary = 
    (Action andThen UserRefiner andThen UserLogging) {
      Ok("Welcome to the CodeMotion14 Dictionary!")
    }

  val FURTHER_QUERY_NAME = "further"

  def isFurtherSearch[A](request: Request[A]): Boolean =
    request.queryString.contains(FURTHER_QUERY_NAME) &&
    request.queryString(FURTHER_QUERY_NAME).size > 0 && 
    request.queryString(FURTHER_QUERY_NAME).head.toBoolean

  def search(word: String): Action[AnyContent] =
    (Action 
     andThen UserRefiner 
     andThen ReadFilter 
     andThen UserLogging).async { urequest =>
      invoke(getWord(word)).map(d => Future(Ok(d))).getOrElse {
	if (isFurtherSearch(urequest)) {
	  wsTokenAndSearch(word).map { odef =>
	    odef.map(Ok(_)).getOrElse {
	      NotFound(s"The word '$word' does not exist")
	    }
	  }
	} else {
	  Future(NotFound(s"The word '$word' does not exist"))
	}
      }
    }

  val jsToWordParser: BodyParser[(String,String)] = parse.json map jsToWord

  def add: Action[(String,String)] = {
    (Action 
     andThen UserRefiner 
     andThen WriteFilter 
     andThen UserLogging)(jsToWordParser) { urequest =>
       val entry@(word, _) = urequest.body
       setWord(entry)
       val url = routes.DictionaryApp.search(word).url
       Created(s"The word '$word' has been added successfully")
         .withHeaders((LOCATION -> url))
    }
  }
}

trait DictionaryWebSockets {

  this: Controller 
    with DictionaryStateServices 
    with WordServices 
    with DictionaryUtils =>

  def asJson: Enumeratee[String, JsValue] = Enumeratee.map(Json.parse)

  def asEntry = Enumeratee.map(jsToWord)

  def existingFilter = 
    Enumeratee.filter[(String, String)] { 
      case (word, _) => ! (invoke(containsWord(word)))
    }

  def toDictionary = {
    Iteratee.foreach[(String, String)] { 
      case (word, definition) => invoke(setWord(word, definition))
    }
  }

  /*
   * This can be tested by using: http://websocket.org/echo.html
   */
  def socketAdd = WebSocket.using[String] { request =>
    val in = asJson ><> asEntry ><> existingFilter &>> toDictionary
    val out = Enumerator("You're using the Dictionary WebSocket Service")
    (in, out)
  }
}

trait CacheDictionaryStateServices extends DictionaryStateServices {

  this: Controller =>

  private val STATE_KEY = "state"

  val dfState = DictionaryState(
    Map(
      "mr_proper"    -> User("Mr", "Proper", Option(READ_WRITE)),
      "don_limpio"   -> User("Don", "Limpio", Option(READ)),
      "wipp_express" -> User("Wipp", "Express", None)), 
    Map(
      "hello" -> "greeting",
      "apple" -> "fruit"))

  def getState: DictionaryState = 
    Cache.getOrElse[DictionaryState](STATE_KEY)(dfState)

  def setState(state: DictionaryState) = {
    Cache.set(STATE_KEY, state)
    this
  }
}
