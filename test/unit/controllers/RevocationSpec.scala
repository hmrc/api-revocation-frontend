/*
 * Copyright 2021 HM Revenue & Customs
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

import com.codahale.metrics.SharedMetricRegistries
import com.kenshoo.play.metrics.PlayModule
import connectors.AuthorityNotFound
import controllers.Revocation
import models.{AppAuthorisation, ThirdPartyApplication}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import service.RevocationService
import stubs.FakeRequestCSRFSupport._
import stubs.Stubs
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, InvalidBearerToken}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class RevocationSpec extends UnitSpec with WithFakeApplication with MockitoSugar with Stubs {
  SharedMetricRegistries.clear()
  override def bindModules: Seq[GuiceableModule] = Seq(new PlayModule)

  trait Setup {
    val appId: UUID = UUID.randomUUID()
    val authConnector: AuthConnector = mock[AuthConnector]
    val revocationService: RevocationService = mock[RevocationService]

    val underTest: Revocation = new Revocation(authConnector, revocationService,stubMessagesControllerComponents(), minimalAppConfig, errorTemplate, startPage, loggedOutPage,
      authorizedApplicationsPage, permissionWithdrawnPage, withdrawPermissionPage)

    given(revocationService.fetchApplicationAuthorities()(any(classOf[HeaderCarrier])))
      .willReturn(successful(Seq.empty))
  }

  trait LoggedInSetup extends Setup {
    lazy val request = FakeRequest()
      .withSession(SessionKeys.sessionId -> "SessionId")
    .withCSRFToken

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
      result.header.headers("Location") shouldEqual "/gg/sign-in?continue=/applications-manage-authority/applications"
    }
  }

  "withdrawPage" should {
    "return 200" in new LoggedInSetup {
      val appAuthority = AppAuthorisation(ThirdPartyApplication(appId, "appName"), Set(), DateTime.now)

      given(underTest.revocationService.fetchdApplicationAuthority(ArgumentMatchers.eq(appId))(any(classOf[HeaderCarrier])))
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
  }

  "withdrawConfirmationPage" should {
    "return 200" in new LoggedInSetup {
      val result = underTest.withdrawConfirmationPage(request)

      status(result) shouldBe Status.OK
    }
  }
}
