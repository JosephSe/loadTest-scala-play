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
  def all(filePrefix: String): (List[XmlFile], List[ZipFile])

  def allFuture(filePrefix: String): Future[List[ResponseData]]
}

class ResponseServiceImpl @Inject()(val xmlService: XmlService, val zipService: ZipService) extends ResponseService {

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  def xmlCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("xml")

  def zipCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("zip")

  override def all(filePrefix: String): (List[XmlFile], List[ZipFile]) = {
    val xmls = xmlService.findByCriteria(Map("name" -> Json.obj("$regex" -> JsString(filePrefix))), 100)
    val zips = zipService.findByCriteria(Map("name" -> Json.obj("$regex" -> JsString(filePrefix))), 100)
    val files = for {
      xmlFiles <- xmls
      zipFiles <- zips
    } yield (xmlFiles, zipFiles)
    Await.result(files, 10 seconds)

    var response: (List[XmlFile], List[ZipFile]) = (List(), List())
    files.onComplete {
      //      case _ => (xmlF, zips)
      case Success(responseFiles) => response = (responseFiles._1, responseFiles._2)
      case Failure(ex) => println("empty list")
    }
    response
  }

  override def allFuture(filePrefix: String): Future[List[ResponseData]] = {
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

  /*

    override def all(filePrefix: String): (List[XmlFile], List[ZipFile]) = {
      val xmlFiles = xmlService.findByCriteria(Map("name" -> Json.obj("$regex" -> JsString(filePrefix))), 100)
      //    { name: {$regex: 'test1'}}
      //    val xmlFiles = xmlCollection.find(BSONDocument("name" -> BSONDocument("$regex" -> filePrefix))).cursor[XmlFile].collect[List]()
      //    val zipFiles = xmlCollection.find(BSONDocument("name" -> BSONDocument("$regex" -> filePrefix))).cursor[ZipFile].collect[List]()

      var xmlList: List[XmlFile] = List()
      Await.result(xmlFiles, 10 seconds)
      xmlFiles.onComplete {
        //      case files:Success[List[XmlFile]]=> {
        case Success(files) => xmlList = files.asInstanceOf[List[XmlFile]]
        case _ => List()
      }
      Await.result(xmlFiles, 10 seconds)

      (xmlList, List())
    }
  */
}
