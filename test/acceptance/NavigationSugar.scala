/*
 * Copyright 2017 HM Revenue & Customs
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

package acceptance

import org.openqa.selenium.support.ui.{ExpectedCondition, WebDriverWait}
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.concurrent.Eventually
import org.scalatest.selenium.WebBrowser
import org.scalatest.selenium.WebBrowser.{go => goo}
import org.scalatest.{Assertions, Matchers}
import scala.collection.convert.decorateAsScala._

trait NavigationSugar extends WebBrowser with Eventually with Assertions with Matchers {

  def goOn(page: WebPage)(implicit webDriver: WebDriver) = {
    go(page)
    on(page)
  }

  def go(page: WebLink)(implicit webDriver: WebDriver) = {
    goo to page
  }

  def clickOnSubmit() (implicit webDriver: WebDriver) = {
    webDriver.findElement(By.id("submit")).click()
  }

  def clickOnElement(selectorId: String)(implicit webDriver: WebDriver) = {
    webDriver.findElement(By.cssSelector(s"[$selectorId]")).click()
  }

  def clickOnElement(locator: By)(implicit webDriver: WebDriver) = {
    webDriver.findElement(locator).click()
  }

  def verifyListSize(selectorId: String, expectedSize: Int)(implicit webDriver: WebDriver) = {
    webDriver.findElements(By.cssSelector(s"[$selectorId] > li")).size() shouldBe expectedSize
  }

  def verifyText(selectorId: String, expected: String)(implicit webDriver: WebDriver) = {
    webDriver.findElement(By.cssSelector(s"[$selectorId]")).getText should include (expected)
  }

  def verifyText(locator: By, expected: String)(implicit webDriver: WebDriver) = {
    webDriver.findElement(locator).getText should include (expected)
  }

  def verifyOrderByText(locator: String, expected: Seq[String])(implicit webDriver: WebDriver) = {
    val elements = webDriver.findElements(By.cssSelector(locator)).listIterator.asScala.toSeq
    elements.map(_.getText) == expected
  }

  def redirectedTo(page: WebLink)(implicit webDriver: WebDriver) = {
    assertResult(page.url)(webDriver.getCurrentUrl)
  }

  def on(page: WebPage)(implicit webDriver: WebDriver) = {
    eventually {
      webDriver.findElement(By.tagName("body"))
    }
    withClue(s"Currently in page: $currentUrl " + find(tagName("h1")).map(_.text).fold(" - ")(h1 => s", with title '$h1' - ")) {
      assert(page.isCurrentPage, s"Page was not loaded: ${page.url}")
    }
  }

  def loadPage()(implicit webDriver: WebDriver) = {
    val wait = new WebDriverWait(webDriver, 30)
    wait.until(
      new ExpectedCondition[WebElement] {
        override def apply(d: WebDriver) = d.findElement(By.tagName("body"))
      }
    )
  }
}
