package actors

import akka.actor.{Actor, Props}
import akka.actor.Actor.Receive
import akka.routing.RoundRobinPool
import com.google.inject.Inject
import model.JenkinsJob
import serviceBroker.JenkinsBroker
import play.api.Play._
import scala.concurrent.ExecutionContext.Implicits.global._
case class LoadJob(name: String, no: Int)

/**
  * Created by Joseph Sebastian on 16/06/2016.
  */
class JenkinsActor @Inject()(jenkinsBroker: JenkinsBroker) extends Actor {
  val threadCount = current.configuration.getInt("jenkins.threadCount").get

  val jobLoaderActor = context.actorOf(RoundRobinPool(threadCount).props(Props(new JenkinsJobLoaderActor(jenkinsBroker))), "jenkinsJobLoader")
  override def receive: Receive = {
    case loadJob: LoadJob => jobLoaderActor ! loadJob
    case job: JenkinsJob =>
  }
}

class JenkinsJobLoaderActor (jenkinsBroker: JenkinsBroker) extends Actor {
  override def receive: Receive = {
    case loadJob: LoadJob => sender ! load(loadJob)
  }

  def load(loadJob: LoadJob) = {
//    jenkinsBroker.getJob(loadJob.name, loadJob.no.toString) map (sender ! _)
  }
}
