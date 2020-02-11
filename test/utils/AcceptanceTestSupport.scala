/*
 * Copyright 2020 HM Revenue & Customs
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

package utils

import acceptance.{Env, NavigationSugar}
import com.github.tomakehurst.wiremock.client.WireMock
import org.openqa.selenium.WebDriver
import org.scalatest.{BeforeAndAfterEach, FeatureSpec, Matchers}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.SessionCookieBaker
import play.api.{Configuration, Environment}
import stubs.{WireMockHelper, WireMockSupport}
import uk.gov.hmrc.play.bootstrap.filters.frontend.crypto.SessionCookieCrypto
import uk.gov.hmrc.play.test.WithFakeApplication


trait AcceptanceTestSupport extends FeatureSpec
  with WireMockHelper
  with WireMockSupport
  with GuiceOneServerPerSuite
  with BeforeAndAfterEach
  with NavigationSugar {

  // Override fakeApplication if you need a Application with other than
  // default parameters.

  private val minimalConfigMap = Map(
    "auditing.enabled" -> false,
    "auditing.traceRequests" -> false,
    "microservice.services.auth.host" -> stubHost,
    "microservice.services.auth.port" -> stubPort,
    "microservice.services.auth.login-callback.base-url" -> s"http://$stubHost:$stubPort",
    "microservice.services.third-party-delegated-authority.host" -> stubHost,
    "microservice.services.third-party-delegated-authority.port" -> stubPort,
    "microservice.services.third-party-application.host" -> stubHost,
    "microservice.services.third-party-application.port" -> stubPort,
    "api-revocation-frontend.host" -> "",
    "ca-frontend.host" -> s"http://localhost:$stubPort"
  )
  override def fakeApplication() = new GuiceApplicationBuilder().configure(minimalConfigMap).build()


  implicit val webDriver: WebDriver = Env.driver

  val cookieBaker = app.injector.instanceOf[SessionCookieBaker]
  val sessionCookieCrypto = app.injector.instanceOf[SessionCookieCrypto]

  override def beforeEach() = {
    webDriver.manage().deleteAllCookies()
    WireMock.reset()
  }


}

object AcceptanceTestSupport {

  implicit class StringOps(val s: String) extends AnyVal {

    def withPrefixReplace(prefix: String): String =
      prefix + s.drop(prefix.length())

    def withSuffixReplace(suffix: String): String =
      s.dropRight(suffix.length()) + suffix

  }

}