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

import com.codahale.metrics.SharedMetricRegistries
import connectors.AuthorityNotFound
import controllers.Revocation
import models.{AppAuthorisation, ThirdPartyApplication}
import org.joda.time.DateTime
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.RevocationService
import stubs.FakeRequestCSRFSupport._
import stubs.Stubs
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, InvalidBearerToken}
import uk.gov.hmrc.http.SessionKeys
import utils._
import views.html.error_template
import views.html.revocation._

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class RevocationSpec extends AsyncHmrcSpec with GuiceOneAppPerSuite with Stubs {
  SharedMetricRegistries.clear()

  trait Setup {
    val appId: UUID = UUID.randomUUID()
    val authConnector: AuthConnector = mock[AuthConnector]
    val revocationService: RevocationService = mock[RevocationService]
    val errorTemplate = app.injector.instanceOf[error_template]
    val startPage = app.injector.instanceOf[start]
    val loggedOutPage = app.injector.instanceOf[loggedOut]
    val authorizedApplicationsPage = app.injector.instanceOf[authorizedApplications]
    val permissionWithdrawnPage = app.injector.instanceOf[permissionWithdrawn]
    val withdrawPermissionPage = app.injector.instanceOf[withdrawPermission]

    val underTest: Revocation = new Revocation(authConnector, revocationService,stubMessagesControllerComponents(), minimalAppConfig, errorTemplate, startPage, loggedOutPage,
      authorizedApplicationsPage, permissionWithdrawnPage, withdrawPermissionPage)

    when(revocationService.fetchApplicationAuthorities()(*))
      .thenReturn(successful(Seq.empty))
  }

  trait LoggedInSetup extends Setup {
    lazy val request = FakeRequest()
      .withSession(SessionKeys.sessionId -> "SessionId")
    .withCSRFToken

    when(authConnector.authorise(*, eqTo(EmptyRetrieval))(*, *)).thenReturn(successful(()))
  }

  trait LoggedOutSetup extends Setup {
    lazy val request = FakeRequest()

    when(authConnector.authorise(*, eqTo(EmptyRetrieval))(*, *)).thenReturn(Future.failed(InvalidBearerToken()))
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
      header("Location",result) shouldBe Some("/gg/sign-in?continue=/applications-manage-authority/applications")
    }
  }

  "withdrawPage" should {
    "return 200" in new LoggedInSetup {
      val appAuthority = AppAuthorisation(ThirdPartyApplication(appId, "appName"), Set(), DateTime.now)

      when(underTest.revocationService.fetchdApplicationAuthority(eqTo(appId))(*))
        .thenReturn(successful(appAuthority))

      val result = underTest.withdrawPage(appId)(request)

      status(result) shouldBe Status.OK
    }
  }

  "withdrawAction" should {
    "redirect to authorisation withdrawn page" in new LoggedInSetup {
      when(underTest.revocationService.revokeApplicationAuthority(eqTo(appId))(*))
        .thenReturn(successful(()))

      val result = underTest.withdrawAction(appId)(request)

      status(result) shouldBe 303
      header("Location",result) shouldBe Some(controllers.routes.Revocation.withdrawConfirmationPage().url)
    }

    "return 404 if the authorisation is not found" in new LoggedInSetup {
      when(underTest.revocationService.revokeApplicationAuthority(eqTo(appId))(*))
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
