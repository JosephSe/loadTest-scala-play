package service

import java.util.UUID

import com.google.inject.{Inject, ImplementedBy}
import dao.{CRUDService, MongoCRUDService}
import model.{MongoEntity, ResponseData, XmlFile, ZipFile}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

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
    *
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

  /**
    *
    * @param name
    * @return
    */
  def loadByName(name: String): Future[Option[MongoEntity]] = {
    if (name.contains(".")) {
      val n = name.substring(0, name.indexOf("."))
      name match {
        case xml if xml.endsWith(".xml") => xmlService.findByName(n)
        case zip if zip.endsWith(".zip") => zipService.findByName(n)
        case _ => Future {
          None
        }
      }
    } else Future {
      None
    }
  }

  def create(entity: MongoEntity): Future[Either[String, UUID]] = {
    val service = entity match {
      case xml: XmlFile => xmlService
      case zip: ZipFile => zipService
    }
    val newName = s"${entity.name}-"
    service.findByNameRegEX(newName).flatMap {
      case response: List[AnyRef] => {
        val fullName = newName + (response.size + 1)
        entity match {
          case xml: XmlFile => xmlService.create(new XmlFile(fullName, xml))
          case zip: ZipFile => zipService.create(new ZipFile(fullName, zip))
        }
      }
      case _ => Future {
        Left("Error saving")
      }
    }
  }

}