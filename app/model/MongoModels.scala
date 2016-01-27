package model

import java.util.{UUID, Date}

import play.api.libs.json.Json
import reactivemongo.bson.{BSONDocumentReader, BSONDocument}
import util.CustomBSONHandlers

/**
  * Created by Joseph Sebastian on 19/01/2016.
  */
trait MongoEntity {
  def uuid: Option[UUID]

  //  def of(entity: T): Option[UUID]
  //  def set(entity: T, id: UUID): T
  //  def clear(entity: T): T
}

case class XmlFile(uuid: Option[UUID], name: String, newFile: Boolean, content: Option[String], time: Option[Date] = Some(new Date)) {
  def this(name: String, content: String) = this(Some(UUID.randomUUID()), name, true, Some(content), Some(new Date()))

  def this(name: String) = this(None, name, false, None, None)

  def this(bson: BSONDocument) = {
    this(Some(UUID.fromString(bson.getAs[String]("uuid").getOrElse(UUID.randomUUID().toString))), bson.getAs[String]("name").get, bson.getAs[Boolean]("newFile").getOrElse(false), bson.getAs[String]("content"), bson.getAs[Date]("time"))
  }
}

//  extends MongoEntity


object XmlFile {
  implicit val xmlFormat = Json.format[XmlFile]

  implicit object XmlFileIdentity extends Identity[XmlFile, UUID] {
    override def name: String = "xml"

    override def next: UUID = UUID.randomUUID()

    override def of(entity: XmlFile): Option[UUID] = entity.uuid

    override def set(entity: XmlFile, id: UUID): XmlFile = entity.copy(uuid = Option(id))

    override def clear(entity: XmlFile): XmlFile = entity.copy(uuid = None)
  }

  implicit object XmlFileReader extends BSONDocumentReader[XmlFile] {
    override def read(bson: BSONDocument): XmlFile = {
      val name = bson.getAs[String]("name").get
      XmlFile(None, name, false, null, null)
    }
  }

}

case class ZipFile(uuid: Option[UUID], name: String, newFile: Boolean, content: String, time: Date = new Date) extends MongoEntity {
  def this(name: String, content: String) = this(Some(UUID.randomUUID()), name, true, content, new Date())

  def this(name: String) = this(None, name, false, null, null)

  def this(bson: BSONDocument) = this(None, bson.getAs[String]("name").get, false, null, null)
}

object ZipFile {
  implicit val zipFile = Json.format[ZipFile]

  implicit object ZipFileIdentity extends Identity[ZipFile, UUID] {
    override def name: String = "zip"

    override def next: UUID = UUID.randomUUID()

    override def of(entity: ZipFile): Option[UUID] = entity.uuid

    override def set(entity: ZipFile, id: UUID): ZipFile = entity.copy(uuid = Option(id))

    override def clear(entity: ZipFile): ZipFile = entity.copy(uuid = None)
  }

  implicit object ZipFileReader extends BSONDocumentReader[ZipFile] {
    override def read(bson: BSONDocument): ZipFile = {
      val name = bson.getAs[String]("name").get
      ZipFile(None, name, false, null, null)
    }
  }

}
