package actors

import javax.inject.{Inject, Singleton}

import akka.actor.Actor
import com.google.inject.ImplementedBy
import service.JenkinsService

import scala.concurrent.ExecutionContext

sealed trait ScheduleJob

case class LoadToDBJenkinsJob() extends ScheduleJob
/**
  * Created by Joseph Sebastian on 07/09/2016.
  */
@Singleton
class SchedulerActor @Inject() (implicit ec:ExecutionContext, jenkinsService: JenkinsService) extends Actor {

  override def receive: Receive = {
    case _ => jenkinsService.loadToDB("prod") map (count => println(s"$count job details loaded to DB"))

  }
}
