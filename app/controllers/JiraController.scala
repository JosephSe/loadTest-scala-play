package controllers

import javax.inject.Inject

import play.api.cache.Cached
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.JiraService

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by Joseph Sebastian on 21/09/2016.
  */
class JiraController @Inject() (jiraService: JiraService, cached: Cached) extends Controller {

  def dataComparisonDashboard = cached("dataComparisonDashboard") {
    Action {
      Ok(views.html.load(""))
    }
  }

  def ticketsFromFilter(filterId:Long) = Action.async{
    jiraService.getTicketsFromFilter(filterId).map { tickets =>
      Ok(Json.toJson(tickets))
    }
  }

}
