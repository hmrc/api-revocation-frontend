import scoverage.ScoverageKeys._

object ScoverageSettings {
  def apply() = Seq(
    coverageMinimumStmtTotal := 92,
    coverageFailOnMinimum := true,
    coverageHighlighting := true,
    coverageExcludedPackages :=  Seq(
      "<empty>",
      "prod.*",
      "testOnlyDoNotUseInAppConf.*",
      "app.*",
      ".*Reverse.*",
      ".*Routes.*",
      ".*definition.*",
      ".*BuildInfo.*",
      ".*javascript"
    ).mkString(";")
  )
}