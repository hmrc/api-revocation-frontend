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

package controllers

import play.api.mvc.Results
import uk.gov.hmrc.play.frontend.auth._
import config.FrontendAppConfig

trait Authentication extends Actions {
  def authenticated = AuthenticatedBy(ValidSessionCredentialsProvider, pageVisibility = GGConfidence)
}

object ValidSessionCredentialsProvider extends AnyAuthenticationProvider with Results {

  private lazy val loginUrl = FrontendAppConfig.signInUrl
  private lazy val continueUrl = FrontendAppConfig.continueUrl

  override def ggwAuthenticationProvider: GovernmentGateway = new GovernmentGateway {
    override def loginURL = loginUrl
    override def continueURL = continueUrl
  }
  override def verifyAuthenticationProvider: Verify  = new Verify {
    override def login = loginUrl
  }

  override def login =  loginUrl
}
