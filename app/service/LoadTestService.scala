package service

import javax.inject.{Inject, Singleton}

import actors.LoadTestRunnerActor
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import play.api.Play
import play.api.Play.current
import util.FileUtil

import scala.concurrent.duration._

/**
 * Created by Joseph Sebastian on 13/11/2015.
 */
@Singleton
class LoadTestService @Inject() (fileUtil: FileUtil) {

  implicit val timeout = Timeout(5 seconds)

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
    val rows = fileUtil.loadFile(Play.resourceAsStream(inputFile).get)
    loadTestRunner ! rows
    s"${rows.contentList.size} processed"
  }
}
