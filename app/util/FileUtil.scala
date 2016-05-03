package util

import java.io.InputStream
import javax.inject.Singleton

import model.LoadTestData

import scala.io.Source

/**
 * Created by Joseph Sebastian on 17/11/2015.
 */
object FileUtil {

  def loadFile(file:InputStream) = {
    LoadTestData(Source.fromInputStream(file).getLines())
  }

  def writeToFile() = {

  }

}
