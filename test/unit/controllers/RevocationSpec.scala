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

package unit.controllers

import java.util.UUID

import com.kenshoo.play.metrics.PlayModule
import connectors.AuthorityNotFound
import controllers.Revocation
import models.{AppAuthorisation, ThirdPartyApplication}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.filters.csrf.CSRF.{Token, TokenProvider}
import service.{RevocationService, TrustedAuthorityRevocationException}
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, InvalidBearerToken}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class RevocationSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  override def bindModules: Seq[GuiceableModule] = Seq(new PlayModule)

  trait Setup {
    val appId: UUID = UUID.randomUUID()
    val authConnector: AuthConnector = mock[AuthConnector]
    val revocationService: RevocationService = mock[RevocationService]

    val underTest: Revocation = new Revocation(authConnector, revocationService)

    given(revocationService.fetchUntrustedApplicationAuthorities()(any(classOf[HeaderCarrier])))
      .willReturn(successful(Seq.empty))
  }

  trait LoggedInSetup extends Setup {
    lazy val request = FakeRequest()
      .withSession(SessionKeys.sessionId -> "SessionId")
      .copyFakeRequest(tags = Map(
        Token.NameRequestTag -> "csrfToken",
        Token.RequestTag -> fakeApplication.injector.instanceOf[TokenProvider].generateToken))

    given(authConnector.authorise(any(), ArgumentMatchers.eq(EmptyRetrieval))(any(), any())).willReturn(successful(()))
  }

  trait LoggedOutSetup extends Setup {
    lazy val request = FakeRequest()

    given(authConnector.authorise(any(), ArgumentMatchers.eq(EmptyRetrieval))(any(), any())).willReturn(Future.failed(InvalidBearerToken()))
  }

  "Start" should {
    "return 200" in new LoggedOutSetup {
      val result = underTest.start(request)

      status(result) shouldBe Status.OK
    }
  }

  "Logged Out" should {
    "return 200" in new LoggedOutSetup {
      val result = underTest.loggedOut(request)

      status(result) shouldBe Status.OK
    }
  }

  "listAuthorizedApplications" should {
    "return 200 when the user is logged in" in new LoggedInSetup {
      val result = underTest.listAuthorizedApplications(request)

      status(result) shouldBe Status.OK
    }

    "redirect to the login page when the user is not logged in" in new LoggedOutSetup {
      val result = underTest.listAuthorizedApplications(request)

      status(result) shouldBe 303
      result.header.headers("Location") shouldEqual "http://localhost:9025/gg/sign-in?continue=http://localhost:9686/applications-manage-authority/applications"
    }
  }

  "withdrawPage" should {
    "return 200" in new LoggedInSetup {
      val appAuthority = AppAuthorisation(ThirdPartyApplication(appId, "appName", trusted = false), Set(), DateTime.now)

      given(underTest.revocationService.fetchUntrustedApplicationAuthority(ArgumentMatchers.eq(appId))(any(classOf[HeaderCarrier])))
        .willReturn(successful(appAuthority))

      val result = underTest.withdrawPage(appId)(request)

      status(result) shouldBe Status.OK
    }
  }

  "withdrawAction" should {
    "redirect to authorisation withdrawn page" in new LoggedInSetup {
      given(underTest.revocationService.revokeApplicationAuthority(ArgumentMatchers.eq(appId))(any(classOf[HeaderCarrier])))
        .willReturn(successful(()))

      val result = underTest.withdrawAction(appId)(request)

      status(result) shouldBe 303
      result.header.headers("Location") shouldEqual controllers.routes.Revocation.withdrawConfirmationPage().url
    }

    "return 404 if the authorisation is not found" in new LoggedInSetup {
      given(underTest.revocationService.revokeApplicationAuthority(ArgumentMatchers.eq(appId))(any(classOf[HeaderCarrier])))
        .willReturn(failed(new AuthorityNotFound()))

      val result = underTest.withdrawAction(appId)(request)

      status(result) shouldBe 404
    }

    "return 404 if the authorisation does exist, but it is for a trusted application" in new LoggedInSetup {
      given(underTest.revocationService.revokeApplicationAuthority(ArgumentMatchers.eq(appId))(any(classOf[HeaderCarrier])))
        .willReturn(failed(TrustedAuthorityRevocationException(appId)))

      val result = underTest.withdrawAction(appId)(request)

      status(result) shouldBe 404
    }
  }

  "withdrawConfirmationPage" should {
    "return 200" in new LoggedInSetup {
      val result = underTest.withdrawConfirmationPage(request)

      status(result) shouldBe Status.OK
    }
  }
}
