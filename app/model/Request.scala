package model

import java.util.Date

/**
 * Created by Joseph Sebastian on 27/10/2015.
 */

case class Request(payload: String)

abstract class Response[T <: ServerResponseData] {
  def legacy: T

  def nova: T

  def city: String

  def checkInDate: Date

  def duration: Int

  def roomCode: String
}

case class RawResponses(legacy: ServerResponseData, nova: ServerResponseData, city: String, roomCode: String, checkInDate: Date, duration: Int) extends Response[ServerResponseData]

case class ProcessedResponses(legacy: HotelSearchResponse, nova: HotelSearchResponse, city: String, roomCode: String, checkInDate: Date, duration: Int) extends Response[HotelSearchResponse]

abstract class ServerResponseData {
  def data: String
  def time: Long
  def responseCode: Int
}

case class RawSearchResponse(data: String, time: Long, responseCode: Int) extends ServerResponseData
case class HotelSearchResponse(hotelCount: Int, data: String, time: Long, responseCode: Int) extends ServerResponseData
