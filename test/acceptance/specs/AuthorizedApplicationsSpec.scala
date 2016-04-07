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

import acceptance.BaseSpec
import acceptance.pages.{LoginPage, AuthorizedApplicationsPage}
import acceptance.stubs.LoginStub

class AuthorizedApplicationsSpec extends BaseSpec {

  feature("Logged in") {

    scenario("User is redirected to the sign in page when not logged in") {

      go(AuthorizedApplicationsPage)

      redirectedTo(LoginPage)
    }

    scenario("User sees his authorized applications when logged in") {

      LoginStub.stubSuccessfulLogin()

      go(AuthorizedApplicationsPage)

      on(AuthorizedApplicationsPage)
    }

  }
}
