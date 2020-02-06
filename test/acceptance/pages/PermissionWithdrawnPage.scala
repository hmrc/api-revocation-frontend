/*
 * Copyright 2020 HM Revenue & Customs
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

import acceptance.WebPage
import org.openqa.selenium.By

object PermissionWithdrawnPage extends WebPage {

  override val url = s"http://localhost:$port/applications-manage-authority/application/authority-removed"

  override def isCurrentPage = find(cssSelector("h1")).exists(_.text == "Authority removed")

  val withdrawnContinueLink: By = By.cssSelector("[data-applications-link]")

  val withdrawnMessageText: By = By.cssSelector("[data-withdrawn-message]")
}
