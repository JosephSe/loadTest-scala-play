package module

import actors.SchedulerActor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import scheduler.LoadFromJenkinsScheduler

/**
  * Created by Joseph Sebastian on 07/09/2016.
  */
class JobModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindActor[SchedulerActor]("scheduler-actor")
    bind(classOf[LoadFromJenkinsScheduler]).asEagerSingleton()
  }
}
