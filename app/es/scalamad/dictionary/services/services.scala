package es.scalamad.dictionary.services

import play.api.cache.Cache
import play.api.Play.current

import es.scalamad.dictionary.models._

trait DictionaryServices {

  type Service[A] = ApplicationState => (A, ApplicationState)
  
  def getState: ApplicationState

  def setState(state: ApplicationState): DictionaryServices

  def run[A](service: Service[A]): A = {
    val (ret, state) = service(getState)
    setState(state)
    ret
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
