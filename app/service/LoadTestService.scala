package service

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}

import actors.{LoadTestRunnerActor, ResponseHandlerActor, RequestActor}
import akka.actor.{Props, ActorSystem}
import akka.routing.RoundRobinPool
import model.Request
import play.api.Play
import play.api.Play._
import akka.pattern.ask
import akka.util.Timeout
import play.api.Play.current
import util.FileUtil
import scala.concurrent.duration._

/**
 * Created by Joseph Sebastian on 13/11/2015.
 */
@Singleton
class LoadTestService @Inject()(fileLoader: FileUtil) {

  implicit val timeout = Timeout(5 seconds)

  import scala.concurrent.ExecutionContext.Implicits.global

  val system = ActorSystem.create("LoadTestApplication")
//  val legacyURL = current.configuration.getString("loadtesting.legacy").get
//  val novaURL = current.configuration.getString("loadtesting.nova").get
  val inputFile = current.configuration.getString("loadtesting.inputFile").get
//  val threadCount = current.configuration.getInt("actor.threadCount").get
//  val responseHandler = system.actorOf(Props(new ResponseHandlerActor))

//  val requestActor = system.actorOf(RoundRobinPool(threadCount).props(Props(new RequestActor(legacyURL, novaURL, responseHandler))), "requestActor")

  val loadTestRunner = system.actorOf(Props[LoadTestRunnerActor])

  def run = {
    val path = Play.resource(inputFile).get
    val fileStream = Play.resourceAsStream(inputFile).get
    val rows = fileLoader.loadFile(Play.resourceAsStream(inputFile).get)
    loadTestRunner ! rows
    s"${rows.contentList.size} processed"
  }
}
