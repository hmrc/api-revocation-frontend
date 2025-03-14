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

package uk.gov.hmrc.apirevocationfrontend.connectors

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

import com.codahale.metrics.SharedMetricRegistries
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import uk.gov.hmrc.apirevocationfrontend.models.{AppAuthorisation, Scope, ThirdPartyApplication}
import uk.gov.hmrc.apirevocationfrontend.stubs.DelegatedAuthorityStub
import uk.gov.hmrc.apirevocationfrontend.utils.{AsyncHmrcSpec, WireMockSupport}

class DelegatedAuthorityConnectorSpec extends AsyncHmrcSpec with GuiceOneAppPerSuite with DelegatedAuthorityStub with WireMockSupport with FixedClock {

  private trait Setup {
    SharedMetricRegistries.clear()

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val serviceConfig              = mock[ServicesConfig]
    val http                       = app.injector.instanceOf[HttpClientV2]

    val connector = new DelegatedAuthorityConnector(serviceConfig, http) {
      override val delegatedAuthorityUrl: String = wireMockUrl
    }

  }

  "fetchApplicationAuthorities" should {

    "retrieve all third party delegated authorities granted by a user" in new Setup {

      val authorities = Seq(anApplicationAuthority(), anApplicationAuthority())

      stubSuccessfulFetchApplicationAuthorities(authorities)

      await(connector.fetchApplicationAuthorities()) shouldBe authorities
    }

    "return an empty set if there are no authorised applications" in new Setup {

      val authorities = Seq.empty

      stubSuccessfulFetchApplicationAuthorities(authorities)

      await(connector.fetchApplicationAuthorities()) shouldBe authorities
    }
  }

  "fetchApplicationAuthority" should {

    "retrieve a single third party delegated authority granted by a user to the given application" in new Setup {

      val authority = anApplicationAuthority()

      stubSuccessfulFetchApplicationAuthority(authority)

      await(connector.fetchApplicationAuthority(authority.application.id)) shouldBe authority
    }

    "throw `AuthorityNotFound` if the third party delegated authority is not found" in new Setup {

      val authority = anApplicationAuthority()

      stubFailedFetchApplicationAuthority(authority, status = 404)

      intercept[AuthorityNotFound] {
        await(connector.fetchApplicationAuthority(authority.application.id))
      }
    }
  }

  "revokeApplicationAuthority" should {

    "remove a third party delegated authority" in new Setup {

      val authority = anApplicationAuthority()

      stubSuccessfulAuthorityRevocation(authority)

      await(connector.revokeApplicationAuthority(authority.application.id))
    }

    "throw `AuthorityNotFound` if the third party delegated authority is not found" in new Setup {

      val authority = anApplicationAuthority()

      stubFailedAuthorityRevocation(authority, status = 404)

      intercept[AuthorityNotFound] {
        await(connector.revokeApplicationAuthority(authority.application.id))
      }
    }
  }

  private def anApplicationAuthority() = {
    AppAuthorisation(
      application = ThirdPartyApplication(UUID.randomUUID(), "My App"),
      scopes = Set(Scope("read:api-name", "Access personal info", "Access personal info")),
      earliestGrantDate = Instant.ofEpochMilli(1460713641258L)
    )
  }
}
