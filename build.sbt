import uk.gov.hmrc.DefaultBuildSettings

lazy val appName = "api-revocation-frontend"

Global / bloopAggregateSourceDependencies := true
Global / bloopExportJarClassifiers := Some(Set("sources"))

inThisBuild(
  List(
    majorVersion := 0,
    scalaVersion := "3.7.4",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin, SbtWeb)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(ScoverageSettings(): _*)
  .settings(
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true
  )
  .settings(
    TwirlKeys.templateImports ++= Seq(
      "views.html.helper.CSPNonce",
      "uk.gov.hmrc.apirevocationfrontend.config.FrontendAppConfig",
      "uk.gov.hmrc.apirevocationfrontend.config.FooterConfig",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
    )
  )
  .settings(
    Test / fork := false,
    Test / parallelExecution := false,
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-eT"),
  )
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:msg=unused import&src=html/.*:s"
    )
  )

commands ++= Seq(
  Command.command("run-all-tests") { state => "test" :: state },

  Command.command("clean-and-test") { state => "clean" :: "compile" :: "run-all-tests" :: state },

  // Coverage does not need compile !
  Command.command("pre-commit") { state => "clean" :: "scalafmtAll" :: "scalafixAll" :: "coverage" :: "run-all-tests" :: "coverageOff" :: "coverageAggregate" :: state }
)
