package service

import com.google.inject.ImplementedBy
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue

import scala.collection.mutable.Set

/**
  * Created by Joseph Sebastian on 19/02/2016.
  */
@ImplementedBy(classOf[MessageBroadcasterImpl])
trait MessageBroadcaster {
  def broadcast(message: JsValue)

  def register(channelID: Int, outChannel: Concurrent.Channel[JsValue])
}

class MessageBroadcasterImpl extends MessageBroadcaster {
  val channels = Concurrent.broadcast[JsValue]

  override def broadcast(message: JsValue): Unit = {
    MessageBroadcasterImpl.c.foreach(_._2.push(message))
  }

  override def register(channelID: Int, outChannel: Concurrent.Channel[JsValue]): Unit = {
    MessageBroadcasterImpl.c.add((channelID, outChannel))
  }
}

object MessageBroadcasterImpl {
  val c: Set[(Int, Concurrent.Channel[JsValue])] = Set() // (channelID, Channel))
}
