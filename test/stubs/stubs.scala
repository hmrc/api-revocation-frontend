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

package stubs

import com.typesafe.config.{Config, ConfigFactory}
import config.FrontendAppConfig
import play.api.Mode.Test
import play.api.http.{DefaultFileMimeTypes, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import play.api.test.Helpers.{stubBodyParser, stubPlayBodyParsers}
import play.api.test.{CSRFTokenHelper, FakeRequest, NoMaterializer, StubMessagesFactory}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import uk.gov.hmrc.play.config.{AssetsConfig, GTMConfig, OptimizelyConfig, AccessibilityStatementConfig}
import uk.gov.hmrc.play.views.html.helpers.ReportAProblemLink
import uk.gov.hmrc.play.views.html.layouts.{BetaBanner, Footer, FooterLinks, GTMSnippet, Head, HeaderNav, MainContent, MainContentHeader, OptimizelySnippet, ServiceInfo, Sidebar}
import views.html.layouts.GovUkTemplate
import views.html.revocation.{authorizedApplications, loggedOut, permissionWithdrawn, start, withdrawPermission}
import views.html.{error_template, govuk_wrapper, main_template}

import scala.concurrent.ExecutionContext

trait Stubs extends StubMessagesFactory {

  val stubbedMessagesApi: MessagesApi = stubMessagesApi(Map(
    "en" -> Map(
      "global.error.InternalServerError500.title"      -> "We’re experiencing technical difficulties",
      "global.error.InternalServerError500.heading" -> "We’re experiencing technical difficulties",
      "global.error.InternalServerError500.message"       -> "Please try again in a few minutes."
    )
  ))

  def stubMessagesControllerComponents(
                                        bodyParser: BodyParser[AnyContent] = stubBodyParser(AnyContentAsEmpty),
                                        playBodyParsers: PlayBodyParsers = stubPlayBodyParsers(NoMaterializer),
                                        messagesApi: MessagesApi = stubbedMessagesApi,
                                        langs: Langs = stubLangs(),
                                        fileMimeTypes: FileMimeTypes = new DefaultFileMimeTypes(FileMimeTypesConfiguration()),
                                        executionContext: ExecutionContext = ExecutionContext.global
                                      ): MessagesControllerComponents =
    DefaultMessagesControllerComponents(
      new DefaultMessagesActionBuilderImpl(bodyParser, messagesApi)(executionContext),
      DefaultActionBuilder(bodyParser)(executionContext),
      playBodyParsers,
      messagesApi,
      langs,
      fileMimeTypes,
      executionContext
    )

  private val minimalConfig: Config = ConfigFactory.parseString("""
                                                                  |auditing.enabled=false
                                                                  |auditing.traceRequests=false
                                                                  |assets.url="localhost"
                                                                  |assets.version="version"
                                                                  |google-analytics.token=N/A
                                                                  |google-analytics.host=localhostGoogle
                                                                  |metrics.name=""
                                                                  |metrics.rateUnit="SECONDS"
                                                                  |metrics.durationUnit="SECONDS"
                                                                  |metrics.showSamples=false
                                                                  |metrics.jvm=false
                                                                  |metrics.logback=false""".stripMargin)

  val minimalConfiguration: Configuration = Configuration(minimalConfig)
  private val environment = Environment.simple()

  private def runMode(conf: Configuration): RunMode = new RunMode(conf, Test)
  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf, runMode(conf))
  private def appConfig(conf: Configuration) = new FrontendAppConfig(conf, environment, servicesConfig(conf))

  val minimalAppConfig: FrontendAppConfig = appConfig(minimalConfiguration)

  private val head: Head = new Head(
    new OptimizelySnippet(new OptimizelyConfig(minimalConfiguration)),
    new AssetsConfig(minimalConfiguration),
    new GTMSnippet(new GTMConfig(minimalConfiguration))
  )

  private val footer: Footer = new Footer(new AssetsConfig(minimalConfiguration))

  private def accessibilityStatementConfig = new AccessibilityStatementConfig(minimalConfiguration)

  val govUkWrapper: govuk_wrapper = new  govuk_wrapper (minimalAppConfig,
                                                      new BetaBanner(),
                                                      new GovUkTemplate(),
                                                      head,
                                                      new HeaderNav(),
                                                      footer,
                                                      new ServiceInfo(),
                                                      new MainContentHeader(),
                                                      new MainContent(),
                                                      new ReportAProblemLink(),
                                                      new FooterLinks(accessibilityStatementConfig))

  val errorTemplate: error_template = new error_template(minimalAppConfig, govUkWrapper)
  val sideBar : Sidebar = new Sidebar()
  val main: main_template = new main_template(govUkWrapper, sideBar)
  val startPage: start = new start(main)
  val loggedOutPage: loggedOut = new loggedOut(main)
  val authorizedApplicationsPage: authorizedApplications = new authorizedApplications(main)
  val permissionWithdrawnPage: permissionWithdrawn = new permissionWithdrawn(main)
  val withdrawPermissionPage: withdrawPermission = new withdrawPermission(main)

}

object FakeRequestCSRFSupport {

  implicit class CSRFFakeRequest[A](request: FakeRequest[A]) {
    def withCSRFToken: Request[A] = CSRFTokenHelper.addCSRFToken(request)
  }
}
