import play.core.PlayVersion
import sbt._

object AppDependencies {

  lazy val bootstrapVersion    = "10.5.0"
  lazy val commonDomainVersion = "0.19.0"

  def apply(): Seq[ModuleID] = compile ++ test

  lazy val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "12.25.0"
  )

  lazy val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30"          % bootstrapVersion,
    "org.jsoup"    % "jsoup"                           % "1.10.2",
    "org.mockito" %% "mockito-scala-scalatest"         % "1.17.30",
    "uk.gov.hmrc" %% "api-platform-common-domain-fixtures" % commonDomainVersion
  ).map(_ % "test")
}
