package controllers

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */

import java.io.ByteArrayInputStream
import javax.inject.Inject

import model.ResponseData
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.{XmlService, ZipService}
import util.Utils.StringUtils

import scala.concurrent.Future

class FileController @Inject()(val xmlService: XmlService, val zipService: ZipService) extends Controller {


  def getFile(name: String) = Action.async {
    name match {
      case xml if name.endsWith(".xml") => {
        xmlService.findByName(name.removeDelimiter) map {
          case Some(xml) => val response = new ResponseData(xml)
            Ok(Json.toJson(response))
          case None => NotFound("File not found")
        }
      }
      case zip if name.endsWith(".zip") => {
        zipService.findByName(name.removeDelimiter) map {
          case Some(zip) => Ok(Json.toJson(new ResponseData(zip)))
          case None => NotFound("File not found")
        }
      }
      case _ => Future {
        NotFound("File not found")
      }
    }
  }

  def loadFile(name: String) = Action.async {
    name match {
      case xml if name.endsWith(".xml") => {
        xmlService.findByName(name.removeDelimiter) map {
          case Some(xml) => val xmlContent = xml.content.get
            Ok.stream(Enumerator.fromStream(new ByteArrayInputStream(xmlContent.getBytes())).andThen(Enumerator.eof)) //.as("application/xml")
          case None => NotFound("File not found")
        }
      }
      case zip if name.endsWith(".zip") => {
        zipService.findByName(name.removeDelimiter) map {
          case Some(zip) => Ok.stream(Enumerator.fromStream(new ByteArrayInputStream(zip.content.get)).andThen(Enumerator.eof))
          case None => NotFound("File not found")
        }
      }
      case _ => Future {
        NotFound("File not found")
      }
    }
  }

}
