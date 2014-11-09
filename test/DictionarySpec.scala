import scala.concurrent.Future

import org.scalatest._
import org.scalatestplus.play._

import play.api._
import play.api.cache._
import play.api.libs.json._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import models._
import org.hablapps.codemotion14._
import controllers.DictionaryApp

class DictionarySpec extends PlaySpec with Results with OneAppPerTest {

  object DSLUtils {
    
    def withUsers(users: User*) = Users.reset(users: _*)

    def withoutUsers = withUsers()

    def withWords(entries: (String, String)*) = Dictionary.reset(entries: _*)

    def withoutWords = withWords()
  }

  import DSLUtils._

  "add service" should {

    "allow adding new words if the user is empowered to do so" in {

      withUsers(
	User("Mr", "Proper", Option(READ_WRITE)))

      val request = FakeRequest(
        POST, 
        "/", 
        FakeHeaders(Seq(("user", Seq("mr_proper")))),
    	("new", "a new definition"))
      val result = DictionaryApp.add(request)
      status(result) mustEqual CREATED
    }

    "fail if the user is not empowered to do so" in {

      withUsers(
	User("Don", "Limpio", Option(READ)),
	User("Wipp", "Express"))

      val request = FakeRequest(
	POST, 
	"/", 
	FakeHeaders(Seq(("user", Seq("don_limpio")))),
	("new", "a brand new definition"))
      val result: Future[Result] = DictionaryApp.add(request)
      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustEqual "You are not allowed to write"

      val request2 = request.withHeaders(("user", "wipp_express"))
      val result2 = DictionaryApp.add(request2)
      status(result2) mustEqual FORBIDDEN
      contentAsString(result2) mustEqual "You are not allowed to write"
    }

    "fail if the user does not provide a `user` request" in {

      withoutUsers

      val request = FakeRequest(POST, "/", FakeHeaders(), 
        ("new", "a brand new definition"))
      val result: Future[Result] = DictionaryApp.add(request)
      status(result) mustEqual UNAUTHORIZED
      contentAsString(result) mustEqual "Invalid 'user' header"
    }
  }

  "search service" should {

    "find an existing word if the user is empowered to do so" in {

      withUsers(User("Don", "Limpio", Option(READ)))
      withWords("known" -> "a well known word")

      val word = "known"
      val request = FakeRequest(GET, s"/$word")
	                   .withHeaders(("user" -> "don_limpio"))
      val result: Future[Result] = DictionaryApp.search(word)(request)
      status(result) mustEqual OK
      contentAsString(result) mustEqual "a well known word"
    }

    "not find a non-existing word" in {

      withUsers(User("Don", "Limpio", Option(READ)))
      withoutWords

      val word = "unknown"
      val request = FakeRequest(GET, s"/$word")
	                   .withHeaders(("user" -> "don_limpio"))
      val result: Future[Result] = DictionaryApp.search(word)(request)
      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustEqual s"The word '$word' does not exist"
    }
  }
}
