/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import java.util.UUID

import config.FrontendAppConfig
import connectors.AuthorityNotFound
import javax.inject.{Inject, Singleton}
import play.api.i18n.Messages
import play.api.mvc._
import play.twirl.api.Html
import service.RevocationService
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.error_template
import views.html.revocation._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Revocation @Inject()(override val authConnector: AuthConnector,
                           val revocationService: RevocationService,
                           mcc: MessagesControllerComponents,
                           frontendAppConfig: FrontendAppConfig,
                           error_template: error_template,
                           startPage: start,
                           loggedOutPage: loggedOut,
                           authorizedApplicationsPage: authorizedApplications,
                           permissionWithdrawnPage: permissionWithdrawn,
                           withdrawPermissionPage: withdrawPermission)
                          (implicit val ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with play.api.i18n.I18nSupport {

  private lazy val loginURL: String = frontendAppConfig.signInUrl
  private lazy val loginUrlParameters = Map[String, Seq[String]]()

  private def notFoundTemplate(implicit request: Request[_]): Html = {
    error_template(
      Messages("global.error.pageNotFound404.title"),
      Messages("global.error.pageNotFound404.heading"),
      Messages("global.error.pageNotFound404.message"))
  }

  private def unauthorisedRecovery: PartialFunction[Throwable, Result] = {
    case _: AuthorisationException => Redirect(loginURL, loginUrlParameters)
  }

  val start: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(startPage()))
  }

  val loggedOut: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(loggedOutPage()))
  }

  val listAuthorizedApplications: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      revocationService.fetchApplicationAuthorities() map {
        applications => Ok(authorizedApplicationsPage(applications))
      }
    } recover unauthorisedRecovery
  }

  def withdrawPage(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      revocationService.fetchdApplicationAuthority(id) map {
        authority => Ok(withdrawPermissionPage(authority))
      } recover {
        case _: AuthorityNotFound => NotFound(notFoundTemplate)
      }
    } recover unauthorisedRecovery
  }

  def withdrawAction(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      revocationService.revokeApplicationAuthority(id) map {
        _ => Redirect(routes.Revocation.withdrawConfirmationPage())
      } recover {
        case _: AuthorityNotFound => NotFound(notFoundTemplate)
      }
    } recover unauthorisedRecovery
  }

  val withdrawConfirmationPage: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(Ok(permissionWithdrawnPage()))
    } recover unauthorisedRecovery
  }
}
