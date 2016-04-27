/*
 * Copyright 2016 HM Revenue & Customs
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
import acceptance.stubs.{DelegatedAuthorityStub, LoginStub}
import acceptance.{BaseSpec, NavigationSugar}
import models.{AppAuthorisation, Scope, ThirdPartyApplication}
import org.joda.time.DateTime

class RevokeApplicationAuthoritySpec extends BaseSpec with NavigationSugar {

  feature("Revoking application authority") {

    scenario("User is able to revoke application authority") {

      val app = applicationAuthority(UUID.randomUUID(), "First Application",
          Set(Scope("read:api-1", "scope name", "Access personal information"),
              Scope("read:api-3", "scope name", "Access tax information")), DateTime.now)

      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(Seq(app))
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthority(app)
      DelegatedAuthorityStub.stubSuccessfulAuthorityRevocation(app.application.id, app.application.name)

      go(AuthorizedApplicationsPage)

      on(AuthorizedApplicationsPage)
      clickOnElement(applicationNameLink(app.application.id))
      clickOnElement(withdrawPermissionButton(app.application.id))

      on(WithdrawPermissionPage(app.application.id))
      verifyText(withdrawWarningText, s"You are about to remove authority from ${app.application.name}.")
      clickOnSubmit()

      on(PermissionWithdrawnPage(app.application.id))
      verifyText(withdrawnMessageText, "This application no longer has authority to interact with HMRC on your behalf.")
      clickOnElement(withdrawnContinueLink)

      on(AuthorizedApplicationsPage)
    }

    scenario("User is able to cancel application authority revocation") {
      val app = applicationAuthority(UUID.randomUUID(), "First Application",
        Set(Scope("read:api-1", "scope name", "Access personal information"),
          Scope("read:api-3", "scope name", "Access tax information")), DateTime.now)

      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(Seq(app))
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthority(app)
      DelegatedAuthorityStub.stubSuccessfulAuthorityRevocation(app.application.id, app.application.name)

      go(AuthorizedApplicationsPage)

      on(AuthorizedApplicationsPage)
      clickOnElement(applicationNameLink(app.application.id))
      clickOnElement(withdrawPermissionButton(app.application.id))

      on(WithdrawPermissionPage(app.application.id))
      clickOnElement(withdrawCancelButton)

      on(AuthorizedApplicationsPage)
    }
  }

  private def applicationAuthority(appId: UUID, appName: String, scopes: Set[Scope], earliestGrantDate: DateTime) = {
    AppAuthorisation(ThirdPartyApplication(appId, appName), scopes, earliestGrantDate)
  }
}
