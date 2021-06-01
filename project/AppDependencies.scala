import play.core.PlayVersion
import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = compile ++ test

  lazy val compile = Seq(
    "uk.gov.hmrc"               %% "play-partials"     % "6.9.0-play-26",
    "uk.gov.hmrc"               %% "bootstrap-play-26" % "1.3.0",
    "uk.gov.hmrc"               %% "play-ui"           % "8.12.0-play-26",
    "uk.gov.hmrc"               %% "govuk-template"    % "5.48.0-play-26",
    "org.apache.httpcomponents" % "httpclient"         % "4.3.3",
    "org.apache.httpcomponents" % "httpcore"           % "4.3.3",
    "com.typesafe.play"         %% "play-json-joda"    % "2.6.10"
  )

  lazy val test = Seq(
    "uk.gov.hmrc"             %% "hmrctest"                   % "3.9.0-play-26" % "test",
    "org.scalatest"           %% "scalatest"                  % "3.0.8" % "test",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "3.1.2" % "test",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current % "test",
    "org.pegdown"             %  "pegdown"                    % "1.6.0" % "test",
    "org.jsoup"               %  "jsoup"                      % "1.10.2" % "test",
    "com.github.tomakehurst"  %  "wiremock-jre8-standalone"   % "2.24.1" % "test",
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.7.1",
  )
}
