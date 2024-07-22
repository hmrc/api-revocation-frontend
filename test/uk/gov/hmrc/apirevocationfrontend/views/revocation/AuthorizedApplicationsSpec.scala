/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.apirevocationfrontend.views.revocation

import java.util.UUID

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock

import uk.gov.hmrc.apirevocationfrontend.models.{AppAuthorisation, Scope, ThirdPartyApplication}
import uk.gov.hmrc.apirevocationfrontend.views.CommonViewSpec
import uk.gov.hmrc.apirevocationfrontend.views.html.revocation.AuthorizedApplicationsView

class AuthorizedApplicationsSpec extends CommonViewSpec with FixedClock {

  trait Setup {
    val authorizedApplicationsPage = app.injector.instanceOf[AuthorizedApplicationsView]
    private val scopes             = Set(Scope("read:api-1", "scope name", "Access personal information"), Scope("read:api-3", "scope name", "Access tax information"))

    val auth1 = AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "app1"), scopes, instant)
    val auth2 = AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "app2"), scopes, instant)
    val auth3 = AppAuthorisation(ThirdPartyApplication(UUID.randomUUID(), "app3"), scopes, instant)

    val apps = List(auth1, auth2, auth3)

  }

  "authorized Applications page" should {

    "render page correctly" in new Setup {

      val page: Html         = authorizedApplicationsPage.render(apps, FakeRequest(), messagesProvider.messages, appConfig, footerConfig)
      val document: Document = Jsoup.parse(page.body)
      document.getElementById("page-heading").text() shouldBe "Authorised software applications"
      document.getElementById("app-summary-0").text() shouldBe "app1"
      document.getElementById("app-summary-1").text() shouldBe "app2"
      document.getElementById("app-summary-2").text() shouldBe "app3"
    }
  }

}
