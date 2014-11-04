import scala.concurrent.Future

import org.scalatest._
import org.scalatestplus.play._

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import models._
import controllers.DictionaryApp

class DictionarySpec extends PlaySpec with Results with OneAppPerSuite {

  class TestController() extends Controller with DictionaryApp

  val controller = new TestController()
  import controller.{ add, search, Dictionary, Users }

  "add service" should {

    "allow adding new words if the user is empowered to do so" in {

      Users.add(User("Mr", "Proper", Option(WRITE)))
      Users.add(User("Don", "Limpio", Option(READ)))
      Users.add(User("Wipp", "Express"))

      val word = "new"
      val request = FakeRequest(
	POST, 
	"/", 
	FakeHeaders(Seq(("user", Seq("mr_proper")))),
	(word, "a new definition"))
      val result = add apply request
      status(result) mustEqual OK
    }

    "fail if the user is not empowered to do so" in {
      val word = "new"
      val request = FakeRequest(
	POST, 
	"/", 
        FakeHeaders(Seq(("user", Seq("don_limpio")))),
	(word, "a brand new definition"))
      val result: Future[Result] = add apply request
      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustEqual "You are not allowed to write"

      val request2 = request.withHeaders(("user", "wipp_express"))
      val result2 = add apply request2
      status(result2) mustEqual FORBIDDEN
      contentAsString(result2) mustEqual "You are not allowed to write"
    }
  }

  "search service" should {

    "find an existing word if the user is empowered to do so" in {
      val word = "known"
      Dictionary.set(word -> "a well known word")
      val request = FakeRequest(GET, s"/$word").withHeaders(("user" -> "don_limpio"))
      val result: Future[Result] = search(word) apply request
      status(result) mustEqual OK
      contentAsString(result) mustEqual "a well known word"
    }

    "not find a non-existing word" in {
      val word = "unknown"
      val request = FakeRequest(GET, s"/$word").withHeaders(("user" -> "don_limpio"))
      val result: Future[Result] = search(word) apply request
      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustEqual s"The word '$word' does not exist"
    }
  }
}
