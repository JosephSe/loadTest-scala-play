package service

import java.util.UUID
import javax.inject.Inject

import com.google.inject.ImplementedBy
import model.{ResponseData, XmlFile, ZipFile}
import play.api.Play.current
import play.api.libs.json.{JsString, Json}
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */
@ImplementedBy(classOf[ResponseServiceImpl])
trait ResponseService {
  def all(filePrefix: String): Future[Map[String, List[ResponseData]]]

  def saveXml(xmlFile: XmlFile): Future[Either[String, UUID]]

  def saveZip(zipFile: ZipFile): Future[Either[String, UUID]]
}

class ResponseServiceImpl @Inject()(val xmlService: XmlService, val zipService: ZipService) extends ResponseService {

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  override def all(filePrefix: String): Future[Map[String, List[ResponseData]]] = {
    val xmls = xmlService.findByCriteriaAndFields(Map("name" -> Json.obj("$regex" -> JsString(filePrefix))), List("name", "uuid", "time", "newFile"))
    val zips = zipService.findByCriteriaAndFields(Map("name" -> Json.obj("$regex" -> JsString(filePrefix))), List("name", "uuid", "time", "newFile"))
    val files = for {
      xmlFiles <- xmls
      zipFiles <- zips
    } yield (xmlFiles, zipFiles)
    files.map { responseFiles =>
      Map("xml" -> responseFiles._1.map(new ResponseData(_)), "zip" -> responseFiles._2.map(new ResponseData(_)))
    }
  }

  override def saveXml(xmlFile: XmlFile): Future[Either[String, UUID]] = {
    xmlService.findByName(xmlFile.name).map { file =>
//      file match {
//        case Some(x)
//      }
//
    }
    xmlService.create(xmlFile)
  }

  override def saveZip(zipFile: ZipFile): Future[Either[String, UUID]] = {
    zipService.create(zipFile)
  }
}
