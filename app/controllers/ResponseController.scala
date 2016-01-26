package controllers

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.ResponseService

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */
class ResponseController @Inject()(val responseService: ResponseService) extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def all(namePrefix: String = "") = Action.async {
    responseService.all(namePrefix).map { responseDataList =>
      Ok(Json.toJson(responseDataList))
    }
  }
}
