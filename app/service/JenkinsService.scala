package service

import javax.inject.Inject

import com.google.inject.ImplementedBy
import model.JenkinsJob
import play.api.cache.CacheApi
import serviceBroker.JenkinsBroker

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Joseph Sebastian on 16/06/2016.
  */
@ImplementedBy(classOf[JenkinsServiceImpl])
trait JenkinsService {
  def getLatestDetails(name: String): Future[JenkinsJob]

  def getAllJobDetails: Future[List[JenkinsJob]]

  def allJobHistory(group: String): Future[Map[String, List[JenkinsJob]]]

  def loadJobHistory(name: String): Future[List[JenkinsJob]]

  def jobHistoryStatusCount(group:String):Future[Map[String, Int]]

}

class JenkinsServiceImpl @Inject()(conf: play.api.Configuration, cache: CacheApi, jenkinsBroker: JenkinsBroker) extends JenkinsService {
//  val jobURL = conf.getString("jenkins.url").get + "/job/"
  lazy val sitJobs: List[String] = conf.getStringList("jenkins.sitJobs").get.toList
  lazy val prodJobs: List[String] = conf.getStringList("jenkins.prodJobs").get.toList

  override def getLatestDetails(name: String): Future[JenkinsJob] = jenkinsBroker.getLatestDetails(name)

  override def getAllJobDetails: Future[List[JenkinsJob]] = {
    Future.sequence {
      sitJobs.map(getLatestDetails(_))
    }
  }

  override def allJobHistory(group: String): Future[Map[String, List[JenkinsJob]]] = {
    Future.sequence {
      getJobs(group).map(jenkinsBroker.loadJobHistory(_))
    } map {
      jobLst =>
        val jobMap = jobLst.flatten.foldLeft(Map[String, List[JenkinsJob]]())((r, c) => r + (c.name -> (r.getOrDefault(c.name, List[JenkinsJob]()) ::: List[JenkinsJob](c))))
        jobMap.foldLeft(Map[String, List[JenkinsJob]]())((r, c) => r ++ Map(c._1 -> c._2.sortWith((lt, rt) => lt.buildNo < rt.buildNo)))
    }
  }

override def loadJobHistory(name: String): Future[List[JenkinsJob]] = jenkinsBroker.loadJobHistory(name)

  override def jobHistoryStatusCount(group: String): Future[Map[String, Int]] = {
    allJobHistory(group) map { jobMap =>
      jobMap.map { job =>
        (job._1, job._2.count(_.result == "SUCCESS"))
      }
    }
  }

  private def getJobs(group: String): List[String] = group match {
    case "prod" => prodJobs
    case _ => sitJobs
  }
}