package util

import java.text.SimpleDateFormat
import java.util.Date

/**
  * Created by Joseph Sebastian on 26/01/2016.
  */
object Utils {


  implicit class DateUtils(date: Date) {

    val dateFormat = new SimpleDateFormat("dd-MM-yy hh:mm:ss")

    def format: Option[String] = {
      if (date == null) None
      else Some(dateFormat.format(date))
    }
  }

  implicit class OptionDateUtils(date: Option[Date]) {

    val dateFormat = new SimpleDateFormat("dd-MM-yy hh:mm:ss")

    def format: Option[String] = {
      if (date isEmpty) None
      else Some(dateFormat.format(date.get))
    }
  }

  implicit class StringUtils(str: String) {
    def removeDelimiter: String = {
      str.substring(0, str.lastIndexOf("."))
    }

    def removeText(strArray:Array[String]) = {
      strArray.foldLeft(str)((r, c) => r.replace(c, ""))
    }
    def removeTextCSV(str:String) = {
      val strArray = str.split(",")
      strArray.foldLeft(str)((r, c) => r.replace(c, ""))
    }
  }

}