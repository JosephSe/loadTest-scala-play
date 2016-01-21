package actors

import java.util.Date

import akka.actor.{Actor, ActorRef}
import model._

import scala.xml.{Elem, XML}

/**
 * Created by Joseph Sebastian on 19/11/2015.
 */
trait ResponseProcessor {
  def process(response: RawResponses, sender: ActorRef)
}

trait HotelResponseProcessor extends ResponseProcessor {
  override def process(response: RawResponses, sender: ActorRef) = {
    //    val legacy = XML.loadString(response.legacy.data)
    //    val novaXML = XML.loadString(response.nova.data)

    def getHotelCount(response: Elem) = {
      getMaxPriceAndR(response)
      response \ "ResponseDetails" \ "SearchHotelPriceResponse" \ "HotelDetails" \ "Hotel" size
    }
    def getMaxPriceAndR(response:Elem) = {
      var maxRoomCatId = ""
      var maxPrice = Double.MinValue
      response \ "ResponseDetails" \ "SearchHotelPriceResponse" \ "HotelDetails" \ "Hotel" \ "RoomCategories" \ "RoomCategory" foreach { roomCategoty =>
        val price = (roomCategoty \ "ItemPrice" text).toDouble
        if(price > maxPrice) {
          maxRoomCatId = (roomCategoty \ "@Id").text
          maxPrice = price
        }
      }
      println(maxRoomCatId, maxPrice)

      (maxRoomCatId, maxPrice)
    }
    def hotelSearchResponse(response:ServerResponseData) = {
      val xml = XML.loadString(response.data)
      HotelSearchResponse(getHotelCount(xml), "", response.time, response.responseCode)
    }
    sender ! ProcessedResponses(hotelSearchResponse(response.legacy), hotelSearchResponse(response.nova), response.city, response.roomCode, response.checkInDate, response.duration)
  }
}

class HotelResponseProcessorActor extends Actor with HotelResponseProcessor {
  override def receive: Receive = {
    case response: RawResponses =>
      process(response, sender())
  }

}
