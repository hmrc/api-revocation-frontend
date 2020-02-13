import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning

import scala.util.Properties

lazy val appName = "api-revocation-frontend"

lazy val appDependencies: Seq[ModuleID] = compile ++ test

lazy val compile = Seq(
  ws,
  "uk.gov.hmrc" %% "play-partials" % "6.9.0-play-26",
  "uk.gov.hmrc" %% "bootstrap-play-26" % "1.3.0",
  "uk.gov.hmrc" %% "play-ui" % "8.7.0-play-26",
  "uk.gov.hmrc" %% "govuk-template" % "5.48.0-play-26",
  "org.apache.httpcomponents" % "httpclient" % "4.3.3",
  "org.apache.httpcomponents" % "httpcore" % "4.3.3",
  "com.typesafe.play"  %% "play-json-joda"  % "2.6.10"
)

lazy val wireMockVersion = "2.21.0"

lazy val test = Seq(
  "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-26" % "test",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
  "com.typesafe.play" %% "play-test"  % PlayVersion.current % "test",
  "org.pegdown" % "pegdown" % "1.6.0" % "test",
  "org.jsoup" % "jsoup" % "1.10.2" % "test",
  "com.github.tomakehurst" %  "wiremock-jre8"  % "2.24.1" % "test",
  "org.seleniumhq.selenium" % "selenium-java" % "2.53.0" % "test",
  "org.mockito" % "mockito-core" % "2.13.0" % "test"
)


// Transitive dependencies in scalatest/scalatestplusplay drag in a newer version of jetty that is not
// compatible with wiremock, so we need to pin the jetty stuff to the older version.
// see https://groups.google.com/forum/#!topic/play-framework/HAIM1ukUCnI
val jettyVersion = "9.4.26.v20200117"
lazy val akkaVersion = "2.5.23"
lazy val akkaHttpVersion = "10.0.15"

val overrides: Set[ModuleID] = Set(
  "com.typesafe.akka"           %% "akka-stream" % akkaVersion,
  "com.typesafe.akka"           %% "akka-protobuf" % akkaVersion,
  "com.typesafe.akka"           %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka"           %% "akka-actor" % akkaVersion,
  "com.typesafe.akka"           %% "akka-http-core" % akkaHttpVersion,
  "org.eclipse.jetty"           % "jetty-server"       % jettyVersion,
  "org.eclipse.jetty"           % "jetty-servlet"      % jettyVersion,
  "org.eclipse.jetty"           % "jetty-security"     % jettyVersion,
  "org.eclipse.jetty"           % "jetty-servlets"     % jettyVersion,
  "org.eclipse.jetty"           % "jetty-continuation" % jettyVersion,
  "org.eclipse.jetty"           % "jetty-webapp"       % jettyVersion,
  "org.eclipse.jetty"           % "jetty-xml"          % jettyVersion,
  "org.eclipse.jetty"           % "jetty-client"       % jettyVersion,
  "org.eclipse.jetty"           % "jetty-http"         % jettyVersion,
  "org.eclipse.jetty"           % "jetty-io"           % jettyVersion,
  "org.eclipse.jetty"           % "jetty-util"         % jettyVersion,
  "org.eclipse.jetty.websocket" % "websocket-api"      % jettyVersion,
  "org.eclipse.jetty.websocket" % "websocket-common"   % jettyVersion,
  "org.eclipse.jetty.websocket" % "websocket-client"   % jettyVersion
)

lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty
lazy val microservice = (project in file("."))
  .enablePlugins(Seq(_root_.play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory, SbtWeb) ++ plugins: _*)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    name := appName,
    majorVersion := 0,
    targetJvm := "jvm-1.8",
    scalaVersion := "2.11.11",
    libraryDependencies ++= appDependencies,
    dependencyOverrides ++= overrides,
    parallelExecution in Test := false,
    fork in Test := false,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(testOptions in Test := Seq(Tests.Filter(unitFilter)),
    addTestReportOption(Test, "test-reports")
  )
  .configs(AcceptanceTest)
  .settings(inConfig(AcceptanceTest)(Defaults.testSettings): _*)
  .settings(
    testOptions in AcceptanceTest := Seq(Tests.Filter(acceptanceFilter)),
    unmanagedSourceDirectories in AcceptanceTest := (baseDirectory in AcceptanceTest) (base => Seq(base / "test")).value,
    addTestReportOption(AcceptanceTest, "int-test-reports"),
    testGrouping in AcceptanceTest := oneForkedJvmPerTest((definedTests in AcceptanceTest).value)
  )
  .settings(resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo))

lazy val AcceptanceTest = config("acceptance") extend Test

def unitFilter(name: String): Boolean = name startsWith "unit"

def acceptanceFilter(name: String): Boolean = name startsWith "acceptance"

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests map {
    test => Group(test.name, Seq(test),
      SubProcess(ForkOptions(runJVMOptions = Seq(s"-Dtest.name=${test.name}", s"-Dtest_driver=${Properties.propOrElse("test_driver", "chrome")}"))))
  }

// Coverage configuration
coverageMinimum := 90
coverageFailOnMinimum := true
coverageExcludedPackages := "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;app.*;uk.gov.hmrc.BuildInfo"
