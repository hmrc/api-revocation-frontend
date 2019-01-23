/*
 * Copyright 2019 HM Revenue & Customs
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

object AuthorizedApplicationsPage extends WebPage {

  override val url: String = s"http://localhost:$port/applications-manage-authority/applications"

  override def isCurrentPage: Boolean = find(cssSelector("h1")).exists(_.text == "Authorised software applications")

  val applicationList: String = "data-applications"

  val applicationNameLinks: String = "[data-name-for]"

  def applicationNameLink(appId: UUID): By = By.cssSelector(s"[data-name-for='$appId']")

  def applicationScopeElement(appId: UUID, scopeKey: String): By = By.cssSelector(s"[data-scope-$appId='$scopeKey']")

  def authorityGrantDateElement(appId: UUID): By = By.cssSelector(s"[data-grant-date-$appId]")

  val applicationsMessageText: By = By.cssSelector("[data-info-message]")

  def withdrawPermissionButton(appId: UUID): By = By.cssSelector(s"[data-withdraw-permission-$appId]")
}
