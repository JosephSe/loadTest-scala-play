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
    //      Future {Json.toJson(jenkinsService.getLatestDetails(name))}
    jenkinsService.getLatestDetails(name).map { job =>
      Ok(Json.toJson(job))
    }
  }

  def allJobDetails = Action.async {
    jenkinsService.getAllJobDetails.map { job =>
      Ok(Json.toJson(job))
    }
  }
}
