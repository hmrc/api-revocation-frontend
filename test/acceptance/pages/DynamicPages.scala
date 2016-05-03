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

package acceptance.pages

import java.util.UUID

import acceptance.WebPage
import org.openqa.selenium.By

trait DynamicPage extends WebPage {
  val pageHeading: String

  override def isCurrentPage: Boolean = find(tagName("h1")).exists {
    e => e.text == pageHeading
  }
}

case class WithdrawPermissionPage(applicationId: UUID) extends DynamicPage {
  override val pageHeading = "Remove authority"
  override val url = s"http://localhost:9000/applications-manage-authority/application/$applicationId/remove-authority"
}

object WithdrawPermissionPage {

  val withdrawCancelButton: By = By.cssSelector("[data-cancel-withdraw-permission]")

  val withdrawWarningText: By = By.cssSelector("[data-withdraw-warning]")
}