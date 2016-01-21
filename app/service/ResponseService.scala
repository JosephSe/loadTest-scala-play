package service

import javax.inject.Inject

import com.google.inject.ImplementedBy
import model.{ZipFile, XmlFile}
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
import scala.util.Success

import scala.concurrent._
import scala.concurrent.duration._

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */
@ImplementedBy(classOf[ResponseServiceImpl])
trait ResponseService {
  def all(filePrefix: String): (List[XmlFile], List[ZipFile])
}

class ResponseServiceImpl @Inject()(val xmlService: XmlService) extends ResponseService {

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  def xmlCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("xml")

  def zipCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("zip")

  override def all(filePrefix: String): (List[XmlFile], List[ZipFile]) = {
    val xmlFiles = xmlService.findByCriteria(Map("name" -> Json.obj("$regex" -> JsString(filePrefix))), 100)
    //    { name: {$regex: 'test1'}}
    //    val xmlFiles = xmlCollection.find(BSONDocument("name" -> BSONDocument("$regex" -> filePrefix))).cursor[XmlFile].collect[List]()
    //    val zipFiles = xmlCollection.find(BSONDocument("name" -> BSONDocument("$regex" -> filePrefix))).cursor[ZipFile].collect[List]()

    var xmlList: List[XmlFile] = List()
    xmlFiles.onComplete {
      //      case files:Success[List[XmlFile]]=> {
      case Success(files) => xmlList = files.asInstanceOf[List[XmlFile]]
      case _ => List()
    }
    Await.result(xmlFiles, 10 seconds)

    (xmlList, List())

  }
}
