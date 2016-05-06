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

package models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json.Json

case class Scope(key: String, name: String, description: String)

object Scope {
  implicit val format = Json.format[Scope]
}

case class ThirdPartyApplication(id: UUID, name: String, trusted: Boolean)

object ThirdPartyApplication {
  implicit val format = Json.format[ThirdPartyApplication]
}

case class AppAuthorisation(application: ThirdPartyApplication,
                            scopes: Set[Scope],
                            earliestGrantDate: DateTime)

object AppAuthorisation {
  implicit val format = Json.format[AppAuthorisation]
  implicit val ordering: Ordering[AppAuthorisation] = Ordering.by(_.application.name)
}

case class ApplicationDetails(id: UUID, name: String)

object ApplicationDetails {
  implicit val format = Json.format[ApplicationDetails]
}
