package controllers

import javax.inject.Inject

import play.api.libs.EventSource
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._
import play.sockjs.api._
import service.MessageBroadcaster
import scala.concurrent.Channel
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.{Set => MS}
import play.api._
import play.api.mvc._
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.collection.mutable.{Set => MS}
import scala.concurrent._

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
