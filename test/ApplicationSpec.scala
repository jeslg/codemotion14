import scala.concurrent.Future

import org.scalatest._
import org.scalatestplus.play._

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import controllers.Application

class ApplicationSpec extends PlaySpec with Results {

  class TestController() extends Controller with Application

  "add service" should {
    "should be valid" in {
      val controller = new TestController()
      val request = FakeRequest(PUT, "/new", FakeHeaders(), "a brand new word")
      val result: Future[Result] = controller.add("new").apply(request)
      status(result) mustEqual OK
    }
  }
}
