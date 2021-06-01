import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import bloop.integrations.sbt.BloopDefaults

lazy val appName = "api-revocation-frontend"

// Transitive dependencies in scalatest/scalatestplusplay drag in a newer version of jetty that is not
// compatible with wiremock, so we need to pin the jetty stuff to the older version.
// see https://groups.google.com/forum/#!topic/play-framework/HAIM1ukUCnI
lazy val akkaVersion = "2.5.23"
lazy val akkaHttpVersion = "10.0.15"

val overrides: Seq[ModuleID] = Seq(
  "com.typesafe.akka"           %% "akka-stream"        % akkaVersion,
  "com.typesafe.akka"           %% "akka-protobuf"      % akkaVersion,
  "com.typesafe.akka"           %% "akka-slf4j"         % akkaVersion,
  "com.typesafe.akka"           %% "akka-actor"         % akkaVersion,
  "com.typesafe.akka"           %% "akka-http-core"     % akkaHttpVersion
)

lazy val playSettings: Seq[Setting[_]] = Seq.empty
lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin, SbtWeb)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(SilencerSettings(): _*)
  .settings(ScoverageSettings(): _*)
  .settings(
    name := appName,
    majorVersion := 0,
    targetJvm := "jvm-1.8",
    scalaVersion := "2.12.12",
    libraryDependencies ++= AppDependencies(),
    // dependencyOverrides ++= overrides,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(inConfig(Test)(Defaults.testSettings))
  .settings(inConfig(Test)(BloopDefaults.configSettings))
  .settings(
    Test / fork := false,
    Test / parallelExecution := false,
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-eT"),
    Test / unmanagedSourceDirectories += baseDirectory.value / "test",
    addTestReportOption(Test, "test-reports")
  )
