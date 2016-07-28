package service

import com.typesafe.config.{Config, ConfigFactory}
import model.JenkinsJob
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers, ParallelTestExecution}
import play.api.{Configuration, GlobalSettings, Play}
import play.api.cache.CacheApi
import play.api.test.FakeApplication
import serviceBroker.JenkinsBroker

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

//@RunWith(classOf[JUnitSuiteRunner])
class JenkinsServiceSpec extends FlatSpec with Matchers with MockFactory with ScalaFutures with ParallelTestExecution {

  val cache = stub[CacheApi]
  val mockJenkinsBroker = stub[JenkinsBroker]

  class MockableConfig extends Configuration(mock[Config])

  val configStub = stub[MockableConfig]
  //    val conf = stub[play.api.Configuration]
  val underTest = new JenkinsServiceImpl(configStub, cache, mockJenkinsBroker)
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

}
