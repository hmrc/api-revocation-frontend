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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import play.api.test.FakeRequest
import play.twirl.api.Html

import uk.gov.hmrc.apirevocationfrontend.views.CommonViewSpec
import uk.gov.hmrc.apirevocationfrontend.views.html.revocation.StartView

class StartSpec extends CommonViewSpec {

  trait Setup {
    val start = app.injector.instanceOf[StartView]
  }

  "start page" should {

    "render correctly" in new Setup {
      val page: Html         = start.render(FakeRequest(), messagesProvider.messages, appConfig, footerConfig)
      val document: Document = Jsoup.parse(page.body)

      document.getElementById("page-heading").text() shouldBe "Manage the authority you have granted to software applications"
    }
  }

}
