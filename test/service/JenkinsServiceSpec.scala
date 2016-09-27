package service

import com.typesafe.config.{Config, ConfigFactory}
import dao.MongoCRUDService
import model.JenkinsJob
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import play.api.Play._
import play.api.{Configuration, GlobalSettings, Play}
import play.api.cache.CacheApi
import play.api.test.FakeApplication
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import serviceBroker.JenkinsBroker

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

//@RunWith(classOf[JUnitSuiteRunner])
class JenkinsServiceSpec extends FlatSpec with Matchers with MockFactory with ScalaFutures with ParallelTestExecution {

  val cache = stub[CacheApi]
  val mockJenkinsBroker = stub[JenkinsBroker]
  val mockMongoApi = mock[ReactiveMongoApi]

  class MockableConfig extends Configuration(mock[Config])
  val configStub = stub[MockableConfig]
  //    val conf = stub[play.api.Configuration]
  val underTest = new JenkinsServiceImpl(configStub, cache, mockJenkinsBroker, mockMongoApi)

  val emptyResponse = Future {
    JenkinsJob.empty
  }
  val emptyResList = Future {
    List(JenkinsJob.empty, JenkinsJob.empty)
  }

  "A getLatestDetails call" should "return a not null object" in {
    (mockJenkinsBroker.getLatestDetails _) when ("jenJob") returns (emptyResponse)

    val job = underTest.getLatestDetails("jenJob")
    assert(job != null, "Response is null")
  }
  it should "return valid JenkinsJob" in {
    (mockJenkinsBroker.getLatestDetails _) when ("jenJob") returns (emptyResponse)
    val job = underTest.getLatestDetails("jenJob")
    whenReady(job) { j =>
      j should equal(JenkinsJob.empty)
    }
  }
  it should "call service broker" in {
    (mockJenkinsBroker.getLatestDetails _) when ("jenJob") returns (emptyResponse)
    underTest.getLatestDetails("jenJob")
    (mockJenkinsBroker.getLatestDetails _) verify ("jenJob")
  }

  "A getAllJobDetails call" should "return a non empty list" in {
    (configStub.getStringList _) when ("jenkins.sitJobs") returns (Some(List("sit").asJava))
    (mockJenkinsBroker.getLatestDetails _) when ("jenJob") returns (emptyResponse)

    val job = underTest.getAllJobDetails
    assert(job != null, "Response is null")
    (mockJenkinsBroker.getLatestDetails _) verify ("sit")
  }

  "A allJobSummary call" should "return a list with JenkinsSummary in ascending order" ignore {
    (configStub.getStringList _) when ("jenkins.sitJobs") returns (Some(List("sit").asJava))

    val job = underTest.allJobSummary("prod", "jenJob")
    assert(job != null, "Response is null")
    whenReady(job) { result =>
      result.size shouldEqual (2)
      result.get("29/08/16").size shouldEqual (1)
      result.get("30/08/16").size shouldEqual (1)
    }
  }



}
