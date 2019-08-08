/*
 * Copyright 2019 HM Revenue & Customs
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

import java.util.UUID

import connectors.{AuthorityNotFound, DelegatedAuthorityConnector}
import models.{AppAuthorisation, Scope, ThirdPartyApplication}
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers}
import stubs.DelegatedAuthorityStub
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global

class DelegatedAuthorityConnectorSpec extends UnitSpec
  with Matchers
  with ScalaFutures
  with WiremockSugar
  with BeforeAndAfterEach
  with WithFakeApplication
  with MockitoSugar {

  private trait Setup {
    implicit val hc = HeaderCarrier()
    val serviceConfig = mock[ServicesConfig]
    val http = fakeApplication.injector.instanceOf[HttpClient]

    val connector = new DelegatedAuthorityConnector(serviceConfig, http) {
      override val delegatedAuthorityUrl: String = wireMockUrl
    }
  }

  "fetchApplicationAuthorities" should {

    "retrieve all third party delegated authorities granted by a user" in new Setup {

      val authorities = Seq(anApplicationAuthority(), anApplicationAuthority())

      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(authorities)

      await(connector.fetchApplicationAuthorities()) shouldBe authorities
    }

    "return an empty set if there are no authorised applications" in new Setup {

      val authorities = Seq.empty

      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(authorities)

      await(connector.fetchApplicationAuthorities()) shouldBe authorities
    }
  }

  "fetchApplicationAuthority" should {

    "retrieve a single third party delegated authority granted by a user to the given application" in new Setup {

      val authority = anApplicationAuthority()

      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthority(authority)

      await(connector.fetchApplicationAuthority(authority.application.id)) shouldBe authority
    }

    "throw `AuthorityNotFound` if the third party delegated authority is not found" in new Setup {

      val authority = anApplicationAuthority()

      DelegatedAuthorityStub.stubFailedFetchApplicationAuthority(authority, status = 404)

      intercept[AuthorityNotFound] {
        await(connector.fetchApplicationAuthority(authority.application.id))
      }
    }
  }

  "revokeApplicationAuthority" should {

    "remove a third party delegated authority" in new Setup {

      val authority = anApplicationAuthority()

      DelegatedAuthorityStub.stubSuccessfulAuthorityRevocation(authority)

      await(connector.revokeApplicationAuthority(authority.application.id))
    }

    "throw `AuthorityNotFound` if the third party delegated authority is not found" in new Setup {

      val authority = anApplicationAuthority()

      DelegatedAuthorityStub.stubFailedAuthorityRevocation(authority, status = 404)

      intercept[AuthorityNotFound] {
        await(connector.revokeApplicationAuthority(authority.application.id))
      }
    }
  }

  private def anApplicationAuthority() = {
    AppAuthorisation(
      application = ThirdPartyApplication(UUID.randomUUID(), "My App", trusted = true),
      scopes = Set(Scope("read:api-name", "Access personal info", "Access personal info")),
      earliestGrantDate = new DateTime(1460713641258L)
    )
  }
}
