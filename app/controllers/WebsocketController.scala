package controllers

import javax.inject.Inject

import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import service.MessageBroadcaster

import scala.collection.mutable.{Set => MS}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Joseph Sebastian on 19/02/2016.
  */
class WebsocketController @Inject()(messageBroadcaster: MessageBroadcaster) extends Controller {

  /** Central hub for distributing chat messages */

  def sender = Action {
    Ok
  }

  def broadcast = WebSocket.using[JsValue] { _ =>
    val (out, channel) = Concurrent.broadcast[JsValue]
    val channelID = scala.util.Random.nextInt
//    c.add((channelID, channel))
    messageBroadcaster.register(channelID, channel)
    val in = Iteratee.foreach[JsValue] {
      _ match {
        case any => channel.push(Json.parse("thanks")) // push to current channel
      }
    }
//    }.map { _ => c.retain(x => x._1 != channelID) }
    (in, out)
  }

}
