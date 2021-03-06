package util

/**
  * Created by Joseph Sebastian on 06/06/2016.
  */
class RequestHandler {

}


import javax.inject.Inject
import play.api.http._
import play.api.mvc._
import play.api.routing.Router

class SimpleHttpRequestHandler @Inject() (router: Router) extends HttpRequestHandler {
  def handlerForRequest(request: RequestHeader) = {
    router.routes.lift(request) match {
      case Some(handler) =>
        (request, handler)
      case None => (request, Action(Results.NotFound))
    }
  }
}