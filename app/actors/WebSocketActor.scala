package actors

import akka.actor.{Props, ActorRef, Actor}
import akka.actor.Actor.Receive

/**
  * Created by Joseph Sebastian on 19/02/2016.
  */

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {
  override def receive = {
    case msg: String =>
      out ! ("Message recieved: " + msg)
  }
}
