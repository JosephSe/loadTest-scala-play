import org.scalatest
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {
/*
  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("")
    }

    "return Not found status for unknown search request /xml/get xml" in new WithApplication() {
      val respoonse = route(FakeRequest(GET, "/xml/get/test1232.xml")).get

      status(respoonse) must equalTo(NOT_FOUND)
    }

    "return Not found status for unknown search request /xml/get zip" in new WithApplication() {
      val respoonse = route(FakeRequest(GET, "/xml/get/test1232.zip")).get
s
      status(respoonse) must equalTo(NOT_FOUND)
    }
    "return 200 status & respons for valid search request /xml/get xml" in new WithApplication() {
//      val respoonse = route(FakeRequest(GET, "/xml/get/test.xml")).get
//      status(respoonse) must equalTo(OK)
//      contentType(respoonse) must beSome.which(_ == "application/json")
    }
  }
*/
}
