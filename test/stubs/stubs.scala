/*
 * Copyright 2023 HM Revenue & Customs
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

import akka.stream.testkit.NoMaterializer
import uk.gov.hmrc.apirevocationfrontend.config.{FooterConfig, FrontendAppConfig}
import play.api.http.{DefaultFileMimeTypes, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import play.api.test.Helpers.{stubBodyParser, stubPlayBodyParsers}
import play.api.test.{CSRFTokenHelper, FakeRequest, StubMessagesFactory}

import scala.concurrent.ExecutionContext

trait Stubs extends StubMessagesFactory {

  val stubbedMessagesApi: MessagesApi = stubMessagesApi(Map(
    "en" -> Map(
      "global.error.InternalServerError500.title"   -> "We’re experiencing technical difficulties",
      "global.error.InternalServerError500.heading" -> "We’re experiencing technical difficulties",
      "global.error.InternalServerError500.message" -> "Please try again in a few minutes."
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

  val minimalAppConfig: FrontendAppConfig = new FrontendAppConfig(
    analyticsToken = "",
    analyticsHost = "",
    betaFeedbackUrl = "",
    betaFeedbackUnauthenticatedUrl = "",
    reportAProblemPartialUrl = "",
    reportAProblemNonJSUrl = "",
    reportProblemHost = "",
    signInUrl = "/gg/sign-in?continue=/applications-manage-authority/applications",
    signOutUrl = ""
  )

  val minimalFooterConfig: FooterConfig = FooterConfig(
    apiDocumentationFrontendUrl = "",
    platformFrontendHost = "",
    thirdPartyDeveloperFrontendUrl = ""
  )
}

object FakeRequestCSRFSupport {

  implicit class CSRFFakeRequest[A](request: FakeRequest[A]) {
    def withCSRFToken: Request[A] = CSRFTokenHelper.addCSRFToken(request)
  }
}
