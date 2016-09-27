package controllers

import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.ESService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by Joseph Sebastian on 12/09/2016.
  */
class ESController @Inject() (eSService: ESService) extends Controller {

  def summary(date:String) = Action.async {
    eSService.summary(date)
    Future {
      Ok(Json.toJson(Map("processed" -> 10)))
    }
  }
}
