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

package unit.service

import java.util.UUID

import connectors.DelegatedAuthorityConnector
import models.{AppAuthorisation, Scope, ThirdPartyApplication}
import org.joda.time.DateTime
import org.mockito.BDDMockito.given
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import service.{RevocationService, TrustedAuthorityRetrievalException, TrustedAuthorityRevocationException}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RevocationServiceSpec extends UnitSpec with MockitoSugar {

  trait Setup {

    implicit val headerCarrier = HeaderCarrier()
    val appId = UUID.randomUUID()

    val underTest = new RevocationService {
      override val delegatedAuthorityConnector = mock[DelegatedAuthorityConnector]
    }
  }

  "fetchUntrustedApplicationAuthorities" should {

    "return only untrusted applications" in new Setup {

      val authority1 = someAppAuthorisation(trusted = false)
      val authority2 = someAppAuthorisation(trusted = false)
      val trustedAuthority = someAppAuthorisation(trusted = true)

      given(underTest.delegatedAuthorityConnector.fetchApplicationAuthorities()(headerCarrier))
        .willReturn(Future(Seq(authority1, authority2, trustedAuthority)))

      await(underTest.fetchUntrustedApplicationAuthorities()) shouldBe Seq(authority1, authority2)
    }

    "return untrusted applications in application name order" in new Setup {
      val authority1 = untrustedApplicationWithName("Zapplication")
      val authority2 = untrustedApplicationWithName("Application")
      val authority3 = untrustedApplicationWithName("4pplication")

      given(underTest.delegatedAuthorityConnector.fetchApplicationAuthorities()(headerCarrier))
        .willReturn(Future(Seq(authority1, authority2, authority3)))

      await(underTest.fetchUntrustedApplicationAuthorities()) shouldBe Seq(authority3, authority2, authority1)
    }
  }

  "fetchUntrustedApplicationAuthority" should {

    "return authority if it is untrusted" in new Setup {

      val authority = someAppAuthorisation(trusted = false)

      given(underTest.delegatedAuthorityConnector.fetchApplicationAuthority(appId)(headerCarrier)).willReturn(Future(authority))

      await(underTest.fetchUntrustedApplicationAuthority(appId)) shouldBe authority

      verify(underTest.delegatedAuthorityConnector).fetchApplicationAuthority(appId)(headerCarrier)
    }

    "throw exception if authority is trusted" in new Setup {

      val authority = someAppAuthorisation(trusted = true)

      given(underTest.delegatedAuthorityConnector.fetchApplicationAuthority(appId)(headerCarrier)).willReturn(Future(authority))

      intercept[TrustedAuthorityRetrievalException](await(underTest.fetchUntrustedApplicationAuthority(appId)))

      verify(underTest.delegatedAuthorityConnector).fetchApplicationAuthority(appId)(headerCarrier)
    }
  }

  "revokeApplicationAuthority" should {

    "revoke authority if it is untrusted" in new Setup {

      val authority = someAppAuthorisation(trusted = false)

      given(underTest.delegatedAuthorityConnector.fetchApplicationAuthority(appId)(headerCarrier)).willReturn(Future(authority))
      given(underTest.delegatedAuthorityConnector.revokeApplicationAuthority(appId)(headerCarrier)).willReturn(Future(()))

      await(underTest.revokeApplicationAuthority(appId))

      verify(underTest.delegatedAuthorityConnector).fetchApplicationAuthority(appId)(headerCarrier)
      verify(underTest.delegatedAuthorityConnector).revokeApplicationAuthority(appId)(headerCarrier)
    }

    "throw exception if authority is trusted" in new Setup {

      val authority = someAppAuthorisation(trusted = true)

      given(underTest.delegatedAuthorityConnector.fetchApplicationAuthority(appId)(headerCarrier)).willReturn(Future(authority))

      intercept[TrustedAuthorityRevocationException](await(underTest.revokeApplicationAuthority(appId)))

      verify(underTest.delegatedAuthorityConnector).fetchApplicationAuthority(appId)(headerCarrier)
      verifyNoMoreInteractions(underTest.delegatedAuthorityConnector)
    }
  }

  private val scopes = Set(Scope("read:api-1", "scope name", "Access personal information"), Scope("read:api-3", "scope name", "Access tax information"))

  private def someAppAuthorisation(trusted: Boolean) = {
    AppAuthorisation(
      application = ThirdPartyApplication(UUID.randomUUID(), "First Application", trusted = trusted),
      scopes = scopes,
      earliestGrantDate = DateTime.now
    )
  }

  private def untrustedApplicationWithName(name: String) =
    AppAuthorisation(
      application = ThirdPartyApplication(UUID.randomUUID(), name, trusted = false),
      scopes = scopes,
      earliestGrantDate = DateTime.now
    )
}
