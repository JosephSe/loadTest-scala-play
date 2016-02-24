package service

import java.util.UUID

import com.google.inject.{Inject, ImplementedBy}
import dao.{CRUDService, MongoCRUDService}
import model.{MongoEntity, ResponseData, XmlFile, ZipFile}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Promise, Future}

/**
  * Created by Joseph Sebastian on 21/01/2016.
  */
@ImplementedBy(classOf[XmlMongoService])
trait XmlService extends CRUDService[XmlFile, UUID]

class XmlMongoService extends MongoCRUDService[XmlFile, UUID] with XmlService {

  override def findByCriteriaAndFields(criteria: Map[String, Any], fields: List[String]): Future[List[XmlFile]] = {
    super.findByCritAndFields(criteria, fields).map(_.map(new XmlFile(_)))
  }
}

@ImplementedBy(classOf[ZipMongoService])
trait ZipService extends CRUDService[ZipFile, UUID]

class ZipMongoService extends MongoCRUDService[ZipFile, UUID] with ZipService {

  override def findByCriteriaAndFields(criteria: Map[String, Any], fields: List[String]): Future[List[ZipFile]] = {
    super.findByCritAndFields(criteria, fields).map(_.map(new ZipFile(_)))
  }

}

class FileService @Inject()(xmlService: XmlService, zipService: ZipService) {

  def findByNameAndId(name: String, id: String): Future[Option[MongoEntity]] = {
    name match {
      case xml if name.endsWith("xml") => xmlService.findByUUID(id)
      case zip if name.endsWith("zip") => zipService.findByUUID(id)
      case _ => Future {
        None
      }
    }
  }

  /**
    * Method to load file with xml content
    * @param typ
    * @param id
    * @return
    */
  def loadById(typ: String, id: String): Future[Option[MongoEntity]] = {
    typ match {
      case "xml" => xmlService.findByUUID(id)
      case "zip" => zipService.findByUUID(id)
      case _ => Future {
        None
      }
    }
  }
}