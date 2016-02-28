package actors

import _root_.dispatch.url
import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import dispatch.{Http, as, url}
import model.{RawSearchResponse, ServerResponseData, Request}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by Joseph Sebastian on 28/10/2015.
 */
class RequestLoaderActor(endpoint: String) extends Actor with ActorLogging {
//  import play.api.libs.ws._
  import scala.concurrent.Future

  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val timeout = Timeout(10 seconds)

  override def receive: Receive = {
    case request: Request =>
//    case request: Request => sender ! loadData(request)
  }

/*
  private def loadData(req: Request) = {
    import play.api.Play.current
    val start = System.currentTimeMillis()
    val serResp = WS.url(endpoint).post(req.payload)
//    val futureResposne = WS.url(endpoint).post(req.payload)
    //    val request = Http(url(endpoint).setBody(req.payload).POST OK as.String)
    var response:ServerResponseData = new RawSearchResponse("", 0, 0)
    try {
      val resp = Await.result(serResp, timeout.duration)
      val end = System.currentTimeMillis()
      response = new RawSearchResponse(resp.body, end - start, resp.status)
    } catch {
      case e:Exception => response
    }
    response
  }
*/

//  private def loadData(req: Request) = {
//    val request = Http(url(endpoint).setBody(req.payload).POST OK as.String)
//    val start = System.currentTimeMillis()
//    var response:ServerResponseData = new RawSearchResponse("", 0, 0)
//    try {
//      val resp = Await.result(request, timeout.duration)
//      val end = System.currentTimeMillis()
//      response = new ServerResponseData(resp, end - start)
//    } catch {
//      case e:Exception => response
//    }
//    response
//  }
}
