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

package connectors

import java.util.UUID

import config.WSHttp
import javax.inject.{Inject, Singleton}
import models.AppAuthorisation
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.inject.DefaultServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class DelegatedAuthorityConnector @Inject()(servicesConfig: DefaultServicesConfig, http: WSHttp){

  val delegatedAuthorityUrl: String = servicesConfig.baseUrl("third-party-delegated-authority")

  def fetchApplicationAuthorities()(implicit hc: HeaderCarrier): Future[Seq[AppAuthorisation]] = {
    http.GET[Seq[AppAuthorisation]](s"$delegatedAuthorityUrl/authority/granted-applications")
  }

  def fetchApplicationAuthority(applicationId: UUID)(implicit hc: HeaderCarrier): Future[AppAuthorisation] = {
    http.GET[AppAuthorisation](s"$delegatedAuthorityUrl/authority/granted-application/$applicationId") recover recovery
  }

  def revokeApplicationAuthority(applicationId: UUID)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.DELETE(s"$delegatedAuthorityUrl/authority/granted-application/$applicationId") map (_ => ()) recover recovery
  }

  private def recovery: PartialFunction[Throwable, Nothing] = {
    case _: NotFoundException => throw new AuthorityNotFound
  }
}

class AuthorityNotFound extends RuntimeException
