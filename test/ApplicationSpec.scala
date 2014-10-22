import scala.concurrent.Future

import org.scalatest._
import org.scalatestplus.play._

import play.api.libs.json._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import controllers.Application

class ApplicationSpec extends PlaySpec with Results {

  class TestController() extends Controller with Application

  "add service" should {
    
    "allow adding unknown words" in {
      val controller = new TestController()
      val word = "new"
      val request = FakeRequest(PUT, s"/$word", FakeHeaders(), "brand new word")
      val result: Future[Result] = controller.add(word).apply(request)
      status(result) mustEqual OK
    }

    "fail if the word does already exist" in {
      val controller = new TestController()
      val word = "emotion"
      val request = FakeRequest(PUT, s"/$word", FakeHeaders(), "new emotion")
      val result = controller.add(word).apply(request)
      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustEqual s"the word '$word' does already exist"
    }
  }

  "addPost service" should {
    
    "allow adding unknown words" in {
      val controller = new TestController()
      val body = ("new", "brand new word")
      val request = FakeRequest(POST, "/", FakeHeaders(), body)
      val result: Future[Result] = controller.addPost.apply(request)
      status(result) mustEqual OK
    }

    "fail if the word does already exist" in {
      val controller = new TestController()
      val word = "emotion"
      val body = (word, "new emotion")
      val request = FakeRequest(POST, "/", FakeHeaders(), body)
      val result: Future[Result] = controller.addPost.apply(request)
      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustEqual s"the word '$word' does already exist"
    }
  }
}
