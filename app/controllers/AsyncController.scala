package controllers

import javax.inject.Inject

import model.{XmlFile, ZipFile}
import play.api.mvc.{Action, Controller}
import service.ResponseService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
        case "application/zip" =>
          val content = request.body.asRaw.get.asBytes()
          responseService.saveZip(new ZipFile(fileName.get, content.get)).map {
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
