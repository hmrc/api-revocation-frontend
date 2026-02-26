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

package uk.gov.hmrc.apirevocationfrontend.service

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

import org.mockito.BDDMockito.`given` as Given
import org.mockito.Mockito.verify
import org.scalatestplus.mockito.MockitoSugar

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apirevocationfrontend.connectors.DelegatedAuthorityConnector
import uk.gov.hmrc.apirevocationfrontend.models.{AppAuthorisation, Scope, ThirdPartyApplication}
import uk.gov.hmrc.apirevocationfrontend.service.RevocationService
import uk.gov.hmrc.apirevocationfrontend.utils.AsyncHmrcSpec

class RevocationServiceSpec extends AsyncHmrcSpec with FixedClock with MockitoSugar {

  private trait Setup {

    given HeaderCarrier = HeaderCarrier()
    val appId           = UUID.randomUUID()

    val delegatedAuthorityConnector = mock[DelegatedAuthorityConnector]
    val underTest                   = new RevocationService(delegatedAuthorityConnector)
  }

  "fetchApplicationAuthorities" should {
    "return applications in application name order" in new Setup {
      val authority1 = someAppAuthorisation("Zapplication")
      val authority2 = someAppAuthorisation("Application")
      val authority3 = someAppAuthorisation("4pplication")
      Given(underTest.delegatedAuthorityConnector.fetchApplicationAuthorities())
        .willReturn(successful(Seq(authority1, authority2, authority3)))

      await(underTest.fetchApplicationAuthorities) shouldBe Seq(authority3, authority2, authority1)
    }
  }

  "fetchApplicationAuthority" should {
    "return authority" in new Setup {
      val authority = someAppAuthorisation()
      Given(underTest.delegatedAuthorityConnector.fetchApplicationAuthority(appId))
        .willReturn(successful(authority))

      await(underTest.fetchdApplicationAuthority(appId)) shouldBe authority

      verify(underTest.delegatedAuthorityConnector).fetchApplicationAuthority(appId)
    }
  }

  "revokeApplicationAuthority" should {
    "revoke authority" in new Setup {
      Given(underTest.delegatedAuthorityConnector.revokeApplicationAuthority(appId))
        .willReturn(successful(()))

      await(underTest.revokeApplicationAuthority(appId))

      verify(underTest.delegatedAuthorityConnector).revokeApplicationAuthority(appId)
    }
  }

  private val scopes = Set(Scope("read:api-1", "scope name", "Access personal information"), Scope("read:api-3", "scope name", "Access tax information"))

  private def someAppAuthorisation(name: String = "First Application") =
    AppAuthorisation(
      application = ThirdPartyApplication(UUID.randomUUID(), name),
      scopes = scopes,
      earliestGrantDate = instant
    )
}
