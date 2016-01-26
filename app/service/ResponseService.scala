package service

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
}

class ResponseServiceImpl @Inject()(val xmlService: XmlService, val zipService: ZipService) extends ResponseService {

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  override def all(filePrefix: String): Future[List[ResponseData]] = {
    val xmls = xmlService.findByCriteria(Map("name" -> Json.obj("$regex" -> JsString(filePrefix))), 100)
    val zips = zipService.findByCriteria(Map("name" -> Json.obj("$regex" -> JsString(filePrefix))), 100)
    val files = for {
      xmlFiles <- xmls
      zipFiles <- zips
    } yield (xmlFiles, zipFiles)
    files.map { responseFiles =>
      responseFiles._1.map(new ResponseData(_)) ++ responseFiles._2.map(new ResponseData(_))
    }
  }
}
