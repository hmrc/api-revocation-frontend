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

import acceptance.WebPage
import org.openqa.selenium.By

object StartPage extends WebPage {

  override val url: String = "http://localhost:9000/applications-permissions-withdrawal/"

  override def isCurrentPage: Boolean = find(cssSelector("h1")).fold(false)(_.text == "Withdraw permission to software accessing HMRC data")

  val startButton: By = By.cssSelector("[data-start-button]")
}
