package service

import java.util.UUID
import javax.inject.Inject

import com.google.inject.ImplementedBy
import model.{ResponseData, ZipFile, XmlFile}
import play.api.libs.Collections
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.collections.GenericCollection
import reactivemongo.api.{DB, FailoverStrategy, Collection}
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future
import play.api.Play.current
import reactivemongo.play.json._
import play.api.libs.json.{JsObject, JsString, Json}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

import scala.concurrent._
import scala.concurrent.duration._

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */
@ImplementedBy(classOf[ResponseServiceImpl])
trait ResponseService {
  def all(filePrefix: String): Future[List[ResponseData]]

  def saveXml(xmlFile: XmlFile): Future[Either[String, UUID]]

  def saveZip(zipFile: ZipFile): Future[Either[String, UUID]]
}

class ResponseServiceImpl @Inject()(val xmlService: XmlService, val zipService: ZipService) extends ResponseService {

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  override def all(filePrefix: String): Future[List[ResponseData]] = {
    val xmls = xmlService.findByCriteriaAndFields(Map("name" -> Json.obj("$regex" -> JsString(filePrefix))), List("name", "uuid", "time", "newFile"))
    val zips = zipService.findByCriteriaAndFields(Map("name" -> Json.obj("$regex" -> JsString(filePrefix))), List("name", "uuid", "time", "newFile"))
    val files = for {
      xmlFiles <- xmls
      zipFiles <- zips
    } yield (xmlFiles, zipFiles)
    files.map { responseFiles =>
      responseFiles._1.map(new ResponseData(_)) ++ responseFiles._2.map(new ResponseData(_))
    }
  }

  override def saveXml(xmlFile: XmlFile): Future[Either[String, UUID]] = {
    xmlService.create(xmlFile).map { response =>
      response
    }
  }

  override def saveZip(zipFile: ZipFile): Future[Either[String, UUID]] = {
    zipService.create(zipFile)
  }
}
