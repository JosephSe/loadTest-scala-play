package serviceBroker

import javax.inject.Inject

import com.google.inject.ImplementedBy
import play.api.cache.CacheApi
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by Joseph Sebastian on 12/09/2016.
  */
@ImplementedBy(classOf[ESServiceBrokerImpl])
trait ESServiceBroker {

  val server = "http://awsiels01a4:9200/"
  val index = "logstash-nova-prod-2016.09.11"

  def getErrorSumm(date: String)

}

class ESServiceBrokerImpl @Inject() (ws: WSClient, conf: play.api.Configuration, cache: CacheApi) extends ESServiceBroker {

  override def getErrorSumm(date: String): Unit = {
    val query = "{\"size\":0,\"query\":{\"match\":{\"LogLevel\":\"ERROR\"}},\"aggs\":{\"group_by_class\":{\"terms\":{\"field\":\"Class.raw\"}}}}"
    val host = s"$server$index/_search"
    println(s"HOST >>>> $host")
    ws.url(host).post(query).map { response =>
      val total = response.json \ "hits" \ "total" get
      val buckets = response.json \ "aggregations" \ "group_by_class" \ "buckets"
    }

  }
}
