package service

import java.text.SimpleDateFormat
import java.util.Date

import util.Utils.StringUtils
import com.google.inject.{ImplementedBy, Inject}
import dao.{CRUDService, MongoCRUDService}
import model.JenkinsJob.genId
import model.{JenkinsJob, JenkinsSummary}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import play.api.cache.CacheApi
import play.api.libs.json.{JsNumber, JsString, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import serviceBroker.JenkinsBroker

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by Joseph Sebastian on 16/06/2016.
  */
@ImplementedBy(classOf[JenkinsServiceImpl])
trait JenkinsService extends CRUDService[JenkinsJob, String] {

  def getLatestDetails(name: String): Future[JenkinsJob]

  def getAllJobDetails: Future[List[JenkinsJob]]

  def allJobHistory(group: String): Future[Map[String, List[JenkinsJob]]]

  def loadJobHistory(name: String): Future[List[JenkinsJob]]

  def jobHistoryStatusCount(group: String): Future[Map[String, Int]]

  def latestJobStatus(group: String): Future[Map[String, String]]

  def loadToDB(group: String): Future[Int]

  def allJobSummary(group: String, clearText: String): Future[Map[String, Map[String, List[JenkinsSummary]]]]

  def allJobSummaryPerDay(group: String): Future[Map[String, Int]]

}

class JenkinsServiceImpl @Inject()(conf: play.api.Configuration, cache: CacheApi, jenkinsBroker: JenkinsBroker, reactiveMongo: ReactiveMongoApi) extends MongoCRUDService[JenkinsJob, String](reactiveMongo) with JenkinsService {
  //  val jobURL = conf.getString("jenkins.url").get + "/job/"
  lazy val sitJobs: List[String] = conf.getStringList("jenkins.sitJobs").get.toList
  lazy val prodJobs: List[String] = conf.getStringList("jenkins.prodJobs").get.toList
  val pattern = "ddMMMyyyy"
  val dateFormat = new SimpleDateFormat(pattern)
  val dtf = DateTimeFormat.forPattern(pattern)

  val logger = Logger("JenkinsServiceImpl").logger

  override def getLatestDetails(name: String): Future[JenkinsJob] = jenkinsBroker.getLatestDetails(name)

  override def getAllJobDetails: Future[List[JenkinsJob]] = {
    Future.sequence {
      sitJobs.map(getLatestDetails(_))
    }
  }

  override def allJobHistory(group: String): Future[Map[String, List[JenkinsJob]]] = {
    Future.sequence {
      getJobs(group).map(findByName(_, 100))
    } map { jobLst =>
      val jobMap = jobLst.flatten.foldLeft(Map[String, List[JenkinsJob]]())((r, c) => r + (c.name -> (r.getOrDefault(c.name, List[JenkinsJob]()) ::: List[JenkinsJob](c))))
      jobMap.foldLeft(Map[String, List[JenkinsJob]]())((r, c) => r ++ Map(c._1 -> c._2.sortWith((lt, rt) => lt.buildNo < rt.buildNo)))
    }
  }

  override def loadJobHistory(name: String): Future[List[JenkinsJob]] = {
    val cacheName = s"jenkins.history-$name"
    val jobHist = cache.get[List[JenkinsJob]](cacheName)
    if(jobHist.isDefined) Future {
      jobHist.get
    } else {
      findByName(name, Int.MaxValue).map { list =>
//      findByCriteria(Map("name" -> name), Int.MaxValue).map { list =>
        cache.set(cacheName, list, 3.hours)
        list
      }
      //    getJobsList
    }
  }

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

  override def latestJobStatus(group: String): Future[Map[String, String]] = {
    Future.sequence {
      getJobs(group).map {
        jenkinsBroker.getLatestDetails(_)
      }
    } map { jobList =>
      jobList.foldLeft(Map[String, String]())((r, c) => r + (c.name -> c.result))
    }
  }

  override def loadToDB(group: String): Future[Int] = {
    var sum = 0
    logger.info(s"Load request for group :$group")
    val total = getJobs(group).map { name =>
      val latest = for {
        jenLatest <- getLatestDetails(name)
        mongLatest <- findLatestBuild(name)
      } yield (jenLatest, mongLatest)
      val total = latest map { vals =>
        val start = vals._2 match {
          case Some(jenJob) => jenJob.buildNo
          case _ => 1
        }
        val end = vals._1.buildNo
        sum = sum + (end - start)
        for (i <- start to end) saveOrIgnore(name, i)
        sum
      }
      total
    }
    Future.sequence(total).map(_.sum)
  }

  private def saveOrIgnore(name: String, jobNo: Int) = {
    findByUUID(genId(name, jobNo)).map { entity =>
      if (!entity.isDefined) jenkinsBroker.getJob(name, jobNo.toString).filter(!_.isRunning).map { job =>
        val date = DateTime.now()
        if (date.isBefore(job.time)) {
          println("Wrong date found " + job.toString + ":" + new DateTime(job.time))
        }
        create(job)
      }
    }
  }

  private def findLatestBuild(name: String): Future[Option[JenkinsJob]] = {
    val crit = Map("name" -> JsString(name))
    val sort = Map("buildNo" -> JsNumber(-1))
    findByCriteriaAndSort(crit, sort, 1) map {
      case empty if empty.isEmpty => None
      case someVal => Some(someVal.head)
    }
  }

  override def allJobSummary(group: String, clearText: String): Future[Map[String, Map[String, List[JenkinsSummary]]]] = {
    val clearTxt = clearText.split(",")
    val jobSumm = cache.get[Map[String, Map[String, List[JenkinsSummary]]]](s"jenkins.summary-$group")
    //    val jobSumm = None
    if (jobSumm.isDefined) Future {
      jobSumm.get
    } else {
      val data = Future.sequence {
        getJobs((group)).map { name =>
          val newName = name.removeText(clearTxt)
          loadJobHistory(name).map { jobList =>
            groupByDate(newName, jobList).flatten
          }
        }
      } map { va =>
        va.flatten.toSeq.groupBy { j =>
          dateFormat.format(j.day)
        }.map(kv => (kv._1, kv._2.toList.groupBy(_.name))).toSeq
          .sortWith { (lft, rt) =>
            dtf.parseDateTime(lft._1).isAfter(dtf.parseDateTime(rt._1).getMillis)
          }.toMap
      }
      data.map {
        cache.set(s"jenkins.summary-$group", _, 3.hour)
      }
      data
    }
  }

  private def groupByDate(name: String, jobLst: List[JenkinsJob]): List[Option[JenkinsSummary]] = {
    jobLst.groupBy(job => dtf.print(job.time)) map { values =>
      val total = values._2.size
      val failCount = values._2.filter(_.result == "FAILURE").size
      try {
        val date = dtf.parseDateTime(values._1)
        if (date.isAfter(DateTime.now())) println(values)
        Some(JenkinsSummary(name, failCount, total - failCount, total, date.toDate))
      } catch {
        case e: Exception => println("JenkinsSummary failed for values : " + values)
          e.printStackTrace()
          None
      }
    } toList
  }

  // Test methods
  private def getJobsList: Future[List[JenkinsJob]] = {
    val jobLst = mutable.ListBuffer[JenkinsJob]()
    jobLst.add(new JenkinsJob(None, "FO", 1, 1, 0, 1, "", 1472544000000l, "FAILURE", false)) //Tue Aug 30 2016 09:00:00 GMT+0100
    jobLst.add(new JenkinsJob(None, "FO", 1, 1, 0, 1, "", 1472547600000l, "SUCCESS", false)) //Tue Aug 30 2016 10:00:00 GMT+0100
    jobLst.add(new JenkinsJob(None, "FO", 1, 1, 0, 1, "", 1472551200000l, "SUCCESS", false)) //Tue Aug 30 2016 11:00:00 GMT+0100
    jobLst.add(new JenkinsJob(None, "FO", 1, 1, 0, 1, "", 1472464800000l, "SUCCESS", false)) //Tue Aug 29 2016 11:00:00 GMT+0100
    jobLst.add(new JenkinsJob(None, "FO", 1, 1, 0, 1, "", 1472500800000l, "FAILURE", false)) //Tue Aug 29 2016 21:00:00 GMT+0100
    jobLst.add(new JenkinsJob(None, "FO", 1, 1, 0, 1, "", 1472421600000l, "FAILURE", false)) //Tue Aug 28 2016 23:00:00 GMT+0100
    jobLst.add(new JenkinsJob(None, "FO", 1, 1, 0, 1, "", 1467367200000l, "FAILURE", false)) //Tue Jul 07 2016 11:00:00 GMT+0100
    Future {
      jobLst.toList
    }
  }

  override def allJobSummaryPerDay(group: String): Future[Map[String, Int]] = {
    allJobSummary(group, "").map { jobs =>
      jobs.map { dayMap =>
        (dayMap._1, dayMap._2.values.flatten.foldLeft(0)((sum, summary) => sum + summary.failCount))
      }.toMap
    }
  }
}