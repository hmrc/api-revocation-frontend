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

  override def isCurrentPage: Boolean = find(tagName("h1")).fold(false)({
    e => e.text == pageHeading
  })
}

case class WithdrawPermissionPage(applicationId: UUID) extends DynamicPage {
  override val pageHeading = "Remove authority"
  override val url = s"http://localhost:9000/applications-permissions-withdrawal/$applicationId/withdraw-permission"
}

object WithdrawPermissionPage {

  val withdrawCancelButton: By = By.cssSelector("[data-cancel-withdraw-permission]")

  val withdrawWarningText: By = By.cssSelector("[data-withdraw-warning]")
}

case class PermissionWithdrawnPage(applicationId: UUID) extends DynamicPage {
  override val pageHeading = "Authority removed"
  override val url = s"http://localhost:9000/applications-permissions-withdrawal/$applicationId/permission-withdrawn"
}

object PermissionWithdrawnPage {

  val withdrawnContinueLink: By = By.cssSelector("[data-applications-link]")

  val withdrawnMessageText: By = By.cssSelector("[data-withdrawn-message]")
}