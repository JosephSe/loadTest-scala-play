package controllers

import javax.inject.Inject

import play.api.cache.Cached
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.JenkinsService

//import views.html.jen._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Joseph Sebastian on 15/07/2016.
  */
class JenkinsController @Inject()(jenkinsService: JenkinsService, cached: Cached) extends Controller {

  def jenkins = cached("jenkinsHomePage") {
    Action {
      Ok(views.html.jenkins(""))
    }
  }

  def messages = Action {
    Ok(views.html.jen.messages())
  }

  def jobDetails(name: String) = Action.async {
    jenkinsService.getLatestDetails(name).map { job =>
      Ok(Json.toJson(job))
    }
  }

  def allJobDetails = Action.async {
    jenkinsService.getAllJobDetails.map { job =>
      Ok(Json.toJson(job))
    }
  }

  def jobHistory(name:String) = Action.async {
    jenkinsService.loadJobHistory(name).map { jobs =>
      Ok(Json.toJson(jobs))
    }
  }
  def allJobHistory(group:String) = Action.async {
    jenkinsService.allJobHistory(group).map { job =>
      Ok(Json.toJson(job))
    }
  }
  def jobHistoryStatusCount(group:String, clearText:String) = Action.async {
    import util.Utils.StringUtils
    jenkinsService.jobHistoryStatusCount(group).map { job =>
      val j = job.map(jobDetail => (jobDetail._1.removeText(clearText.split(",")), jobDetail._2))
      Ok(Json.toJson(j))
    }
  }

}
