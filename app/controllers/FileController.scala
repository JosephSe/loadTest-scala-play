package controllers

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */

import java.io.ByteArrayInputStream
import javax.inject.Inject

import model.{ZipFile, XmlFile, ResponseData}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.{FileService, XmlService, ZipService}
import util.Utils.StringUtils

import scala.concurrent.Future

class FileController @Inject()(fileService: FileService) extends Controller {
  //class FileController @Inject()(val xmlService: XmlService, val zipService: ZipService) extends Controller {


  def getFile(typ: String, id: String) = Action.async {
    fileService.findByNameAndId(typ, id) map { file =>
      file match {
        case Some(xml: XmlFile) => Ok(Json.toJson(new ResponseData(xml)))
        case Some(zip: ZipFile) => Ok(Json.toJson(new ResponseData(zip)))
        case _ => {
          NotFound("File not found")
        }
      }
    }
  }

  def downloadFileName(name:String) = Action.async {
    fileService.loadByName(name).map {
      case Some(xml: XmlFile) => Ok.stream(Enumerator.fromStream(new ByteArrayInputStream(xml.content.get.getBytes)).andThen(Enumerator.eof))
      case Some(zip: ZipFile) => Ok.stream(Enumerator.fromStream(new ByteArrayInputStream(zip.content.get)).andThen(Enumerator.eof))
      case _ => {
        NotFound("File not found")
      }
    }
  }

  def downloadFile(typ: String, id: String) = Action.async {
    fileService.loadById(typ, id) map {
      case Some(xml: XmlFile) => Ok.stream(Enumerator.fromStream(new ByteArrayInputStream(xml.content.get.getBytes)).andThen(Enumerator.eof)).withHeaders(("Content-Disposition", s"attachment; filename=${xml.name}.xml"))
      case Some(zip: ZipFile) => Ok.stream(Enumerator.fromStream(new ByteArrayInputStream(zip.content.get)).andThen(Enumerator.eof)).withHeaders(("Content-Disposition", s"attachment; filename=${zip.name}.zip"))
      case _ => NotFound("")
    }
  }

  /*
      def loadFile(name: String) = Action.async {
        name match {
          case xml if name.endsWith(".xml") => {
            fileService.findByName(name.removeDelimiter) map {
              case xml: ResponseData => val xmlContent = xml.content.get
                Ok.stream(Enumerator.fromStream(new ByteArrayInputStream(xmlContent.getBytes())).andThen(Enumerator.eof)) //.as("application/xml")
              case None => Future {PriceBreakdown2790TestNotFound("File not found")}
            }
          }
          case zip if name.endsWith(".zip") => {
            fileService.findByName(name.removeDelimiter) map {
              //            case zip:ResponseData => Ok.stream(Enumerator.fromStream(new ByteArrayInputStream(zip.content.get)).andThen(Enumerator.eof))
              case None => Future {NotFound("File not found")}
            }
          }
          case _ => Future {
            NotFound("File not found")
          }
        }
      }
  */
}
