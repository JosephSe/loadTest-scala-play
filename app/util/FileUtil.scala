package util

import java.io.{File, InputStream}
import java.nio.file.{Files, Paths}
import javax.inject.Inject

import model.{ChartData, LoadTestData}

import scala.io.Source

/**
 * Created by Joseph Sebastian on 17/11/2015.
 */
import scala.collection.JavaConversions._

class FileUtil @Inject() (environment: play.api.Environment, configuration: play.api.Configuration) {

  def loadFile(file:InputStream) = {
    LoadTestData(Source.fromInputStream(file).getLines())
  }

  def writeToFile() = {

  }

  def getChartData(): List[ChartData]= {
//    new File(classOf[FileUtil].getClassLoader.getResource("../test.file").getFile)
    println(environment.resource("DataComparisonChartData.csv").isDefined)
    val pMatch = configuration.getString("chart.price.matching").get
    val pLegacyGt = configuration.getString("chart.price.legacyGt").get
    val pNovaGt = configuration.getString("chart.price.novaGt").get
    val hMatch = configuration.getString("chart.hotels.matching").get
    val hLegacyGt = configuration.getString("chart.hotels.legacyGt").get
    val hNovaGt = configuration.getString("chart.hotels.novaGt").get
//    Files.readAllLines(Paths.get(environment.resource("resources/DataComparisonChartData.csv").get.getPath)).toList.map { data =>
//      ChartData(data)
//    }
    List(new ChartData("Price", "", pMatch, "", pLegacyGt, "", pNovaGt), new ChartData("Hotels", "", hMatch, "", hLegacyGt, "", hNovaGt))
  }

}
