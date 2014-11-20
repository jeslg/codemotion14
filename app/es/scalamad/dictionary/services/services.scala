package es.scalamad.dictionary.services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.cache.Cache
import play.api.Play.current

import es.scalamad.dictionary.models._

trait DictionaryServices {

  def interpreter[A](
      effect: Effect[A], 
      state: ApplicationState): Future[(A, ApplicationState)] =

    effect match {
      case GetEntry(word, next) => {
        interpreter(next(state.words.get(word)), state)
      }
      case SetEntry(entry, next) => {
        interpreter(next, state.copy(words = state.words + entry))
      }
      case RemoveEntry(word, next) => {
        interpreter(next, state.copy(words = state.words - word))
      }
      case ResetEntries(nwords, next) => {
        interpreter(next, state.copy(words = nwords))
      }
      case GetUser(nick, next) => {
        interpreter(next(state.users.get(nick)), state)
      }
      case SetUser(user, next) => {
        val nuser = user.nick -> user
        interpreter(next, state.copy(users = state.users + nuser))
      }
      case RemoveUser(nick, next) => {
        interpreter(next, state.copy(users = state.users - nick))
      }
      case ResetUsers(nusers, next) => {
        interpreter(next, state.copy(users = nusers))
      }
      case CanRead(user, next) => {
        interpreter(next(user.permission.fold(false)(_ => true)), state)
      }
      case CanWrite(user, next) => {
        interpreter(next(user.permission.fold(false)(_ == READ_WRITE)), state)
      }
      case Return(value) => Future((value, state))
    }

  def impure[A](effect: Effect[A]): Future[A]
}

trait CacheDictionaryServices extends DictionaryServices {

  def impure[A](effect: Effect[A]): Future[A] = {
    interpreter(effect, getState) map { case (a, state) =>
      setState(state)
      a
    }
  }

  def getState: ApplicationState = 
    Cache.getOrElse[ApplicationState](STATE_KEY)(dfState)

  def setState(state: ApplicationState): Unit =
    Cache.set(STATE_KEY, state)

  private val STATE_KEY = "state"

  val dfState = ApplicationState(
    Map(
      "mr_proper"    -> User("Mr", "Proper", Option(READ_WRITE)),
      "don_limpio"   -> User("Don", "Limpio", Option(READ)),
      "wipp_express" -> User("Wipp", "Express", None)), 
    Map(
      "hello" -> "greeting",
      "apple" -> "fruit"))
}
