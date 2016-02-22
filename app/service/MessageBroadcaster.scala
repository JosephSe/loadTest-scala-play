package service

import com.google.inject.ImplementedBy
import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.libs.json.JsValue

import scala.concurrent.Channel

/**
  * Created by Joseph Sebastian on 19/02/2016.
  */
@ImplementedBy(classOf[MessageBroadcasterImpl])
trait MessageBroadcaster {

  def broadcast(message:JsValue)

  val messageOut:Enumerator[JsValue]
  val broadcastChannel:Concurrent.Channel[JsValue]
//  def messageOut:Enumerator[JsValue]
//  def broadcastChannel:Concurrent.Channel[JsValue]
}

class MessageBroadcasterImpl extends MessageBroadcaster {
   val channels = Concurrent.broadcast[JsValue]
//  val (messageOt, broadcastChan) = Concurrent.broadcast[JsValue]

  override def broadcast(message: JsValue): Unit = {
    broadcastChannel push message
  }

//  override def messageOut: Enumerator[JsValue] = messageOt
//  override def broadcastChannel: Concurrent.Channel[JsValue] = broadcastChan
  override val messageOut: Enumerator[JsValue] = channels._1
  override val broadcastChannel: Concurrent.Channel[JsValue] = channels._2
}
