package controllers

import javax.inject.Inject

import play.api.mvc._
import service.LoadTestService

/**
 * Created by Joseph Sebastian on 13/11/2015.
 */
class LoadTestController extends  Controller {
//class LoadTestController @Inject() (loadTestService: LoadTestService) extends  Controller {

  def run = Action {
//    Ok(loadTestService.run)
    Ok
  }

}
