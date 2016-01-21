package model

/**
 * Created by Joseph Sebastian on 18/11/2015.
 */
import play.api.libs.json._

case class ChartData(typ:String, bothSame:String, bothSamePct:String, nova:String, novaPct:String, legacy:String, legacyPct:String,bothZeroHotels:String, bothZeroHotelsPct:String)
//implicit val chartData = Json.format[ChartD]
object ChartData {
  implicit val chartData = Json.format[ChartData]
}
