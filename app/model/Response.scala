package model

import java.util.Date

import play.api.libs.json.Json

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */
case class ResponseData(name: String, newFile: Boolean, content: String, time: Date = new Date, typ: String) {
  def this(xmlFile: XmlFile) = this(xmlFile.name, xmlFile.newFile, xmlFile.content, xmlFile.time, "xml")

  def this(zipFile: ZipFile) = this(zipFile.name, zipFile.newFile, zipFile.content, zipFile.time, "zip")
}

object ResponseData {
  implicit val responseDataFormat = Json.format[ResponseData]
}
