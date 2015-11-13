package model

/**
 * Created by Joseph Sebastian on 27/10/2015.
 */

case class Request(payload: String)

case class Response(legacy: ServerResponse, nova: ServerResponse)

case class ServerResponse(data: String, time: Long)

//abstract class Request {
//  def payload: String
//}

//case class LegacyRequest(payload: String) extends Request

//case class NovaRequest(payload: String) extends Request
