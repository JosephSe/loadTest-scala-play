package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import model.{LoadTestData, Request}
import play.api.Play._

import scala.concurrent.duration._

/**
 * Created by Joseph Sebastian on 27/11/2015.
 */

trait LoadTestProcessor {
  implicit val timeout = Timeout(5 seconds)

  def process(loadTestData: LoadTestData, reqActor: ActorRef) = {
    loadTestData.contentList.foreach { payload =>
      val request = Request(payload)
      reqActor ! request
    }
  }
}

class LoadTestRunnerActor extends Actor  with  ActorLogging with LoadTestProcessor {
  val threadCount = current.configuration.getInt("actor.threadCount").get
  val legacyURL = current.configuration.getString("loadtesting.legacy").get
  val novaURL = current.configuration.getString("loadtesting.nova").get
  val inputFile = current.configuration.getString("loadtesting.inputFile").get

  val responseHandler = context.actorOf(Props(new ResponseHandlerActor(threadCount)))

  val requestActor = context.actorOf(RoundRobinPool(threadCount).props(Props(new RequestActor(legacyURL, novaURL, responseHandler))), "requestActor")

  override def receive = {
    case load: LoadTestData => process(load, requestActor)
  }
}
