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

package uk.gov.hmrc.apirevocationfrontend.config

import javax.inject.{Inject, Provider, Singleton}

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

case class FooterConfig(apiDocumentationFrontendUrl: String, platformFrontendHost: String, thirdPartyDeveloperFrontendUrl: String)

@Singleton
class FooterConfigProvider @Inject() (config: ServicesConfig) extends Provider[FooterConfig] {

  override def get(): FooterConfig = {
    lazy val apiDocumentationFrontendUrl    = config.baseUrl("api-documentation-frontend")
    lazy val platformFrontendHost           = config.getString("platform.frontend.host")
    lazy val thirdPartyDeveloperFrontendUrl = config.baseUrl("third-party-developer-frontend")

    FooterConfig(
      apiDocumentationFrontendUrl,
      platformFrontendHost,
      thirdPartyDeveloperFrontendUrl
    )
  }
}
