package model


import java.util.Date

import play.api.libs.json._

/**
  * Created by Joseph Sebastian on 16/06/2016.
  */
class JenkinsModel {

}

case class JenkinsJob(id: Option[String], name: String, buildNo: Int, failCount: Int, skipCount: Int, totalCount: Int, url: String, time: Long, result: String, var isRunning: Boolean = false) extends MongoEntity[String] {
}

object JenkinsJob {
  implicit val jenkinsJob = Json.format[JenkinsJob]

  implicit object JenkinsJobIdentity extends Identity[JenkinsJob, String] {
    override def entityName: String = "jenkins"

    override def next: String = ""

    override def of(entity: JenkinsJob): Option[String] = Some(entity.name)

    override def set(entity: JenkinsJob, id: String): JenkinsJob = if(!entity.id.isDefined) entity.copy(id = Some(genId(entity.name,entity.buildNo))) else entity

    override def clear(entity: JenkinsJob): JenkinsJob = ???
  }

  def empty = JenkinsJob(None, "", 0, 0, 0, 0, "", 0L, "")

  def genId(name:String, jobNo:Int) = s"$name-$jobNo"
}

case class JenkinsSummary(name:String, failCount:Int, passCount:Int, totalCount:Int, day:Date)

object JenkinsSummary {
  implicit val jenkinsSummary = Json.format[JenkinsSummary]
}