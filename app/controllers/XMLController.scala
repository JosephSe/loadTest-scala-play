package controllers

/**
  * Created by Joseph Sebastian on 20/01/2016.
  */

import java.util.UUID
import javax.inject.Inject

import akka.actor.FSM.Failure
import akka.actor.Status.Success
import model.XmlFile
import play.api.libs.json.Json
import play.api.mvc.{Results, Action, Controller}
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import play.mvc.Http.Response
import reactivemongo.bson.BSONDocument

class XMLController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents {

  import reactivemongo.play.json._
  import play.api.libs.json.Json

  def xmlCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("xml")

  def createXml = Action.async {
    val xml = XmlFile(Some(UUID.randomUUID()), "test1", true, Some("<content>test</content>"))
    val futureResult = xmlCollection.insert(xml)
    futureResult.map(_ => Ok)
  }

  def getXML(name: String) = Action.async {
    val xmlFile = xmlCollection.find(BSONDocument("name" -> name)).one[XmlFile]
    xmlFile.map {
      case Some(xml) => Ok(Json.toJson(xml))
      case None => NotFound
    }
  }

}
