/*
 * Copyright 2017 HM Revenue & Customs
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

import java.net.URLEncoder
import java.util.UUID

import acceptance.pages.AuthorizedApplicationsPage
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlEqualTo}
import play.api.http.HeaderNames
import play.api.libs.Crypto
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, PlainText}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.time.DateTimeUtils

object LoginStub extends SessionCookieBaker {

  private val sessionId = s"stubbed-${UUID.randomUUID}"

  def stubSuccessfulLogin() = {
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
        .withStatus(303)
        .withHeader(HeaderNames.SET_COOKIE, cookieValue(data))
        .withHeader(HeaderNames.LOCATION, AuthorizedApplicationsPage.url)))

    stubFor(get(urlEqualTo("/auth/authority"))
      .willReturn(
        aResponse()
          .withStatus(200)
          .withBody(
            s"""
               |{
               |    "uri": "/auth/oid/1234567890",
               |    "loggedInAt": "2014-06-09T14:57:09.522Z",
               |    "previouslyLoggedInAt": "2014-06-09T14:48:24.841Z",
               |    "accounts": {
               |    },
               |    "levelOfAssurance": "2",
               |    "confidenceLevel" : 50,
               |    "credentialStrength": "strong",
               |    "legacyOid":"1234567890"
               |}
               |
            """.stripMargin
          )))
  }
}

trait SessionCookieBaker {
  def cookieValue(sessionData: Map[String,String]) = {
    def encode(data: Map[String, String]): PlainText = {
      val encoded = data.map {
        case (k, v) => URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")
      }.mkString("&")
      val key = "yNhI04vHs9<_HWbC`]20u`37=NGLGYY5:0Tg5?y`W<NoJnXWqmjcgZBec@rOxb^G".getBytes
      PlainText(Crypto.sign(encoded, key) + "-" + encoded)
    }

    val encodedCookie = encode(sessionData)
    val encrypted = CompositeSymmetricCrypto.aesGCM("gvBoGdgzqG1AarzF1LY0zQ==", Seq()).encrypt(encodedCookie).value

    s"""mdtp="$encrypted"; Path=/; HTTPOnly"; Path=/; HTTPOnly"""
  }
}
