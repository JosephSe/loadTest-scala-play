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

/**
  * Created by Joseph Sebastian on 19/02/2016.
  */
class WebsocketController @Inject() (messageBroadcaster: MessageBroadcaster) extends Controller {

  /** Central hub for distributing chat messages */
  val (messageOut, broadcastChannel) = Concurrent.broadcast[JsValue]

  def socket = WebSocket.using[String] { request =>
    // Log events to the console
    //    val in = Iteratee.foreach[String](println).map { _ =>
    //      println("Disconnected")
    //    }

    // Just consume and ignore the input
    val in = Iteratee.consume[String]()

    // Send a single 'Hello!' message
    val out = Enumerator("Hello!")

    (in, out)
  }

  def sender = Action {
    messageBroadcaster.broadcast(Json.parse("{\"test1\":2232}"));Ok
  }

  def broadcast = WebSocket.using[String] { request =>
    //Concurrent.broadcast returns (Enumerator, Concurrent.Channel)
    val (out, channel) = Concurrent.broadcast[String]
    val in = Iteratee.foreach[String] {
      msg => println(msg)
        channel push (s"Response:$msg")
    }
    (in, out)
  }

  def broadcast1 = Action {req =>
    val (out, channel) = Concurrent.broadcast[String]
    val in = Iteratee.foreach[String] {
      msg => println(msg)
        channel push (s"Response:$msg")
    }
    (in, out)
    Ok.chunked(out &> EventSource()).as("text/event-stream")
    //   Ok.chunked(messageBroadcaster.messageOut &> EventSource()).as("text/event-stream")
  }
  def broadcast3 = WebSocket.using[JsValue] { request =>
    //Concurrent.broadcast returns (Enumerator, Concurrent.Channel)
//    val (out, channel) = Concurrent.broadcast[JsValue]

    val in = Iteratee.foreach[JsValue] {
      msg => println(msg)
        messageBroadcaster broadcast msg
    }
    (in, messageBroadcaster.messageOut)
  }

  lazy val broadcastSockJS = SockJS.using[JsValue] { req =>
    val in = Iteratee.foreach[JsValue] {
      msg => println(msg)
        messageBroadcaster broadcast msg
    }
    (in, messageBroadcaster.messageOut)
  }

  // it must be a `val` or `lazy val` because you are instantiating a play Router and not a
  // classic request handler
  lazy val sockjs = SockJSRouter.using[String] { request =>

    // Log events to the console
    val in = Iteratee.foreach[String](println).map { _ =>
      println("Disconnected")
    }

    // Send a single 'Hello!' message and close
    val out = Enumerator("Hello SockJS!") >>> Enumerator.eof

    (in, out)
  }


}
