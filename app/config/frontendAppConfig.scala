/*
 * Copyright 2021 HM Revenue & Customs
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

package config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig


trait AppConfig {
  val analyticsToken: String
  val analyticsHost: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val signInUrl: String
  val signOutUrl: String
}

@Singleton
class FrontendAppConfig @Inject()(val configuration: Configuration,
                                  val environment: Environment,
                                  servicesConfig: ServicesConfig) extends AppConfig {

  private def loadConfig(key: String) =  configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val caFrontendHost = configuration.getOptional[String]("ca-frontend.host").getOrElse("")
  private val contactHost = configuration.getOptional[String]("contact-frontend.host").getOrElse("")
  private val loginCallbackBaseUrl = configuration.getOptional[String]("auth.login-callback.base-url").getOrElse("")

  private val contactFormServiceIdentifier = "api-revocation-frontend"

  override lazy val analyticsToken = loadConfig(s"google-analytics.token")
  override lazy val analyticsHost = loadConfig(s"google-analytics.host")
  override lazy val betaFeedbackUrl: String = s"$contactHost/contact/beta-feedback"
  override lazy val betaFeedbackUnauthenticatedUrl: String = s"$contactHost/contact/beta-feedback-unauthenticated"
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override lazy val signInUrl = s"$caFrontendHost/gg/sign-in?continue=$loginCallbackBaseUrl/applications-manage-authority/applications"
  override lazy val signOutUrl = s"$caFrontendHost/gg/sign-out?continue=$loginCallbackBaseUrl/applications-manage-authority/loggedout"
}
