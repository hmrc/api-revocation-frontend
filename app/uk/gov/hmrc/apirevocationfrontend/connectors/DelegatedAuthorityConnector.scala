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

package uk.gov.hmrc.apirevocationfrontend.connectors

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future.*
import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import uk.gov.hmrc.apirevocationfrontend.models.AppAuthorisation

@Singleton
class DelegatedAuthorityConnector @Inject() (servicesConfig: ServicesConfig, http: HttpClientV2)(implicit val ec: ExecutionContext) {

  val delegatedAuthorityUrl: String = servicesConfig.baseUrl("third-party-delegated-authority")

  def fetchApplicationAuthorities()(implicit hc: HeaderCarrier): Future[Seq[AppAuthorisation]] = {
    http.get(url"$delegatedAuthorityUrl/authority/granted-applications").execute[Seq[AppAuthorisation]]
  }

  def fetchApplicationAuthority(applicationId: UUID)(implicit hc: HeaderCarrier): Future[AppAuthorisation] = {
    http.get(url"$delegatedAuthorityUrl/authority/granted-application/$applicationId").execute[Option[AppAuthorisation]].flatMap(handleNotFound)
  }

  def revokeApplicationAuthority(applicationId: UUID)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.delete(url"$delegatedAuthorityUrl/authority/granted-application/$applicationId").execute[Option[Unit]].flatMap(handleNotFound)
  }

  def handleNotFound[T](o: Option[T]): Future[T] = {
    o.fold[Future[T]](failed(new AuthorityNotFound))(t => successful(t))
  }
}

class AuthorityNotFound extends RuntimeException
