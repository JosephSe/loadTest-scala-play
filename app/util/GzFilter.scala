package util

import javax.inject.Inject

import play.api.http.{DefaultHttpRequestHandler, HttpFilters}
import play.api.mvc.EssentialFilter
import play.filters.csrf.CSRFFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Joseph Sebastian on 13/05/2016.
  */
//class GzFilter @Inject() ()(gzip: GzipFilter, csrfFilter: CSRFFilter) extends HttpFilters {
//  override def filters: Seq[EssentialFilter] = Seq(gzip, csrfFilter)
//}

import play.api.Logger
import play.api.mvc._

import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter

class MyFilters @Inject() (gzipFilter: GzipFilter) extends HttpFilters {
  def filters = Seq(gzipFilter)
}


import javax.inject.Inject
import akka.stream.Materializer
import play.api.Logger
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

class LoggingFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    val startTime = System.currentTimeMillis

    nextFilter(requestHeader).map { result =>

      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime

      Logger.info(s"${requestHeader.method} ${requestHeader.uri} took ${requestTime}ms and returned ${result.header.status}")

      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }
}


import javax.inject.Inject
import play.api.http._
import play.api.mvc._
import play.api.routing.Router

class SimpleHttpRequestHandlerOld @Inject() (router: Router) extends HttpRequestHandler {
  def handlerForRequest(request: RequestHeader) = {
    router.routes.lift(request) match {
      case Some(handler) => (request, handler)
      case None => (request, Action(Results.NotFound))
    }
  }
}