package controllers

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import play.api.mvc.Results
import service._

/**
  * Created by Joseph Sebastian on 19/07/2016.
  */
class JenkinsControllerTest extends FlatSpec with Results with MockFactory with Matchers {
//class JenkinsControllerTest extends FlatSpec with PlaySpecification with Results with MockFactory with Matchers {

  val service = stub[JenkinsService]
//  val cache = stub[Cached]
//  "Jenkins Page#index" should {
//    "should be valid" in {
//      val controller = new JenkinsController(service, cache)
//      val result:Future[Result] = controller.jenkins().apply(FakeRequest())
//      val bodyText = contentAsString(result)
//      bodyText mustBe "ok"
//    }
//  }


}
