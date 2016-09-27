package serviceBroker

import java.lang.Boolean
import javax.inject.Inject

import com.google.inject.ImplementedBy
import model.JenkinsJob
import play.api.cache.CacheApi
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.libs.concurrent.Futures

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import JenkinsJob.genId
@ImplementedBy(classOf[JenkinsBrokerImpl])
trait JenkinsBroker {

  def getLatestDetails(name: String): Future[JenkinsJob]

  def isCurrentlyBuilding(name: String): Future[Boolean]

  def loadJobHistory(name: String): Future[List[JenkinsJob]]

  def getJob(name: String, jobNumber: String): Future[JenkinsJob]

}

class JenkinsBrokerImpl @Inject()(ws: WSClient, conf: play.api.Configuration, cache: CacheApi) extends JenkinsBroker {

  val jobURL = conf.getString("jenkins.url").get + "/job/"

  override def getLatestDetails(name: String): Future[JenkinsJob] = {
    ws.url(jobURL + s"$name/api/json").get().map { response =>
      val lastBulidNo = (response.json \ "lastCompletedBuild" \ "number").get.toString()
      loadJobDetails(name, lastBulidNo)
    } flatMap(response => response)
  }

  override def isCurrentlyBuilding(name: String): Future[Boolean] = {
    ws.url(s"$jobURL$name/lastBuild/api/json").get()
      .map(raw => new Boolean((raw.json \ "building").asInstanceOf[JsDefined].value.toString()))
  }

  override def loadJobHistory(name: String): Future[List[JenkinsJob]] = {
    import scala.concurrent.duration._
    val jobList = cache.get[List[JenkinsJob]](s"history.$name")
    if(jobList.isDefined) {
      Future {jobList.get}
    } else {
      ws.url(jobURL + s"$name/api/json").get().map {
        response =>
          Future.sequence {
            (response.json \ "builds").asInstanceOf[JsDefined].value.asInstanceOf[JsArray].value.toList.map {
              build =>
                getJob(name, (build \ "number").asInstanceOf[JsDefined].value.toString())
            }
          }
      } flatMap (job => job) map { jobs =>
        cache.set(s"history.$name", jobs, 5.minutes)
        jobs
      }
    }
  }

  private def loadJobDetails(name: String, jobNumber: String): Future[JenkinsJob] = {
    val job = cache.get[JenkinsJob](s"jenkins.$name-$jobNumber")
    if (job.isDefined) Future {
      job.get
    }
    else {
      val futureResponse = for {
        status <- isCurrentlyBuilding(name)
        job <- getJob(name, jobNumber)
      } yield (status, job)

      futureResponse map { value =>
        value._2.isRunning = value._1
        cache.set(s"jenkins.$name-$jobNumber", value._2)
        value._2
      }
    }
  }

  def getJob(name: String, jobNumber: String): Future[JenkinsJob] = {
    val job = cache.get[JenkinsJob](s"jenkins.$name-$jobNumber")
    //    if (false) Future {
    if (job.isDefined) Future {
      job.get
    }
    else ws.url(s"$jobURL$name/$jobNumber/api/json").get().map { rawResponse =>
      val response = rawResponse.json
      val url = (response \ "url" get).as[JsString].value
      val time = (response \ "timestamp" get).as[JsNumber].value.toLong
      val actions = (response \ "actions").get
      val fail = (actions \\ "failCount")
      val failCount = (actions \\ "failCount") match {
        case seq: Seq[JsValue] => if (seq.size == 0) 0
        else seq.get(0).toString().toInt
        case _ => 0
      }
      val skipCount = (actions \\ "skipCount") match {
        case seq: Seq[JsValue] => if (seq.size == 0) 0
        else seq.get(0).toString().toInt
        case _ => 0
      }
      val totalCount = (actions \\ "totalCount") match {
        case seq: Seq[JsValue] => if (seq.size == 0) 0
        else seq.get(0).toString().toInt
        case _ => 0
      }
      val building = (response \ "building") match {
        case seq: JsBoolean => seq.value
        case seq: JsDefined => seq.value.asInstanceOf[JsBoolean].value
        case _ => false
      }
      val result = (response \ "result") match {
        case value: JsValue => value.toString()
        case JsDefined(null) => println("nulll !!!!")
          "FAILURE"
        case defined: JsDefined => var resultResponse = "FAILURE"
          try {
            resultResponse = defined.get.asInstanceOf[JsString].value
          } catch {
            case _:Throwable =>
              if (building) resultResponse = "BUILDING"
          }
          resultResponse
        case seq: JsString => seq.value
        case _ => "unknown"
      }
      val job = JenkinsJob(Some(genId(name, jobNumber.toInt)), name, jobNumber.toInt, failCount, skipCount, totalCount, url, time, result, building)
      if (!building) cache.set(s"jenkins.$name-$jobNumber", job)
      job
    }
  }


}
