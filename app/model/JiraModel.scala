package model

import play.api.libs.json.Json

/**
  * Created by Joseph Sebastian on 21/09/2016.
  */
case class JiraTicket(key:String, summary:String, priority:String, status:String)

object JiraTicket {
  implicit val jiraTicket = Json.format[JiraTicket]
}

class JiraModel {

}
