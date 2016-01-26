package controllers

import java.util.Date
import javax.inject.Inject

import model.{ResponseData, Response}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import service.ResponseService

import scala.concurrent.Future

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */
class ResponseController @Inject()(val responseService: ResponseService) extends Controller {

  import scala.concurrent.ExecutionContext.Implicits.global

  def all(namePrefix: String = "") = Action.async {
    val result = responseService.allFuture(namePrefix)
    result.map { responseDataList =>
      Ok(Json.toJson(responseDataList))
    }
  }

  /*
    def all(namePrefix:String="") = Action.async {
      val (xmls, zips) = responseService.all(namePrefix)
      val responseList = xmls.map(new ResponseData(_)) ++ zips.map(new ResponseData(_))

  //    Ok(responseService.all(namePrefix))
      scala.concurrent.Future {
  //      Ok()
  //      Ok(Json.toJson(ResponseData("te",true, "empty", new Date(), "xml")))
        Ok(Json.toJson(responseList))
      }
    }

  */
}
