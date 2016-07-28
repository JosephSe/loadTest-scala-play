package model

import java.util.{Date, UUID}

import play.api.libs.json.Json
import reactivemongo.bson._

/**
  * Created by Joseph Sebastian on 19/01/2016.
  */
sealed trait MongoEntity {
  def name: String

  def uuid: Option[UUID]
}

case class XmlFile(uuid: Option[UUID], var name: String, content: Option[String], time: Option[Date] = Some(new Date)) extends MongoEntity {
  def this(name: String, content: String) = this(Some(UUID.randomUUID()), name, Some(content), Some(new Date()))

  def this(name: String) = this(None, name, None, None)

  def this(bson: BSONDocument) = {
    this(Some(UUID.fromString(bson.getAs[String]("uuid").getOrElse(UUID.randomUUID().toString))), bson.getAs[String]("name").get, bson.getAs[String]("content"), Some(new Date(bson.getAs[Long]("time").get)))
  }
}

object XmlFile {
  implicit val xmlFormat = Json.format[XmlFile]

  implicit object XmlFileIdentity extends Identity[XmlFile, UUID] {
    override def name: String = "xml"

    override def next: UUID = UUID.randomUUID()

    override def of(entity: XmlFile): Option[UUID] = entity.uuid

    override def set(entity: XmlFile, id: UUID): XmlFile = if (!entity.uuid.isDefined) entity.copy(uuid = Option(id)) else entity

    override def clear(entity: XmlFile): XmlFile = entity.copy(uuid = None)
  }
  def empty = XmlFile(None, "", None, None)
}

case class ZipFile(uuid: Option[UUID], var name: String, content: Option[Array[Byte]], time: Option[Date] = Some(new Date)) extends MongoEntity {
  def this(name: String, content: Array[Byte]) = this(Some(UUID.randomUUID()), name, Some(content), Some(new Date()))

  def this(name: String) = this(None, name, None, None)

  def this(bson: BSONDocument) = this(Some(UUID.fromString(bson.getAs[String]("uuid").getOrElse(UUID.randomUUID().toString))), bson.getAs[String]("name").get, bson.getAs[Array[Byte]]("content"), Some(new Date(bson.getAs[Long]("time").get)))
}

object ZipFile {
  implicit val zipFile = Json.format[ZipFile]

  implicit object ZipFileIdentity extends Identity[ZipFile, UUID] {
    override def name: String = "zip"

    override def next: UUID = UUID.randomUUID()

    override def of(entity: ZipFile): Option[UUID] = entity.uuid

    override def set(entity: ZipFile, id: UUID): ZipFile = if (!entity.uuid.isDefined) entity.copy(uuid = Option(id)) else entity

    override def clear(entity: ZipFile): ZipFile = entity.copy(uuid = None)
  }

  implicit object ZipFileReader extends BSONDocumentReader[ZipFile] {
    override def read(bson: BSONDocument): ZipFile = {
      val name = bson.getAs[String]("name").get
      ZipFile(None, name, null, null)
    }
  }
  def empty = ZipFile(None, "", None, None)

}
