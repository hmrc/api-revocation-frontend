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

package acceptance.specs

import java.util.UUID

import acceptance.pages.AuthorizedApplicationsPage.{applicationNameLink, withdrawPermissionButton}
import acceptance.pages.PermissionWithdrawnPage.{withdrawnContinueLink, withdrawnMessageText}
import acceptance.pages.WithdrawPermissionPage.{withdrawCancelButton, withdrawWarningText}
import acceptance.pages._
import acceptance.{BaseSpec, NavigationSugar}
import models.{AppAuthorisation, Scope, ThirdPartyApplication}
import org.joda.time.DateTime
import stubs.{DelegatedAuthorityStub, LoginStub}

class RevokeApplicationAuthoritySpec extends BaseSpec with NavigationSugar {

  feature("Revoking application authority") {

    scenario("User is able to revoke application authority") {
      val authority = someAppAuthority(trusted = false)

      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(Seq(authority))
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthority(authority)
      DelegatedAuthorityStub.stubSuccessfulAuthorityRevocation(authority)

      go(AuthorizedApplicationsPage)

      on(AuthorizedApplicationsPage)
      clickOnElement(applicationNameLink(authority.application.id))
      clickOnElement(withdrawPermissionButton(authority.application.id))

      on(WithdrawPermissionPage(authority.application.id))
      verifyText(withdrawWarningText, s"You are about to remove authority from ${authority.application.name}.")
      clickOnSubmit()

      on(PermissionWithdrawnPage)
      verifyText(withdrawnMessageText, "This application no longer has authority to interact with HMRC on your behalf.")
      clickOnElement(withdrawnContinueLink)

      on(AuthorizedApplicationsPage)
    }

    scenario("User is able to cancel application authority revocation") {
      val authority = someAppAuthority(trusted = false)

      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(Seq(authority))
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthority(authority)
      DelegatedAuthorityStub.stubSuccessfulAuthorityRevocation(authority)

      go(AuthorizedApplicationsPage)

      on(AuthorizedApplicationsPage)
      clickOnElement(applicationNameLink(authority.application.id))
      clickOnElement(withdrawPermissionButton(authority.application.id))

      on(WithdrawPermissionPage(authority.application.id))
      clickOnElement(withdrawCancelButton)

      on(AuthorizedApplicationsPage)
    }

    scenario("User is not able to revoke application authority if the application is trusted") {
      val authorityForTrustedApp = someAppAuthority(trusted = true)

      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(Seq(authorityForTrustedApp))
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthority(authorityForTrustedApp)

      go(AuthorizedApplicationsPage)

      go(WithdrawPermissionPage(authorityForTrustedApp.application.id))

      on(NotFoundPage)
    }

    scenario("User is not able to revoke non existent application authority") {
      val authority = someAppAuthority(trusted = false)

      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(Seq(authority))
      DelegatedAuthorityStub.stubFailedFetchApplicationAuthority(authority, status = 404)

      go(AuthorizedApplicationsPage)

      go(WithdrawPermissionPage(authority.application.id))

      on(NotFoundPage)
    }
  }

  private def someAppAuthority(trusted: Boolean) = {
    AppAuthorisation(
      application = ThirdPartyApplication(UUID.randomUUID(), "First Application", trusted = trusted),
      scopes = Set(Scope("read:api-1", "scope name", "Access personal information"), Scope("read:api-3", "scope name", "Access tax information")),
      earliestGrantDate = DateTime.now
    )
  }
}
