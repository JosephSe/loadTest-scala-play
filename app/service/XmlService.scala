package service

import java.util.UUID
import javax.inject.Inject

import com.google.inject.ImplementedBy
import dao.{MongoCRUDService, CRUDService}
import model.XmlFile
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB

/**
  * Created by Joseph Sebastian on 21/01/2016.
  */
@ImplementedBy(classOf[XmlMongoService])
trait XmlService extends CRUDService[XmlFile, UUID]
class XmlMongoService extends MongoCRUDService[XmlFile, UUID] with XmlService {
//class XmlMongoService @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends MongoCRUDService[XmlFile, UUID] with XmlService {
  /** Mongo collection deserializable to [E] */
//  override def collection: JSONCollection = reactiveMongoApi.db.collection("xml")
}
