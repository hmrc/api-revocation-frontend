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

package uk.gov.hmrc.apirevocationfrontend.controllers

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

import com.codahale.metrics.SharedMetricRegistries
import org.mockito.ArgumentMatchers.{any as `*`, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, InvalidBearerToken}
import uk.gov.hmrc.http.SessionKeys

import uk.gov.hmrc.apirevocationfrontend.config.{FooterConfig, FrontendAppConfig}
import uk.gov.hmrc.apirevocationfrontend.connectors.AuthorityNotFound
import uk.gov.hmrc.apirevocationfrontend.controllers.Revocation
import uk.gov.hmrc.apirevocationfrontend.models.{AppAuthorisation, ThirdPartyApplication}
import uk.gov.hmrc.apirevocationfrontend.service.RevocationService
import uk.gov.hmrc.apirevocationfrontend.stubs.FakeRequestCSRFSupport.*
import uk.gov.hmrc.apirevocationfrontend.stubs.Stubs
import uk.gov.hmrc.apirevocationfrontend.utils.AsyncHmrcSpec
import uk.gov.hmrc.apirevocationfrontend.views.html.ErrorView
import uk.gov.hmrc.apirevocationfrontend.views.html.revocation.*

class RevocationSpec extends AsyncHmrcSpec with GuiceOneAppPerSuite with Stubs with FixedClock with MockitoSugar {
  SharedMetricRegistries.clear()

  trait Setup {
    val appId: UUID                           = UUID.randomUUID()
    val authConnector: AuthConnector          = mock[AuthConnector]
    val revocationService: RevocationService  = mock[RevocationService]
    val errorPage                             = app.injector.instanceOf[ErrorView]
    val startPage                             = app.injector.instanceOf[StartView]
    val loggedOutPage                         = app.injector.instanceOf[LoggedOutView]
    val authorizedApplicationsPage            = app.injector.instanceOf[AuthorizedApplicationsView]
    val permissionWithdrawnPage               = app.injector.instanceOf[PermissionWithdrawnView]
    val withdrawPermissionPage                = app.injector.instanceOf[WithdrawPermissionView]
    implicit val appconfig: FrontendAppConfig = minimalAppConfig
    implicit val footerConfig: FooterConfig   = minimalFooterConfig

    val underTest: Revocation = new Revocation(
      authConnector,
      revocationService,
      stubMessagesControllerComponents(),
      errorPage,
      startPage,
      loggedOutPage,
      authorizedApplicationsPage,
      permissionWithdrawnPage,
      withdrawPermissionPage
    )

    when(revocationService.fetchApplicationAuthorities(using *))
      .thenReturn(successful(Seq.empty))
  }

  trait LoggedInSetup extends Setup {

    lazy val request = FakeRequest()
      .withSession(SessionKeys.sessionId -> "SessionId")
      .withCSRFToken

    when(authConnector.authorise(*, eqTo(EmptyRetrieval))(using *, *)).thenReturn(successful(()))
  }

  trait LoggedOutSetup extends Setup {
    lazy val request = FakeRequest()

    when(authConnector.authorise(*, eqTo(EmptyRetrieval))(using *, *)).thenReturn(Future.failed(InvalidBearerToken()))
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
      header("Location", result) shouldBe Some("/gg/sign-in?continue=/applications-manage-authority/applications")
    }
  }

  "withdrawPage" should {
    "return 200" in new LoggedInSetup {
      val appAuthority = AppAuthorisation(ThirdPartyApplication(appId, "appName"), Set(), instant)

      when(underTest.revocationService.fetchdApplicationAuthority(eqTo(appId))(using *))
        .thenReturn(successful(appAuthority))

      val result = underTest.withdrawPage(appId)(request)

      status(result) shouldBe Status.OK
    }
  }

  "withdrawAction" should {
    "redirect to authorisation withdrawn page" in new LoggedInSetup {
      when(underTest.revocationService.revokeApplicationAuthority(eqTo(appId))(using *))
        .thenReturn(successful(()))

      val result = underTest.withdrawAction(appId)(request)

      status(result) shouldBe 303
      header("Location", result) shouldBe Some(uk.gov.hmrc.apirevocationfrontend.controllers.routes.Revocation.withdrawConfirmationPage.url)
    }

    "return 404 if the authorisation is not found" in new LoggedInSetup {
      when(underTest.revocationService.revokeApplicationAuthority(eqTo(appId))(using *))
        .thenReturn(failed(new AuthorityNotFound()))

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
