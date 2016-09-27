package service

import javax.inject.Inject

import com.google.inject.ImplementedBy
import model.JiraTicket
import play.api.cache.CacheApi
import serviceBroker.JiraBroker

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by Joseph Sebastian on 21/09/2016.
  */
@ImplementedBy(classOf[JiraServiceImpl])
trait JiraService {
  def getTicketsFromFilter(filterId: Long): Future[List[JiraTicket]]
}

class JiraServiceImpl @Inject()(jiraBroker: JiraBroker, cache: CacheApi) extends JiraService {
  override def getTicketsFromFilter(filterId: Long): Future[List[JiraTicket]] = {
    val cacheName = s"jira.filter.$filterId"
    val list = cache.get[List[JiraTicket]](cacheName)
    if (list.isDefined) Future {
      list.get
    }
    else {
      jiraBroker.getTicketsFromFilter(filterId).map { va =>
        cache.set(cacheName, va, 1.hours)
        va
      }
    }

  }
}
