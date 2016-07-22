package model


import play.api.libs.json._
/**
  * Created by Joseph Sebastian on 16/06/2016.
  */
class JenkinsModel {

}

case class JenkinsJob(name:String, buildNo:Int, failCount:Int, skipCount:Int, totalCount:Int, url:String, time:Long, result:String, var isRunning:Boolean = false)

object JenkinsJob {
  implicit val jenkinsJob = Json.format[JenkinsJob]
}
