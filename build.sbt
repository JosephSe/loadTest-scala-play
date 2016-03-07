name := """loadTest-scala"""

version := "2.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(SbtWeb)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
//  cache,
//  ws,
  specs2 % Test,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.9" exclude("org.apache.logging.log4j", "log4j-core"),
  "net.databinder.dispatch" % "dispatch-core_2.11" % "0.11.3",
  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.9",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.9",
  "com.typesafe.akka" % "akka-camel_2.11" % "2.3.9",
  "com.github.fdimuccio" %% "play2-sockjs" % "0.4.0",
  "org.webjars" % "angular-material" % "1.0.5",
  //SocksJS
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
  //  Conflict resolution
  "org.apache.httpcomponents" % "httpclient" % "4.3.4" exclude("commons-logging", "commons-logging"),
  "com.google.guava" % "guava" % "18.0",
  "org.apache.httpcomponents" % "httpcore" % "4.4",
//  "commons-logging" % "commons-logging" % "1.1.3",
  "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.4",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4",
  "javax.mail" % "mail" % "1.4"
//  "io.swagger" %% "swagger-play2" % "1.5.1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

scalacOptions += "-Ylog-classpath"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

(managedClasspath in Runtime) += (packageBin in Assets).value

WebKeys.packagePrefix in Assets := "public/"

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

fork in run := true