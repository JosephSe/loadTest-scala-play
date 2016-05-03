package actors

import java.text.SimpleDateFormat

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import model.{RawResponses, Request, ServerResponseData}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.xml.XML

trait RequestProcessor {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  def extract(request: Request) = {
    val requestXML = XML.loadString(request.payload)
    val priceRequest = requestXML \\ "Request" \\ "RequestDetails" \\ "SearchHotelPriceRequest"
    val city = (priceRequest \\ "ItemDestination" \\ "@DestinationCode").text
    val checkInDate = (priceRequest \\ "PeriodOfStay" \\ "CheckInDate").text
    val duration = (priceRequest \\ "PeriodOfStay" \\ "Duration").text
    val roomCode = (priceRequest \\ "Rooms" \\ "Room" \\ "@Code").text
    (city, dateFormat.parse(checkInDate), duration.toInt, roomCode)
  }
}

class RequestActor(legacyURL: String, novaURL: String, responseHandler: ActorRef) extends Actor with ActorLogging with RequestProcessor {

  private val legacyLoader = context.actorOf(Props(new RequestLoaderActor(legacyURL)))
  private val novaLoader = context.actorOf(Props(new RequestLoaderActor(novaURL)))
  implicit val timeout = Timeout(15 seconds)

  override def receive: Receive = {
    case request: Request => loadData(request)
  }

  private def loadData(request: Request): RawResponses = {
    val legacyFutureResponse = legacyLoader ? request
    val novaFutureResponse = novaLoader ? request
    val legacyResponse = Await.result(legacyFutureResponse, timeout.duration).asInstanceOf[ServerResponseData]
    val novaResponse = Await.result(novaFutureResponse, timeout.duration).asInstanceOf[ServerResponseData]
    val requestData = extract(request)
    /*
        val response = for{
          legacyResponse <- legacyFutureResponse
          novaResponse <- novaFutureResponse
        } yield (legacyLoader, novaLoader)
        Await.result(response , timeout.duration)
    */
    val response = RawResponses(legacyResponse, novaResponse, requestData._1, requestData._4, requestData._2, requestData._3)
    responseHandler ! response
    response
  }
}
