/*
 * Copyright 2016 HM Revenue & Customs
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

package unit.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import config.WSHttp
import connectors.DelegatedAuthorityConnector
import models.{AppAuthorisation, Scope, ThirdPartyApplication}
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, Matchers}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class DelegatedAuthorityConnectorSpec extends UnitSpec with Matchers with ScalaFutures with WiremockSugar with BeforeAndAfterEach with WithFakeApplication {


  trait Setup {
    implicit val hc = HeaderCarrier()

    val connector = new DelegatedAuthorityConnector {
      override val delegatedAuthorityUrl: String = wireMockUrl
      override val http: HttpPost with HttpGet = WSHttp
    }
  }

  val appId = "applicationId"
  val appName = "My App"
  val scopeKey = "read:api-name"
  val scopeName = "Access personal info"
  val scopeDescription = "Access personal info"
  val earliestGrantDate = 1460713641258L

  "fetchApplicationAuthorities" should {

    "retrieve all third party delegated authorities granted by a user" in new Setup {

      stubFor(get(urlEqualTo(s"/authority/granted-applications")).willReturn(
        aResponse().withStatus(200).withBody(
          s"""
             |[
             |{
             |  "application" : {
             |    "id":"$appId",
             |    "name":"$appName"
             |    },
             |  "scopes":
             |    [
             |      {
             |        "key":"$scopeKey",
             |        "name":"$scopeName",
             |        "description":"$scopeDescription"
             |      }
             |    ],
             |  "earliestGrantDate":$earliestGrantDate
             |}
             |]
     """.stripMargin)))

      val response = await(connector.fetchApplicationAuthorities())
      val expected = Seq(AppAuthorisation(ThirdPartyApplication(appId, appName), Set(Scope(scopeKey, scopeName, scopeDescription)), new DateTime(earliestGrantDate)))

      response shouldBe expected
    }

    "return an empty set if there are no authorised applications" in new Setup {
      stubFor(get(urlEqualTo(s"/authority/granted-applications")).willReturn(
        aResponse().withStatus(200).withBody("[]")))

      await(connector.fetchApplicationAuthorities()) shouldBe Seq()
    }
  }
}
