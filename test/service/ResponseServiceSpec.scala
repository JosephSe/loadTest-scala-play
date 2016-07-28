package service

import com.typesafe.config.Config
import model.{JenkinsJob, XmlFile, ZipFile}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers, ParallelTestExecution}
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.libs.json.{JsString, Json}
import serviceBroker.JenkinsBroker

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//@RunWith(classOf[JUnitSuiteRunner])
class ResponseServiceSpec extends FlatSpec with Matchers with MockFactory with ScalaFutures with ParallelTestExecution {

  val xmlServStub = stub[XmlService]
  val zipServStub = stub[ZipService]
  val fileServStub = new FileService(xmlServStub, zipServStub)
  val underTest = new ResponseServiceImpl(xmlServStub, zipServStub, fileServStub)
  val xmlList = Future {List(XmlFile.empty)}
  val zipList = Future {List(ZipFile.empty)}
  val map = Map("name" -> Json.obj("$regex" -> JsString("")))
  val list = List("name", "uuid", "time", "newFile")


  "An all call with file prefix" should "return a not null object" in {
    (xmlServStub.findByCriteriaAndFields _) when (map, list) returns (xmlList)
    (zipServStub.findByCriteriaAndFields _) when (map, list) returns (zipList)

    val job = underTest.all("")
    assert(job != null, "Response is null")
    (xmlServStub.findByCriteriaAndFields _) verify (map, list)
    (zipServStub.findByCriteriaAndFields _) verify (map, list)
  }
  it should "return map with both xml and zip keys" in {
    (xmlServStub.findByCriteriaAndFields _) when (map, list) returns (xmlList)
    (zipServStub.findByCriteriaAndFields _) when (map, list) returns (zipList)

    val response = underTest.all("")
    whenReady(response) { j =>
      j.keySet should contain("xml")
      j.keySet should contain("zip")
    }
    assert(response != null, "Response is null")
    (xmlServStub.findByCriteriaAndFields _) verify (map, list)
    (zipServStub.findByCriteriaAndFields _) verify (map, list)

  }
}
