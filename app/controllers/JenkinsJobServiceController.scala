package controllers

import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by Joseph Sebastian on 16/06/2016.
  */
class JenkinsJobServiceController extends Controller {

  def getLatestDetails(name: String) = Action.async {

    Future {
      Ok("done")
    }
  }

}
