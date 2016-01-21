package model

import java.util.{UUID, Date}

import play.api.libs.json.Json

/**
  * Created by Joseph Sebastian on 19/01/2016.
  */
case class XmlFile(uuid: Option[UUID], name: String, newFile: Boolean, content: String, time: Date = new Date)


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

case class ZipFile(name: String, newFile: Boolean, content: String, time: Date = new Date)

object ZipFile {
  implicit val zipFile = Json.format[ZipFile]
}
