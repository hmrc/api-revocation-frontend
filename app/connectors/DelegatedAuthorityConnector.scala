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

package connectors

import java.util.UUID

import config.WSHttp
import models.AppAuthorisation
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpDelete, HttpGet, HttpPost}
import scala.concurrent.ExecutionContext.Implicits.global

trait DelegatedAuthorityConnector {

  val delegatedAuthorityUrl: String
  val http: HttpPost with HttpGet with HttpDelete

  def fetchApplicationAuthorities()(implicit hc: HeaderCarrier) = {
    http.GET[Seq[AppAuthorisation]](s"$delegatedAuthorityUrl/authority/granted-applications")
  }

  def revokeApplicationAuthority(applicationId: UUID)(implicit hc: HeaderCarrier) = {
    val url = s"$delegatedAuthorityUrl/authority/granted-application/$applicationId"
    http.DELETE(url)
      .recover {
        case e: Throwable => throw DelegatedAuthorityMicroserviceException(url, e)
      }
  }
}

case class DelegatedAuthorityMicroserviceException(url: String, t: Throwable) extends RuntimeException(s"Unable to revoke application authority for $url", t)

object DelegatedAuthorityConnector extends DelegatedAuthorityConnector with ServicesConfig {
  override val delegatedAuthorityUrl = s"${baseUrl("third-party-delegated-authority")}"
  override val http = WSHttp
}