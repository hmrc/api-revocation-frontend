import play.core.PlayVersion
import sbt._

object AppDependencies {

  val bootstrapVersion    = "10.5.0"
  val commonDomainVersion = "1.0.0"

  def apply(): Seq[ModuleID] = compile ++ test

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "12.25.0"
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"               % bootstrapVersion,
    "org.jsoup"          % "jsoup"                                % "1.10.2",
    "org.scalatestplus" %% "mockito-5-18"                         % "3.2.19.0",
    "uk.gov.hmrc"       %% "api-platform-common-domain-fixtures"  % commonDomainVersion
  ).map(_ % "test")
}
