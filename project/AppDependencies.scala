import play.core.PlayVersion
import sbt._

object AppDependencies {
  def apply(): Seq[ModuleID] = compile ++ test

  lazy val compile = Seq(
    "uk.gov.hmrc"               %% "bootstrap-frontend-play-28" % "5.16.0",
    "uk.gov.hmrc"               %% "play-partials"              % "8.2.0-play-28",
    "uk.gov.hmrc"               %% "play-ui"                    % "9.7.0-play-28",
    "uk.gov.hmrc"               %% "govuk-template"             % "5.72.0-play-28",
    "org.apache.httpcomponents" %  "httpclient"                 % "4.3.3",
    "org.apache.httpcomponents" %  "httpcore"                   % "4.3.3",
    "com.typesafe.play"         %% "play-json-joda"             % "2.8.1"
  )

  lazy val test = Seq(
    "uk.gov.hmrc"               %% "bootstrap-test-play-28"     % "5.16.0",
    "org.jsoup"                 %  "jsoup"                      % "1.10.2",
    "com.github.tomakehurst"    %  "wiremock-jre8-standalone"   % "2.24.1",
    "org.mockito"               %% "mockito-scala-scalatest"    % "1.7.1",
  ).map(_ % "test")
}
