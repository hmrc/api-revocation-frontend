/*
 * Copyright 2020 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{MappingBuilder, ResponseDefinitionBuilder, WireMock}
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import com.github.tomakehurst.wiremock.stubbing.StubMapping

trait WireMockHelper {

  def verifyExactlyOne(request: RequestPatternBuilder): Unit =
    verify(WireMock.exactly(1), request)

  def stubPut(uri: String, request: String, response: ResponseDefinitionBuilder): StubMapping =
    stub(put(urlPathEqualTo(uri)).withRequestBody(equalToJson(request)), response)

  def stubPost(uri: String, response: ResponseDefinitionBuilder): StubMapping =
    stub(post(urlPathEqualTo(uri)), response)

  def stubPatch(uri: String, response: ResponseDefinitionBuilder): StubMapping =
    stub(patch(urlPathEqualTo(uri)), response)

  def stubGet(uri: String, body: String): StubMapping =
    stub(get(urlPathEqualTo(uri)), okJson(body))

  private def stub(method: MappingBuilder, response: ResponseDefinitionBuilder): StubMapping =
    stubFor(method.willReturn(response))
}