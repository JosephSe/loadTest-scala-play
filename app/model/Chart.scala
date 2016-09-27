package model

/**
  * Created by Joseph Sebastian on 18/11/2015.
  */

import play.api.libs.json._

case class ChartData(typ: String, bothSame: String, bothSamePct: String, nova: String, novaPct: String, legacy: String, legacyPct: String)

case class DataComparisonData(dataList:ChartData)

object ChartData {
  implicit val chartData = Json.format[ChartData]

  def apply(fileRow:String) = {
    val content = fileRow.split(";")
    new ChartData(content(0), content(1), content(2), content(3), content(4), content(5), content(6))
  }


}
