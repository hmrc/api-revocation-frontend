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

package service

import java.util.UUID

import connectors.DelegatedAuthorityConnector
import javax.inject.{Inject, Singleton}
import models.AppAuthorisation
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RevocationService @Inject()(val delegatedAuthorityConnector: DelegatedAuthorityConnector)
                                 (implicit val ec: ExecutionContext) {

  def fetchUntrustedApplicationAuthorities()(implicit hc: HeaderCarrier): Future[Seq[AppAuthorisation]] = {
    delegatedAuthorityConnector.fetchApplicationAuthorities().map { authorities =>
      authorities.filter(!_.application.trusted).sorted
    }
  }

  def fetchUntrustedApplicationAuthority(appId: UUID)(implicit hc: HeaderCarrier): Future[AppAuthorisation] = {
    delegatedAuthorityConnector.fetchApplicationAuthority(appId).map {
      case authority if authority.application.trusted => throw TrustedAuthorityRetrievalException(appId)
      case authority => authority
    }
  }

  def revokeApplicationAuthority(appId: UUID)(implicit hc: HeaderCarrier): Future[Unit] = {
    delegatedAuthorityConnector.fetchApplicationAuthority(appId).flatMap {
      case authority if authority.application.trusted => throw TrustedAuthorityRevocationException(appId)
      case _ => delegatedAuthorityConnector.revokeApplicationAuthority(appId)
    }
  }
}

case class TrustedAuthorityRetrievalException(appId: UUID)
  extends RuntimeException(s"Authority for application [$appId] was found, but the application is trusted")

case class TrustedAuthorityRevocationException(appId: UUID)
  extends RuntimeException(s"Authority for application [$appId] cannot be revoked as the application is trusted")
