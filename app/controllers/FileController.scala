package controllers

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */

import java.io.ByteArrayInputStream
import javax.inject.Inject

import model.{ResponseData, XmlFile, ZipFile}
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.FileService

import scala.concurrent.ExecutionContext.Implicits.global

class FileController @Inject()(fileService: FileService) extends Controller {

  def getFile(typ: String, id: String) = Action.async {
    fileService.findByNameAndId(typ, id) map {
      case Some(xml: XmlFile) => Ok(Json.toJson(new ResponseData(xml)))
      case Some(zip: ZipFile) => Ok(Json.toJson(new ResponseData(zip)))
      case _ => {
        NotFound("File not found")
      }
    }
  }

  def downloadFileName(name: String) = Action.async {
    fileService.loadByName(name).map {
      case Some(xml: XmlFile) => Ok.chunked(Enumerator.fromStream(new ByteArrayInputStream(xml.content.get.getBytes("UTF-8"))).andThen(Enumerator.eof))
      case Some(zip: ZipFile) => Ok.chunked(Enumerator.fromStream(new ByteArrayInputStream(zip.content.get)).andThen(Enumerator.eof))
      case _ => {
        NotFound("File not found")
      }
    }
/*
    fileService.loadByName(name).map {
      case Some(xml: XmlFile) => Ok.stream(Enumerator.fromStream(new ByteArrayInputStream(xml.content.get.getBytes("UTF-8"))).andThen(Enumerator.eof))
      case Some(zip: ZipFile) => Ok.stream(Enumerator.fromStream(new ByteArrayInputStream(zip.content.get)).andThen(Enumerator.eof))
      case _ => {
        NotFound("File not found")
      }
    }
*/
  }

  def downloadFile(typ: String, id: String) = Action.async {
    fileService.loadById(typ, id) map {
      case Some(xml: XmlFile) => Ok.chunked(Enumerator.fromStream(new ByteArrayInputStream(xml.content.get.getBytes("UTF-8"))).andThen(Enumerator.eof)).withHeaders(("Content-Disposition", s"attachment; filename=${xml.name}.xml"))
      case Some(zip: ZipFile) => Ok.chunked(Enumerator.fromStream(new ByteArrayInputStream(zip.content.get)).andThen(Enumerator.eof)).withHeaders(("Content-Disposition", s"attachment; filename=${zip.name}.zip"))
      case _ => NotFound("")
    }
  }
}
