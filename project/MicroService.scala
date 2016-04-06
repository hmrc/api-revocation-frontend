import sbt.Keys._
import sbt.Tests.{SubProcess, Group}
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._


trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import uk.gov.hmrc.{SbtBuildInfo, ShellPrompt}

  import TestPhases._

  val appName: String

  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val plugins : Seq[Plugins] = Seq(play.PlayScala)
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  def unitFilter(name: String): Boolean = name startsWith "unit"
  def acceptanceFilter(name: String): Boolean = name startsWith "acceptance"

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(Seq(play.PlayScala) ++ plugins : _*)
    .settings(playSettings : _*)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(
      targetJvm := "jvm-1.8",
      scalaVersion := "2.11.8",
      libraryDependencies ++= appDependencies,
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
      unmanagedSourceDirectories   in AcceptanceTest <<= (baseDirectory in AcceptanceTest)(base => Seq(base / "test")),
      testGrouping in AcceptanceTest := oneForkedJvmPerTest((definedTests in AcceptanceTest).value)
    )
    .settings(resolvers += Resolver.bintrayRepo("hmrc", "releases"))
}

private object TestPhases {
  lazy val AcceptanceTest = config("acceptance") extend Test

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}
