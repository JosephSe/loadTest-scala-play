package serviceBroker

import javax.inject.Inject

import com.google.inject.ImplementedBy
import model.JiraTicket
import play.api.cache.CacheApi
import play.api.libs.json.{JsArray, JsValue}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@ImplementedBy(classOf[JiraBrokerImpl])
trait JiraBroker {

  def getTicket(key: String): Future[JiraTicket]

  def getTicketsFromFilter(filterId: Long): Future[List[JiraTicket]]
}

class JiraBrokerImpl @Inject()(ws: WSClient, conf: play.api.Configuration, cache: CacheApi) extends JiraBroker {
  private val url: String = "http://jira.emea.kuoni.int/rest/api/latest/issue/"
  val jobURL = conf.getString("jira.ticketURL").get
  val filterURL = conf.getString("jira.filterURL").get

  override def getTicket(key: String): Future[JiraTicket] = {
    ws.url(jobURL + s"$key").get().map { response =>
      getTicket(response.json)
    }
  }

  override def getTicketsFromFilter(filterId: Long): Future[List[JiraTicket]] = {
    ws.url(filterURL + s"$filterId").get().map { response =>
      val res = (response.json \ "issues").get
      res.asInstanceOf[JsArray].value.toList.map { ticket =>
        getTicket(ticket)
      }
    } map { tickets =>
      cache.set(s"filter.$filterId", tickets, 1.hours)
      tickets
    }
  }

  private def getTicket(jiraJson: JsValue): JiraTicket = {
    val key = (jiraJson \ "key" get).as[String]
    val summary = (jiraJson \ "fields" \ "summary").get.as[String]
    val priority = (jiraJson \ "fields" \ "priority" \ "name").get.as[String]
    val status = (jiraJson \ "fields" \ "status" \ "name").get.as[String]
    JiraTicket(key, summary, priority, status)
  }
}
