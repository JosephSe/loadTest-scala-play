package controllers

import javax.inject.Inject

import model.{ResponseData, XmlFile, ZipFile}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.{FileService, MessageBroadcaster, ResponseService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Joseph Sebastian on 26/01/2016.
  */
class AsyncController @Inject()(val responseService: ResponseService, messageBroadcaster: MessageBroadcaster, fileService: FileService) extends Controller {

  val printer = new xml.PrettyPrinter(10, 4)

  def booking = Action.async { implicit request =>
    val fileName = request.getQueryString("fileName")
    if (fileName.isDefined) {
      request.headers.get("Content-Type").get match {
        case "application/xml" =>
          val xmlContent = request.body.asXml.get
          responseService.saveFile(new XmlFile(fileName.get, xmlContent.toString())).map {
            result => if (result.isRight) {
              val id = result.right.toOption.get.toString
              getAndBroadcast(s"${fileName.get}.xml", id)
              Created(id)
            }
            else BadRequest(result.left.toOption.get)
          }
        case "application/zip" =>
          val content = request.body.asRaw.get.asBytes()
          responseService.saveFile(new ZipFile(fileName.get, content.get)).map {
            result => if (result.isRight) {
              val id = result.right.toOption.get.toString
              getAndBroadcast(s"${fileName.get}.zip", id)
              Created(id)
            }
            else BadRequest(result.left.toOption.get)
          }
        case _ => Future {
          BadRequest("")
        }
      }
    } else Future {
      BadRequest
    }

  }

  private def getAndBroadcast(fileName: String, id:String) = {
    val file = fileService.findByNameAndId(fileName, id).map {
      case xml: Some[XmlFile] => messageBroadcaster.broadcast(Json.toJson(new ResponseData(xml.get).withNew.withoutData))
      case zip: Some[ZipFile] => messageBroadcaster.broadcast(Json.toJson(new ResponseData(zip.get).withNew.withoutData))
//      case data: ResponseData => messageBroadcaster.broadcast(Json.toJson(data))
      case _ => println("No data found")
    }
  }

}
