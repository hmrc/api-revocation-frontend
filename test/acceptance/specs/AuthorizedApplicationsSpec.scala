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

import acceptance.pages.AuthorizedApplicationsPage._
import acceptance.pages.{AuthorizedApplicationsPage, LoginPage}
import acceptance.stubs.{DelegatedAuthorityStub, LoginStub}
import acceptance.{BaseSpec, NavigationSugar}
import models.{AppAuthorisation, Scope, ThirdPartyApplication}
import org.joda.time.DateTime

class AuthorizedApplicationsSpec extends BaseSpec with NavigationSugar {

  feature("Viewing authorised applications for user") {

    scenario("User is redirected to the sign in page when not logged in") {

      go(AuthorizedApplicationsPage)
      redirectedTo(LoginPage)
    }

    scenario("User sees his authorized applications") {

      val applications = Seq(
        AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "First Application", trusted = false),
          Set(Scope("read:api-1", "access personal information", "Access personal information description"),
              Scope("read:api-3", "access tax information", "Access tax information description")), DateTime.now),

        AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "Second Application", trusted = false),
          Set(Scope("read:api-2", "access confidential information", "Access confidential information description")), DateTime.now.minusDays(2)),

        AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "Third Application", trusted = false),
          Set(), DateTime.now.minusMonths(2))
      )

      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(applications)

      go(AuthorizedApplicationsPage)
      on(AuthorizedApplicationsPage)

      verifyApplicationsDisplayed(applications)
    }

    scenario("User sees only authorized applications, which are not trusted") {

      val trustedApp = AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "Third Application", trusted = true), Set(), DateTime.now.minusMonths(2))
      val otherApp = AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "Third Application", trusted = false), Set(), DateTime.now.minusMonths(2))

      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(Seq(trustedApp, otherApp))

      go(AuthorizedApplicationsPage)
      on(AuthorizedApplicationsPage)

      verifyApplicationsDisplayed(Seq(otherApp))
    }

    scenario("User sees 'no authorized applications' message if all of the authorised applications are trusted") {

      val trustedApp = AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "Third Application", trusted = true), Set(), DateTime.now.minusMonths(2))

      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(Seq(trustedApp))

      go(AuthorizedApplicationsPage)
      on(AuthorizedApplicationsPage)

      verifyNoAuthorisedApplicationsMessageDisplayed()
    }

    scenario("User sees 'no authorized applications' message if there are no authorized applications") {
      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(Seq.empty)

      go(AuthorizedApplicationsPage)
      on(AuthorizedApplicationsPage)

      verifyNoAuthorisedApplicationsMessageDisplayed()
    }
  }

  private def verifyNoAuthorisedApplicationsMessageDisplayed() = {
    verifyText(applicationsMessageText, "You currently have no authorised software applications.")
    verifyText(applicationsMessageText, "If you want to grant authority to an application you must do it in the application itself.")
  }

  private def verifyApplicationsDisplayed(apps: Seq[AppAuthorisation]) = {
    verifyText(applicationsMessageText, "You have granted authority to the following software applications. You can remove this authority below.")
    verifyListSize(applicationList, apps.size)

    apps.foreach { app =>
      verifyText(applicationNameLink(app.application.id), app.application.name)
      clickOnElement(applicationNameLink(app.application.id))
      app.scopes.foreach(scope => verifyText(applicationScopeElement(app.application.id, scope.key), scope.name))
      verifyText(authorityGrantDateElement(app.application.id), app.earliestGrantDate.toString("dd MMMM yyyy"))
    }
  }
}
