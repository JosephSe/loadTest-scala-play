package controller

import controllers.ResponseController
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}
import service.ResponseService

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Joseph Sebastian on 02/02/2016.
  */
class ResponseControllerTest extends FlatSpec with MockFactory with Matchers {

  val responseService = stub[ResponseService]
  val underTest = new ResponseController(responseService)
  val xmlResponse = Future{Map("xml" -> List())}
  val zipResponse =  Future{Map("zip" -> List())}
  (responseService.all _) when("xml") returns xmlResponse
  (responseService.all _) when("zip") returns zipResponse

  "ResponseController.all should call" should "call responseService.all" in {
    val response = underTest.all("xml")
    response should equal(xmlResponse)
  }
}
