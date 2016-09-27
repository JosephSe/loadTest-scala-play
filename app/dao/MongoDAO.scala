package dao

import com.google.inject.Inject
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

trait CRUDService[E, ID] {
  def getCount(name: String): Future[Int]

  def findById(id: ID): Future[Option[E]]

  def findByUUID(id: String): Future[Option[E]]

  def findByName(name: String): Future[Option[E]]

  def findByName(name: String, count: Int): Future[List[E]]

  def findByNameRegEX(name: String): Future[List[BSONDocument]]

  def findByCriteria(criteria: Map[String, Any], limit: Int): Future[List[E]]

  def findByCriteriaAndSort(criteria: Map[String, Any], sort: Map[String, Any], limit: Int): Future[List[E]]

  def findAll: Future[List[E]]

  def findByCriteriaAndFields(criteria: Map[String, Any], fields: List[String]): Future[List[E]]

  def create(entity: E): Future[Either[String, ID]]

  def update(id: ID, entity: E): Future[Either[String, ID]]

  def delete(id: ID): Future[Either[String, ID]]
}

import model.Identity
import reactivemongo.api._

/**
  * Abstract {{CRUDService}} impl backed by JSONCollection
  */
abstract class MongoCRUDService[E: Format, ID: Format] @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit identity: Identity[E, ID])
  extends CRUDService[E, ID] {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import play.modules.reactivemongo.json._
  import play.modules.reactivemongo.json.collection.JSONCollection

  //  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  /** Mongo collection deserializable to [E] */
  def collection: JSONCollection = reactiveMongoApi.db.collection(identity.entityName)

  override def findById(id: ID): Future[Option[E]] = collection.find(Json.obj(identity.entityName -> id)).one[E]

  override def findByUUID(id: String): Future[Option[E]] = collection.find(Json.obj("id" -> id)).one[E]

  override def findByName(name: String): Future[Option[E]] = collection.find(Json.obj("name" -> name)).one[E]

  override def findByName(name: String, count: Int): Future[List[E]] = findByCriteria(Map("name" -> name), count)


  override def findByNameRegEX(name: String): Future[List[BSONDocument]] = collection.find(Json.obj("name" -> Json.obj("$regex" -> name)))
    .cursor[BSONDocument](readPreference = ReadPreference.primary)
    .collect[List]()

  override def findByCriteria(criteria: Map[String, Any], limit: Int): Future[List[E]] =
    findByCriteria(CriteriaJSONWriter.writes(criteria), limit)

  protected def findByCritAndFields(criteria: Map[String, Any], fields: List[String]): Future[List[BSONDocument]] = {
    val filter = JsObject(fields.map(_ -> JsNumber(1)).toSeq)
    collection.genericQueryBuilder.query(CriteriaJSONWriter.writes(criteria)).projection(filter)
      .sort(Json.obj("time" -> -1))
      .cursor[BSONDocument](readPreference = ReadPreference.primary)
      .collect[List](50)
  }

  def findByCriteriaAndSort(criteria: Map[String, Any], sort: Map[String, Any], limit: Int): Future[List[E]] = {
    collection.genericQueryBuilder.query(CriteriaJSONWriter.writes(criteria)).sort(CriteriaJSONWriter.writes(sort))
      .cursor[E](readPreference = ReadPreference.primary).collect[List](limit)
  }

  private def findByCriteria(criteria: JsObject, limit: Int): Future[List[E]] = {
    collection.
      find(criteria).
      sort(JsObject(Seq("time" -> JsNumber(-1)))).
      cursor[E](readPreference = ReadPreference.primary).
      collect[List](limit)
  }

  override def getCount(name: String): Future[Int] = {
    Future {
      1
    }
  }

  override def create(entity: E): Future[Either[String, ID]] = {
    val id = identity.next
    //    val doc = Json.toJson(entity).as[JsObject]
    val doc = Json.toJson(identity.set(entity, id)).as[JsObject]
    collection.insert(doc).map {
      case le if le.ok == true => Right(id)
      case le => Left(le.message)
    }
  }

  override def update(id: ID, entity: E): Future[Either[String, ID]] = {
    val doc = Json.toJson(identity.set(entity, id)).as[JsObject]
    collection.update(Json.obj(identity.entityName -> id), doc) map {
      case le if le.ok == true => Right(id)
      case le => Left(le.message)
    }
  }

  override def delete(id: ID): Future[Either[String, ID]] = {
    collection.remove(Json.obj(identity.entityName -> id)) map {
      case le if le.ok == true => Right(id)
      case le => Left(le.message)
    }
  }

  override def findAll: Future[List[E]] = {
    collection.find(Json.obj()).sort(Json.obj("buildNo" -> 1))
      .cursor[E](readPreference = ReadPreference.primary).collect[List]()
  }

  override def findByCriteriaAndFields(criteria: Map[String, Any], fields: List[String]): Future[List[E]] = ???

}

object CriteriaJSONWriter extends Writes[Map[String, Any]] {
  override def writes(criteria: Map[String, Any]): JsObject = JsObject(criteria.mapValues(toJsValue(_)).toSeq)

  val toJsValue: PartialFunction[Any, JsValue] = {
    case v: String => JsString(v)
    case v: Int => JsNumber(v)
    case v: Long => JsNumber(v)
    case v: Double => JsNumber(v)
    case v: Boolean => JsBoolean(v)
    case obj: JsValue => obj
    case map: Map[String, Any]@unchecked => CriteriaJSONWriter.writes(map)
    case coll: List[_] => JsArray(coll.map(toJsValue(_)).toSeq)
    case null => JsNull
    case other => throw new IllegalArgumentException(s"Criteria value type not supported: $other")
  }
}
