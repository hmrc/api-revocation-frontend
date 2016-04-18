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

package acceptance.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{Scope, AppAuthorisation}

object DelegatedAuthorityStub {
  def stubSuccessfulFetchApplicationAuthorities(applications: Seq[AppAuthorisation]) = {

    def toScopeJson(scope: Scope) =
      s"""
         |{
         |  "key":"${scope.key}",
         |  "name":"${scope.name}",
         |  "description":"${scope.description}"
         |}
         """.stripMargin

    def toAppJson(application: AppAuthorisation) = {
      s"""
         |{
         |  "application" : {
         |    "id":"${application.application.id}",
         |    "name":"${application.application.name}"
         |    },
         |  "scopes": [${application.scopes.map(toScopeJson).mkString(",")}],
         |  "earliestGrantDate":${application.earliestGrantDate.getMillis}
         |}
       """.stripMargin
    }

    stubFor(get(urlEqualTo(s"/authority/granted-applications")).willReturn(
      aResponse().withStatus(200).withBody(
        s"""[${applications.map(toAppJson).mkString(",")}]""".stripMargin)))
  }
}
