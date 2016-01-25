package model

import java.util.{UUID, Date}

import play.api.libs.json.Json

/**
  * Created by Joseph Sebastian on 19/01/2016.
  */
trait MongoEntity {
  def uuid: Option[UUID]

//  def of(entity: T): Option[UUID]
//  def set(entity: T, id: UUID): T
//  def clear(entity: T): T
}

case class XmlFile(uuid: Option[UUID], name: String, newFile: Boolean, content: String, time: Date = new Date)
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

}

case class ZipFile(uuid: Option[UUID], name: String, newFile: Boolean, content: String, time: Date = new Date) extends MongoEntity

object ZipFile {
  implicit val zipFile = Json.format[ZipFile]

  implicit object ZipFileIdentity extends Identity[ZipFile, UUID] {
    override def name: String = "zip"

    override def next: UUID = UUID.randomUUID()

    override def of(entity: ZipFile): Option[UUID] = entity.uuid
    override def set(entity: ZipFile, id: UUID): ZipFile = entity.copy(uuid = Option(id))
    override def clear(entity: ZipFile): ZipFile = entity.copy(uuid = None)
  }

}
