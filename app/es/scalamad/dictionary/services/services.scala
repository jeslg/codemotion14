package es.scalamad.dictionary.services

import play.api.cache.Cache
import play.api.Play.current

import es.scalamad.dictionary.models._

trait DictionaryServices {

  type Service[A] = ApplicationState => (A, ApplicationState)
  
  def getState: ApplicationState

  def setState(state: ApplicationState): DictionaryServices

  def run[A](service: Service[A]): A = {
    val (a, state) = service(getState)
    setState(state)
    a
  }

  def interpreter[A](
      effects: Effect[A], 
      state: ApplicationState): Option[(A, ApplicationState)] =

    effects match {
      case GetEntry(word) => {
	state.words.get(word).map((_, state))
      }
      case SetEntry(word, definition) => {
	Option(((), state.copy(words = state.words + (word -> definition))))
      }
      case RemoveEntry(word) => {
	Option(((), state.copy(words = state.words - word)))
      }
      case ResetEntries(nwords) => {
	Option(((), state.copy(words = nwords)))
      }
      case FlatMap(ra, f) => interpreter(ra, state).flatMap { case (a, nst) =>
	interpreter(f(a), nst)
      }
      case MapM(ra, f) => interpreter(ra, state).map { case (a, nst) =>
	(f(a), nst)
      }
    }

  def irun[A](effects: Effect[A]): Option[A] = {
    interpreter(effects, getState) map { case (a, state) =>
      setState(state)
      a
    }
  }
}

trait CacheDictionaryServices extends DictionaryServices {

  private val STATE_KEY = "state"

  val dfState = ApplicationState(
    Map(
      "mr_proper"    -> User("Mr", "Proper", Option(READ_WRITE)),
      "don_limpio"   -> User("Don", "Limpio", Option(READ)),
      "wipp_express" -> User("Wipp", "Express", None)), 
    Map(
      "hello" -> "greeting",
      "apple" -> "fruit"))

  def getState: ApplicationState = 
    Cache.getOrElse[ApplicationState](STATE_KEY)(dfState)

  def setState(state: ApplicationState) = {
    Cache.set(STATE_KEY, state)
    this
  }
}
