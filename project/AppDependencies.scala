import play.core.PlayVersion
import sbt._

object AppDependencies {

  lazy val bootstrapVersion = "7.21.0"

  def apply(): Seq[ModuleID] = compile ++ test

  lazy val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"         % "7.19.0-play-28",
    "com.typesafe.play" %% "play-json-joda"             % "2.8.1"
  )

  lazy val test = Seq(
    "uk.gov.hmrc"           %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.jsoup"              % "jsoup"                    % "1.10.2",
    "com.github.tomakehurst" % "wiremock-jre8-standalone" % "2.24.1",
    "org.mockito"           %% "mockito-scala-scalatest"  % "1.7.1"
  ).map(_ % "test")
}
