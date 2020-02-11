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

object LoginPage extends WebPage {

    val baseUrl = s"http://localhost:$stubPort/gg/sign-in?continue="
  override val url= s"$baseUrl${AuthorizedApplicationsPage.url}"

  override val urlMatching = s"$baseUrl${AuthorizedApplicationsPage.urlMatching}"

  override def isCurrentPage: Boolean = find(cssSelector("h1")).exists(_.text == "Sign in")

}
