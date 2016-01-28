package controllers

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */

import java.io.ByteArrayInputStream
import javax.inject.Inject

import model.{ResponseData, XmlFile}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Results, Action, Controller}
import play.modules.reactivemongo.{ReactiveMongoApi, _}
import play.modules.reactivemongo.json.collection.JSONCollection
import play.mvc.Http.Response
import reactivemongo.bson.BSONDocument
import service.{ZipService, XmlService}

import scala.concurrent.Future
import play.api.libs.json.Json
import reactivemongo.play.json._
import util.Utils.StringUtils

class FileController @Inject()(val xmlService: XmlService, val zipService: ZipService) extends Controller {


  def getFile(name: String) = Action.async {
    name match {
      case xml if name.endsWith(".xml") => {
        xmlService.findByName(name.removeDelimiter) map {
          case Some(xml) => Ok(Json.toJson(new ResponseData(xml)))
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
