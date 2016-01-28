package service

import java.util.UUID

import com.google.inject.ImplementedBy
import dao.{CRUDService, MongoCRUDService}
import model.{XmlFile, ZipFile}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
