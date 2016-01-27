package controllers

import javax.inject.Inject

import com.fasterxml.jackson.core.PrettyPrinter
import model.XmlFile
import play.api.mvc.{Action, Controller}
import service.ResponseService

import scala.concurrent.Future
import scala.xml
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Joseph Sebastian on 26/01/2016.
  */
class AsyncController @Inject()(val responseService: ResponseService) extends Controller {

  val printer = new xml.PrettyPrinter(10, 4)

  def booking = Action.async { implicit request =>
    val fileName = request.getQueryString("fileName")
    if (fileName.isDefined) {
      request.headers.get("Content-Type").get match {
        case "application/xml" =>
          val xmlContent = request.body.asXml.get
          responseService.saveXml(new XmlFile(fileName.get, xmlContent.toString())).map {
            result => if (result.isRight) Created(result.right.toOption.get.toString)
            else BadRequest(result.left.toOption.get)
          }
        //          Ok ("")
        case "application/zip" => responseService.saveXml(new XmlFile(fileName.get, request.body.toString())).map {
          result => if (result.isRight) Created(result.right.toOption.get.toString)
          else BadRequest(result.left.toOption.get)
        }
        case _ => Future {
          BadRequest
        }
      }
    } else Future {
      BadRequest
    }
  }

}
