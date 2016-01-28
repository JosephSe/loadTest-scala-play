package dao

import play.api.Play._
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

trait CRUDService[E, ID] {
  def findById(id: ID): Future[Option[E]]

  def findByName(name: String): Future[Option[E]]

  def findByCriteria(criteria: Map[String, Any], limit: Int): Future[List[E]]

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
abstract class MongoCRUDService[E: Format, ID: Format](implicit identity: Identity[E, ID])
  extends CRUDService[E, ID] {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import play.modules.reactivemongo.json._
  import play.modules.reactivemongo.json.collection.JSONCollection

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  /** Mongo collection deserializable to [E] */
  def collection: JSONCollection = reactiveMongoApi.db.collection(identity.name)

  override def findById(id: ID): Future[Option[E]] = collection.
    find(Json.obj(identity.name -> id)).
    one[E]


  override def findByName(name: String): Future[Option[E]] = collection.find(Json.obj("name" -> name)).one[E]

  override def findByCriteria(criteria: Map[String, Any], limit: Int): Future[List[E]] =
    findByCriteria(CriteriaJSONWriter.writes(criteria), limit)

  protected def findByCritAndFields(criteria: Map[String, Any], fields: List[String]): Future[List[BSONDocument]] = {
    val filter = JsObject(fields.map(_ -> JsNumber(1)).toSeq)
    collection.genericQueryBuilder.query(CriteriaJSONWriter.writes(criteria)).projection(filter)
      .cursor[BSONDocument](readPreference = ReadPreference.primary)
      .collect[List]()
  }

  private def findByCriteria(criteria: JsObject, limit: Int): Future[List[E]] = {
    //    val filter = BSONDocument(
    //      "uuid" -> 1,
    //      "name" -> 1,
    //      "time" -> 1)
    //
    collection.
      find(criteria).
      sort(JsObject(Seq("time" -> JsNumber(-1)))).
      cursor[E](readPreference = ReadPreference.primary).
      collect[List](limit)
  }

  override def create(entity: E): Future[Either[String, ID]] = {
    findByCriteria(Json.toJson(identity.clear(entity)).as[JsObject], 1).flatMap {
      case t if t.size > 0 =>
        Future.successful(Right(identity.of(t.head).get)) // let's be idempotent
      case _ => {
        val id = identity.next
        val doc = Json.toJson(identity.set(entity, id)).as[JsObject]
        collection.
          insert(doc).
          map {
            case le if le.ok == true => Right(id)
            case le => Left(le.message)
          }
      }
    }
  }

  override def update(id: ID, entity: E): Future[Either[String, ID]] = {
    val doc = Json.toJson(identity.set(entity, id)).as[JsObject]
    collection.update(Json.obj(identity.name -> id), doc) map {
      case le if le.ok == true => Right(id)
      case le => Left(le.message)
    }
  }

  override def delete(id: ID): Future[Either[String, ID]] = {
    collection.remove(Json.obj(identity.name -> id)) map {
      case le if le.ok == true => Right(id)
      case le => Left(le.message)
    }
  }
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