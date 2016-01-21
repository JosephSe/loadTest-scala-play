package actors

import javax.inject.Inject

import akka.actor.{PoisonPill, ActorSystem, Props, Actor}
import akka.actor.Actor.Receive
import akka.routing.RoundRobinPool
import model.{RawResponses, ProcessedResponses, Response}
import play.api.Play._
import akka.actor._


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
      println(processedResponse)
      if (count == initCount) self ! PoisonPill
  }
}
