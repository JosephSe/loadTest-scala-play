package actors

import akka.actor.{Actor, PoisonPill, Props, _}
import akka.routing.RoundRobinPool
import model.{ProcessedResponses, RawResponses}
import play.api.Play._
import play.api.Logger._

/**
 * Created by Joseph Sebastian on 17/11/2015.
 */
class ResponseHandlerActor(count:Int) extends Actor {
  var initCount = 0

  val threadCount = current.configuration.getInt("actor.threadCount").get

  val processorActor = context.actorOf(RoundRobinPool(threadCount).props(Props(new HotelResponseProcessorActor)), "responseProcessorActor")

  override def receive = {
    case response: RawResponses =>
      processorActor ! response
    case processedResponse: ProcessedResponses =>
      initCount += 1
      debug(processedResponse.toString)
      if (count == initCount) self ! PoisonPill
  }
}
