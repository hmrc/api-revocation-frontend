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

import acceptance.pages.{AuthorizedApplicationsPage, StartPage}
import acceptance.stubs.{DelegatedAuthorityStub, LoginStub}
import acceptance.{BaseSpec, NavigationSugar}
import models.{AppAuthorisation, ThirdPartyApplication}
import org.joda.time.DateTime

class StartSpec extends BaseSpec with NavigationSugar {

  feature("Present the user with a Start page") {

    scenario("User can see the Start page and navigate to the list of authorised applications") {

      val app = AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "First Application"), Set.empty, DateTime.now)

      go(StartPage)
      on(StartPage)

      LoginStub.stubSuccessfulLogin()
      DelegatedAuthorityStub.stubSuccessfulFetchApplicationAuthorities(Seq(app))

      clickOnElement(StartPage.startButton)
      on(AuthorizedApplicationsPage)
    }
  }
}
