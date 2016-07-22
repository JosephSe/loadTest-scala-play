package service

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

/**
  * Created by Joseph Sebastian on 16/06/2016.
  */
@ImplementedBy(classOf[JenkinsServiceImpl])
trait JenkinsService {
  def getLatestDetails(name: String): Future[JenkinsJob]

  def loadJobDetails(name: String, jobNumber: String): Future[JenkinsJob]

  def getAllJobDetails: Future[List[JenkinsJob]]

  def isCurrentlyBuilding(name: String): Future[Boolean]

  def loadJobHistory(name: String): Future[List[JenkinsJob]]

  def allJobHistory(group: String): Future[Map[String, List[JenkinsJob]]]

}

class JenkinsServiceImpl @Inject()(ws: WSClient, conf: play.api.Configuration, cache: CacheApi) extends JenkinsService {
  val jobURL = conf.getString("jenkins.url").get + "/job/"
  val sitJobs: List[String] = conf.getStringList("jenkins.sitJobs").get.toList
  val prodJobs: List[String] = conf.getStringList("jenkins.prodJobs").get.toList

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
    //    cache.getOrElse[Future[JenkinsJob]](s"jenkins.$name-$jobNumber") {
    val futureResponse = for {
      status <- isCurrentlyBuilding(name)
      job <- getJob(name, jobNumber)
    } yield (status, job)

    futureResponse map { value =>
      value._2.isRunning = value._1
      cache.set(s"jenkins.$name-$jobNumber", value._2)
      value._2
    }
    //    }
  }

  private def getJob(name: String, jobNumber: String): Future[JenkinsJob] = {
    cache.getOrElse(s"jenkins.$name-$jobNumber") {
      ws.url(s"$jobURL$name/$jobNumber/api/json").get().map { rawResponse =>
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
        val result = (response \ "result") match {
          case value: JsValue => value.toString()
          case defined: JsDefined => var resultResponse = "null"
            try {
              resultResponse = defined.get.asInstanceOf[JsString].value
            } catch {
              case _ => println(s"Fail !!!! $defined")
            }
            resultResponse
          case seq: JsString => seq.value
          case _ => "unknown"
        }
        //        val result = (response \ "result").asInstanceOf[JsDefined].value.asInstanceOf[JsString].value
        val job = JenkinsJob(name, jobNumber.toInt, failCount, skipCount, totalCount, url, time, result)
        cache.set(s"jenkins.$name-$jobNumber", job)
        job
      }
    }
  }


  override def getAllJobDetails: Future[List[JenkinsJob]] = {
    Future.sequence {
      sitJobs.map(getLatestDetails(_))
    }
  }

  override def isCurrentlyBuilding(name: String): Future[Boolean] = {
    ws.url(s"$jobURL$name/lastBuild/api/json").get().map { rawResponse =>
      new Boolean((rawResponse.json \ "building").asInstanceOf[JsDefined].value.toString())
    }
  }

  override def loadJobHistory(name: String): Future[List[JenkinsJob]] = {
    ws.url(jobURL + s"$name/api/json").get().map { response =>
      Future.sequence {
        (response.json \ "builds").asInstanceOf[JsDefined].value.asInstanceOf[JsArray].value.toList.map { build =>
          getJob(name, (build \ "number").asInstanceOf[JsDefined].value.toString())
        }
        //      } flatMap { m =>
        //                Future {m.foldLeft(List[JenkinsJob]())((lst, job) => lst :: List[JenkinsJob](job))}
        //          m.sortWith((lt, rt) => lt.buildNo < rt.buildNo)
        //        }
      }
    } flatMap { job =>
      job
    }
  }

  override def allJobHistory(group: String): Future[Map[String, List[JenkinsJob]]] = {
    val jobMap: Map[String, List[JenkinsJob]] = Map()
    val jobs = group match {
      case "prod" => prodJobs
      case _ => sitJobs
    }
    Future.sequence {
      jobs.map(loadJobHistory(_))
    } map { jobLst =>
      val jobMap = jobLst.flatten.foldLeft(Map[String, List[JenkinsJob]]())((r, c) => r + (c.name -> (r.getOrDefault(c.name, List[JenkinsJob]()) ::: List[JenkinsJob](c))))
      jobMap.foldLeft(Map[String, List[JenkinsJob]]())((r, c) => r ++ Map(c._1 -> c._2.sortWith((lt, rt) => lt.buildNo < rt.buildNo)))

    }
  }
}