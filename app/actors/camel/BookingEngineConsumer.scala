package actors.camel

import akka.actor.Actor.Receive
import akka.actor.ActorLogging
import akka.camel.{CamelMessage, Consumer}

/**
 * Created by Joseph Sebastian on 22/12/2015.
 */
class BookingEngineConsumer extends Consumer with ActorLogging {
  override def endpointUri: String = "rabbitmq://lonskib01c.emea.kuoni.int:15672/booking-engine-queue-sit?autoDelete=false&username=kuoni&password=Travel123"

  override def receive: Receive = {
    case msg:CamelMessage =>
      println(msg)
    case _ => println("Unknown message recieved")
  }
}
