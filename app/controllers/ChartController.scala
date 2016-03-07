package controllers

import model.ChartData
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.mutable.ListBuffer
/**
 * Created by Joseph Sebastian on 18/11/2015.
 */
class ChartController extends Controller{

  def pie = Action {
    var responseList = ListBuffer[ChartData]()
    responseList += ChartData("Price", "100", "52.0", "8", "10", "3", "6", "12", "25")
    responseList += ChartData("Hotels", "10", "52.0", "8", "10", "3", "6", "12", "25")
    responseList += ChartData("Rooms", "20", "52.0", "8", "10", "3", "6", "12", "25")
    Ok(Json.toJson(responseList))
  }

  def charts = Action {
    Ok(views.html.charts(""))
  }

}
