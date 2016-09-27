package scheduler

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import play.api.Configuration

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class LoadFromJenkinsScheduler @Inject()(val system: ActorSystem, @Named("scheduler-actor") val schedulerActor: ActorRef, configuration: Configuration)(implicit ec: ExecutionContext) {
  val frequency = configuration.getInt("jenkins.loadFrequency").get
  val actor = system.scheduler.schedule(5.minutes, frequency.minutes, schedulerActor, "update")
}
