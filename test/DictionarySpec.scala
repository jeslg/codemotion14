import scala.concurrent.Future

import org.scalatest._
import org.scalatestplus.play._

import play.api.libs.json._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import models._
import controllers.DictionaryApp

class DictionarySpec extends PlaySpec with Results with OneAppPerSuite {

  class TestController() extends Controller with DictionaryApp

  val controller = new TestController()
  import controller.{ add, search, Dictionary }

  "add service" should {
    
    "allow adding unknown words" in {
      val word = "new"
      val request = FakeRequest(
	PUT, 
	s"/$word", 
	FakeHeaders(Seq(("user", Seq("don_limpio")))), 
	(word, "a new definition"))
      val result = add apply request
      status(result) mustEqual OK
    }

    "fail if the word does already exist" in {
      val word = "new"
      val request = FakeRequest(
	PUT, 
	s"/$word", 
	FakeHeaders(), 
	("new", "a brand new definition"))
      val result: Future[Result] = add apply request
      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustEqual s"The word '$word' does already exist"
    }
  }

  "search service" should {

    "find an existing word" in {
      val word = "known"
      Dictionary.set(word -> "a well known word")
      val request = FakeRequest(GET, s"/$word")
      val result: Future[Result] = search(word) apply request
      status(result) mustEqual OK
      contentAsString(result) mustEqual "a well known word"
    }

    "not find a non-existing word" in {
      val word = "unknown"
      val request = FakeRequest(
	GET, 
	s"/$word")
      val result: Future[Result] = search(word) apply request
      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustEqual s"The word '$word' does not exist"
    }
  }
}
