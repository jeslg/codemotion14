package es.scalamad.dictionary.services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.cache.Cache
import play.api.Play.current

import es.scalamad.dictionary.models._

trait DictionaryServices {

  def pure[A](
      effect: Effect[A], 
      state: ApplicationState): Future[(A, ApplicationState)] =

    effect match {
      case GetEntry(word, next) => {
        pure(next(state.words.get(word)), state)
      }
      case SetEntry(entry, next) => {
        pure(next, state.copy(words = state.words + entry))
      }
      case RemoveEntry(word, next) => {
        pure(next, state.copy(words = state.words - word))
      }
      case ResetEntries(nwords, next) => {
        pure(next, state.copy(words = nwords))
      }
      case GetUser(nick, next) => {
        println(s"GetUser: ${state.users.get(nick)}")
        pure(next(state.users.get(nick)), state)
      }
      case SetUser(user, next) => {
        val nuser = user.nick -> user
        pure(next, state.copy(users = state.users + nuser))
      }
      case RemoveUser(nick, next) => {
        pure(next, state.copy(users = state.users - nick))
      }
      case ResetUsers(nusers, next) => {
        pure(next, state.copy(users = nusers))
      }
      case CanRead(user, next) => {
        pure(next(user.permission.fold(false)(_ => true)), state)
      }
      case CanWrite(user, next) => {
        pure(next(user.permission.fold(false)(_ == READ_WRITE)), state)
      }
      case Return(value) => Future((value, state))
    }

  def impure[A](effect: Effect[A]): Future[A]
}

trait CacheDictionaryServices extends DictionaryServices {

  def impure[A](effect: Effect[A]): Future[A] = {
    val now = Cache.getOrElse[ApplicationState](STATE_KEY)(dfState)
    pure(effect, now) map { case (a, next) =>
      Cache.set(STATE_KEY, next)
      a
    }
  }

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
