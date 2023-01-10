/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apirevocationfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import javax.inject.Provider

case class FrontendAppConfig(
    analyticsToken: String,
    analyticsHost: String,
    betaFeedbackUrl: String,
    betaFeedbackUnauthenticatedUrl: String,
    reportAProblemPartialUrl: String,
    reportAProblemNonJSUrl: String,
    reportProblemHost: String,
    signInUrl: String,
    signOutUrl: String
  )

@Singleton
class FrontendAppConfigProvider @Inject() (val configuration: Configuration, val environment: Environment, servicesConfig: ServicesConfig) extends Provider[FrontendAppConfig] {

  private def loadConfig(key: String) = configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  override def get(): FrontendAppConfig = {
    lazy val caFrontendHost       = configuration.getOptional[String]("ca-frontend.host").getOrElse("")
    lazy val contactHost          = configuration.getOptional[String]("contact-frontend.host").getOrElse("")
    lazy val loginCallbackBaseUrl = configuration.getOptional[String]("auth.login-callback.base-url").getOrElse("")

    lazy val contactFormServiceIdentifier = "api-revocation-frontend"

    lazy val analyticsToken                 = loadConfig(s"google-analytics.token")
    lazy val analyticsHost                  = loadConfig(s"google-analytics.host")
    lazy val betaFeedbackUrl                = s"$contactHost/contact/beta-feedback"
    lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"
    lazy val reportAProblemPartialUrl       = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
    lazy val reportAProblemNonJSUrl         = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

    lazy val reportProblemHost: String =
      configuration.getOptional[String]("report-a-problem.base.url").getOrElse("") + configuration.getOptional[String]("urls.report-a-problem.problem").getOrElse("")
    lazy val signInUrl                 = s"$caFrontendHost/gg/sign-in?continue=$loginCallbackBaseUrl/applications-manage-authority/applications"
    lazy val signOutUrl                = s"$caFrontendHost/gg/sign-out?continue=$loginCallbackBaseUrl/applications-manage-authority/loggedout"

    FrontendAppConfig(
      analyticsToken,
      analyticsHost,
      betaFeedbackUrl,
      betaFeedbackUnauthenticatedUrl,
      reportAProblemPartialUrl,
      reportAProblemNonJSUrl,
      reportProblemHost,
      signInUrl,
      signOutUrl
    )
  }
}
