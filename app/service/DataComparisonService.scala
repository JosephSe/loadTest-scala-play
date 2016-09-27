package service

import javax.inject.Inject

import com.google.inject.ImplementedBy
import model.ChartData
import play.api.cache.CacheApi
import util.FileUtil
import scala.concurrent.duration._
/**
  * Created by Joseph Sebastian on 22/09/2016.
  */
@ImplementedBy(classOf[DataComparisonServiceImpl])
trait DataComparisonService {
  def getChartData:List[ChartData]
}

class DataComparisonServiceImpl @Inject() (fileUtil: FileUtil, cache: CacheApi) extends DataComparisonService {
  val chartData = List(new ChartData("Price", "17586", "69.79", "5658", "14.63", "3594", "15.57"),
    new ChartData("Hotels", "6814", "94.43", "71", "4.6", "27", "0.97"),
    new ChartData("Rooms", "6814", "94.43", "71", "4.6", "27", "0.97"))

  val cacheName = "chart.pie"

  override def getChartData: List[ChartData] = {
    cache.getOrElse[List[ChartData]](cacheName, 5.hours) (fileUtil.getChartData())
  }
}
