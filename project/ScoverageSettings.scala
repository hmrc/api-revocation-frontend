import scoverage.ScoverageKeys._

object ScoverageSettings {
  def apply() = Seq(
    coverageMinimum := 78,
    coverageFailOnMinimum := true,
    coverageHighlighting := true,
    coverageExcludedPackages :=  Seq(
      "<empty>",
      "prod.*",
      "testOnlyDoNotUseInAppConf.*",
      "app.*",
      ".*Reverse.*",
      ".*Routes.*",
      "com\\.kenshoo\\.play\\.metrics\\.*",
      "uk\\.gov\\.hmrc\\.testuser\\.MicroserviceModule",
      ".*definition.*",
      ".*BuildInfo.*",
      ".*javascript"
    ).mkString(";")
  )
}