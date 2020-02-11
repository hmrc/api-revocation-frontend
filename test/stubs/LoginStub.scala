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

package stubs

import java.util.UUID

import _root_.play.api.http.{HeaderNames, SecretConfiguration}
import _root_.play.api.libs.crypto.DefaultCookieSigner
import _root_.play.api.mvc.SessionCookieBaker
import acceptance.Env._
import acceptance.pages.AuthorizedApplicationsPage
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.FeatureSpec
import play.api.http.Status.{OK, SEE_OTHER, UNAUTHORIZED}
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.filters.frontend.crypto.SessionCookieCrypto
import uk.gov.hmrc.time.DateTimeUtils

trait LoginStub extends SessionCookieCryptoFilterWrapper {


  private val sessionId = s"stubbed-${UUID.randomUUID}"

  def stubLoggedOutUser(): StubMapping = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(
        aResponse()
          .withStatus(UNAUTHORIZED)))
  }

  def stubSuccessfulLogin(): StubMapping = {
    val data = Map(
      SessionKeys.sessionId -> sessionId,
      SessionKeys.userId -> "/auth/oid/1234567890",
      SessionKeys.authToken -> "Bearer+1234",
      SessionKeys.token -> "token",
      SessionKeys.authProvider -> "GGW",
      SessionKeys.lastRequestTimestamp -> DateTimeUtils.now.getMillis.toString
    )
    stubFor(get(urlEqualTo(s"/gg/sign-in?continue=${AuthorizedApplicationsPage.url}"))
      .willReturn(aResponse()
        .withStatus(SEE_OTHER)
        .withHeader(HeaderNames.SET_COOKIE, encryptCookie(data))
        .withHeader(HeaderNames.LOCATION, AuthorizedApplicationsPage.url)))

    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(
            s"""
               |{}
            """.stripMargin
          )))
  }
}



trait SessionCookieCryptoFilterWrapper {
  val sc = SecretConfiguration("secret")
  val cs = new DefaultCookieSigner(sc)
  val mtdpSessionCookie="mdtp"
  val signSeparator="-"

  val cookieBaker: SessionCookieBaker
  val sessionCookieCrypto: SessionCookieCrypto

  def encryptCookie(sessionData: Map[String, String]): String = {
    val encoded = cookieBaker.encode(sessionData)
    val encrypted: String = sessionCookieCrypto.crypto.encrypt(PlainText(encoded)).value
    s"""mdtp=$encrypted"""
  }
}
