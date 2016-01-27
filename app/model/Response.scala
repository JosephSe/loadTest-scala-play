package model

import java.util.{UUID, Date}

import play.api.libs.json.Json
import util.DateUtils.{DateUtils, OptionDateUtils}

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */
case class ResponseData(uuid: Option[UUID], name: String, newFile: Boolean, content: Option[String], time: Option[String], typ: String, date: Option[Date] = Some(new Date)) {
  def this(xmlFile: XmlFile) = this(xmlFile.uuid, xmlFile.name, xmlFile.newFile, xmlFile.content, xmlFile.time.format, "xml", xmlFile.time)

  def this(zipFile: ZipFile) = this(zipFile.uuid, zipFile.name, zipFile.newFile, Some(zipFile.content), zipFile.time.format, "zip", Some(zipFile.time))
}

object ResponseData {
  implicit val responseDataFormat = Json.format[ResponseData]
}
