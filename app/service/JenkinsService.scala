package service

import javax.inject.Inject

import com.google.inject.ImplementedBy
import model.JenkinsJob
import play.api.libs.ws.WSClient
import play.api.Play.current
import play.api.cache.CacheApi
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsString}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

/**
  * Created by Joseph Sebastian on 16/06/2016.
  */
@ImplementedBy(classOf[JenkinsServiceImpl])
trait JenkinsService {
  def getLatestDetails(name: String): Future[JenkinsJob]

  def loadJobDetails(name: String, jobNumber: String): Future[JenkinsJob]

  def getAllJobDetails: Future[List[JenkinsJob]]
}

class JenkinsServiceImpl @Inject()(ws: WSClient, conf: play.api.Configuration, cache: CacheApi) extends JenkinsService {
  val jobURL = conf.getString("jenkins.url").get + "/job/"
  val jobs: List[String] = conf.getStringList("jenkins.jobs").get.toList

  override def getLatestDetails(name: String): Future[JenkinsJob] = {
    val res = ws.url(jobURL + s"$name/api/json").get().map { response =>
      val lastBulidNo = (response.json \ "lastCompletedBuild" \ "number").get.toString()
      loadJobDetails(name, lastBulidNo)
    }
    res.flatMap { response =>
      response
    }
  }

  override def loadJobDetails(name: String, jobNumber: String): Future[JenkinsJob] = {
    //    Future {
    cache.getOrElse[Future[JenkinsJob]](s"jenkins.$name-$jobNumber") {
      ws.url(s"$jobURL$name/$jobNumber/api/json").get().map { rawResponse =>
        val response = rawResponse.json
        val url = (response \ "url" get).as[JsString].value
        val time = (response \ "timestamp" get).as[JsNumber].value.toLong
        val actions = (response \ "actions").get
        val job = JenkinsJob(name, jobNumber.toInt, (actions \\ "failCount").asInstanceOf[ListBuffer[JsNumber]].get(0).value.toInt,
          (actions \\ "skipCount").asInstanceOf[ListBuffer[JsNumber]].get(0).value.toInt,
          (actions \\ "totalCount").asInstanceOf[ListBuffer[JsNumber]].get(0).value.toInt, url, time)
        cache.set(s"jenkins.$name-$jobNumber", job)
        job
      }
    }
  }

  override def getAllJobDetails: Future[List[JenkinsJob]] = {
    Future.sequence {
      jobs.map(getLatestDetails(_))
    }
  }

}