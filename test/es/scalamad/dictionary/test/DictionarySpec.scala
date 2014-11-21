package es.scalamad.dictionary.test

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest._
import org.scalatest.mock._
import org.scalatestplus.play._

import play.api._
import play.api.cache._
import play.api.libs.json._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import es.scalamad.dictionary.{ models, controllers, services }
import models._
import services._
import controllers._

class DictionarySpec extends PlaySpec with Results with OneAppPerSuite {

  object FakeDictionaryController extends DictionaryController 
      with MapRepoInterpreter {

    val result = interpreter(
      for {
	_ <- setUser(User("Mr", "Proper", Option(READ_WRITE)))
	_ <- setUser(User("Don", "Limpio", Option(READ)))
	_ <- setUser(User("Wipp", "Express", None))
	_ <- setEntry("known" -> "a very well known word")
      } yield (), getState)

    result.map(t => setState(t._2))
  }

  "add service" should {

    "allow adding new words if the user is empowered to do so" in {
      val request = FakeRequest(
        POST, 
        "/", 
        FakeHeaders(Seq(("user", Seq("mr_proper")))),
    	("new", "a new definition"))
      val result = FakeDictionaryController.add(request)
      status(result) mustEqual CREATED
    }

    "fail if the user is not empowered to do so" in {
      val request = FakeRequest(
    	POST, 
    	"/", 
    	FakeHeaders(Seq(("user", Seq("don_limpio")))),
    	("new", "a brand new definition"))
      val result = FakeDictionaryController.add(request)
      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustEqual "Could not add the new word"

      val request2 = request.withHeaders(("user", "wipp_express"))
      val result2 = FakeDictionaryController.add(request2)
      status(result2) mustEqual FORBIDDEN
      contentAsString(result2) mustEqual "Could not add the new word"
    }

    "fail if the user does not provide a `user` request" in {
      val request = FakeRequest(POST, "/", FakeHeaders(), 
        ("new", "a brand new definition"))
      val result = FakeDictionaryController.add(request)
      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustEqual "Could not add the new word"
    }
  }

  "search service" should {

    "find an existing word if the user is empowered to do so" in {
      val word = "known"
      val request = 
        FakeRequest(GET, s"/$word").withHeaders(("user" -> "don_limpio"))
      val result = FakeDictionaryController.search(word)(request)
      status(result) mustEqual OK
      contentAsString(result) mustEqual "a very well known word"
    }

    "not find a non-existing word" in {
      val word = "unknown"
      val request = 
        FakeRequest(GET, s"/$word").withHeaders(("user" -> "don_limpio"))
      val result = FakeDictionaryController.search(word)(request)
      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustEqual s"Could not find the requested word"
    }
  }
}
