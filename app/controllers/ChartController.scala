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
    responseList += new ChartData("Price", "100", "52.0", "8", "10", "3", "6")
    responseList +=new  ChartData("Hotels", "10", "52.0", "8", "10", "3", "6")
    responseList += new ChartData("Rooms", "20", "52.0", "8", "10", "3", "6")
    Ok(Json.toJson(responseList))
  }

  def charts = Action {
    Ok(views.html.charts(""))
  }

}
