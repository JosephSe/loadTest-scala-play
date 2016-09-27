package controllers

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.DataComparisonService

/**
  * Created by Joseph Sebastian on 22/09/2016.
  */
class DataComparisonController @Inject() (dataComparisonService: DataComparisonService) extends Controller {

  def getChartData = Action {
    Ok(Json.toJson(dataComparisonService.getChartData))
  }


}
