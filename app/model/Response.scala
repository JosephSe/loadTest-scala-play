package model

import java.util.{Date, UUID}

import util.Utils.OptionDateUtils

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */
//case class ResponseData(uuid: String, name:Option[String]) {
case class ResponseData(uuid: Option[UUID], name: String, var newFile: Boolean, var content: Option[String], time: Option[String], typ: String, date: Option[Date] = Some(new Date)) {
  def this(xmlFile: XmlFile) = this(xmlFile.uuid, xmlFile.name, false, xmlFile.content, xmlFile.time.format, "xml", xmlFile.time)

  def this(zipFile: ZipFile) = this(zipFile.uuid, zipFile.name, false, None, zipFile.time.format, "zip", zipFile.time)

  def withNew = {
    newFile = true; this
  }

  def withoutData = {
    content = None; this
  }
}

object ResponseData {

  import play.api.libs.json._

  implicit val responseDataFormat = Json.format[ResponseData]

}
