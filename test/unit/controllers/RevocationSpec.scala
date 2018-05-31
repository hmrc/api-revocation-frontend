/*
 * Copyright 2018 HM Revenue & Customs
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

package unit.controllers

import java.util.UUID

import com.kenshoo.play.metrics.PlayModule
import config.FrontendAuthConnector
import connectors.AuthorityNotFound
import controllers.Revocation
import models.{AppAuthorisation, ThirdPartyApplication}
import org.joda.time.DateTime
import org.mockito.BDDMockito.given
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.{Token, TokenProvider}
import service.{RevocationService, TrustedAuthorityRevocationException}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.frontend.auth.AuthenticationProviderIds.GovernmentGatewayId
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, Authority, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{failed, successful}

class RevocationSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  override def bindModules = Seq(new PlayModule)

  val appId = UUID.randomUUID()
  val authority = Authority(s"Test User", Accounts(), None, None, CredentialStrength.Strong, ConfidenceLevel.L50, None, None, None, "legacyOid")
  val headerCarrier = HeaderCarrier()

  lazy val loggedOutRequest = FakeRequest()
  lazy val loggedInRequest = FakeRequest().withSession(
    SessionKeys.sessionId -> "SessionId",
    SessionKeys.token -> "Token",
    SessionKeys.userId -> "Test User",
    SessionKeys.authProvider -> GovernmentGatewayId
  ).copyFakeRequest(tags = Map(
    Token.NameRequestTag -> "csrfToken",
    Token.RequestTag -> fakeApplication.injector.instanceOf[TokenProvider].generateToken))

  val authConnector = mock[FrontendAuthConnector]
  val revocationService = mock[RevocationService]

  val underTest = new Revocation(authConnector, revocationService) {
    implicit val hc = headerCarrier

    given(authConnector.currentAuthority(any(classOf[HeaderCarrier]), any(classOf[ExecutionContext])))
      .willReturn(successful(Some(authority)))
    given(revocationService.fetchUntrustedApplicationAuthorities()(any(classOf[HeaderCarrier])))
      .willReturn(successful(Seq.empty))
  }

  "Start" should {
    "return 200" in {

      val result = underTest.start(loggedOutRequest)

      status(result) shouldBe Status.OK
    }
  }

  "Logged Out" should {
    "return 200" in {

      val result = underTest.loggedOut(loggedOutRequest)

      status(result) shouldBe Status.OK
    }
  }

  "listAuthorizedApplications" should {
    "return 200 when the user is logged in" in {

      val result = underTest.listAuthorizedApplications(loggedInRequest)

      status(result) shouldBe Status.OK
    }

    "redirect to the login page when the user is not logged in" in {

      val result = underTest.listAuthorizedApplications(loggedOutRequest)

      status(result) shouldBe 303
      result.header.headers("Location") shouldEqual "http://localhost:9025/gg/sign-in?continue=http://localhost:9686/applications-manage-authority/applications"
    }
  }

  "withdrawPage" should {
    "return 200" in {
      val appAuthority = AppAuthorisation(ThirdPartyApplication(appId, "appName", trusted = false), Set(), DateTime.now)

      given(underTest.revocationService.fetchUntrustedApplicationAuthority(Matchers.eq(appId))(any(classOf[HeaderCarrier])))
        .willReturn(successful(appAuthority))

      val result = underTest.withdrawPage(appId)(loggedInRequest)

      status(result) shouldBe Status.OK
    }
  }

  "withdrawAction" should {
    "redirect to authorisation withdrawn page" in {

      given(underTest.revocationService.revokeApplicationAuthority(Matchers.eq(appId))(any(classOf[HeaderCarrier])))
        .willReturn(successful(()))

      val result = underTest.withdrawAction(appId)(loggedInRequest)

      status(result) shouldBe 303
      result.header.headers("Location") shouldEqual controllers.routes.Revocation.withdrawConfirmationPage().url
    }

    "return 404 if the authorisation is not found" in {

      given(underTest.revocationService.revokeApplicationAuthority(Matchers.eq(appId))(any(classOf[HeaderCarrier])))
        .willReturn(failed(new AuthorityNotFound()))

      val result = underTest.withdrawAction(appId)(loggedInRequest)

      status(result) shouldBe 404
    }

    "return 404 if the authorisation does exist, but it is for a trusted application" in {

      given(underTest.revocationService.revokeApplicationAuthority(Matchers.eq(appId))(any(classOf[HeaderCarrier])))
        .willReturn(failed(TrustedAuthorityRevocationException(appId)))

      val result = underTest.withdrawAction(appId)(loggedInRequest)

      status(result) shouldBe 404
    }
  }

  "withdrawConfirmationPage" should {
    "return 200" in {

      val result = underTest.withdrawConfirmationPage(loggedInRequest)

      status(result) shouldBe Status.OK
    }
  }
}
