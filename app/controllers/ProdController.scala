package controllers

import play.api.mvc.{Action, Controller}

/**
  * Created by Joseph Sebastian on 21/07/2016.
  */
class ProdController extends Controller {

  def prodView = Action {
    Ok(views.html.prod("") )
  }

}
