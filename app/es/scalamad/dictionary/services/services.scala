package es.scalamad.dictionary.services

import play.api.cache.Cache
import play.api.Play.current

import es.scalamad.dictionary.models._

trait DictionaryServices {

  def interpreter[A](
      effects: Effect[A], 
      state: ApplicationState): Option[(A, ApplicationState)] =

    effects match {
      case GetEntry(word) => {
	Option((state.words.get(word), state))
      }
      case SetEntry(entry) => {
	Option(((), state.copy(words = state.words + entry)))
      }
      case RemoveEntry(word) => {
	Option(((), state.copy(words = state.words - word)))
      }
      case ResetEntries(nwords) => {
	Option(((), state.copy(words = nwords)))
      }
      case GetUser(nick) => {
        Option((state.users.get(nick), state))
      }
      case SetUser(user) => {
        Option(((), state.copy(users = state.users + (user.nick -> user))))
      }
      case RemoveUser(nick) => {
        Option(((), state.copy(users = state.users - nick)))
      }
      case ResetUsers(nusers) => {
        Option((), state.copy(users = nusers))
      }
      case CanRead(user) => {
        Option((user.permission.fold(false)(_ => true), state))
      }
      case CanWrite(user) => {
        Option((user.permission.fold(false)(_ == READ_WRITE), state))
      }
      case FlatMap(ra, f) => interpreter(ra, state).flatMap { case (a, nst) =>
	interpreter(f(a), nst)
      }
      case MapM(ra, f) => interpreter(ra, state).map { case (a, nst) =>
	(f(a), nst)
      }
    }

  def impure[A](effect: Effect[A]): Option[A]
}

trait CacheDictionaryServices extends DictionaryServices {

  def impure[A](effect: Effect[A]): Option[A] = {
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
