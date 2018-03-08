/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatsubscriptionfrontend.httpparsers

import play.api.http.Status.{BAD_REQUEST, CREATED}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsubscriptionfrontend.httpparsers.IVProxyHttpParser._

class IVProxyHttpParserSpec extends UnitSpec {
  val testHttpVerb = "POST"
  val testUri = "/"

  "IVProxyHttpReads" when {
    "read" should {
      "parse a CREATED response as an IVSuccessResponse" in {
        val httpResponse = HttpResponse(CREATED, Some(
          Json.parse(
            """{
              |  "link": "/some-journey-start-url",
              |  "journeyLink": "/some-journey-status-url"
              |}""".stripMargin('|')))
        )

        val res = IVProxyHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Right(IVSuccessResponse("/some-journey-start-url", "/some-journey-status-url"))
      }

      "parse any other response as an IVFailureResponse" in {
        val httpResponse = HttpResponse(BAD_REQUEST, Some(Json.obj()))

        val res = IVProxyHttpReads.read(testHttpVerb, testUri, httpResponse)

        res shouldBe Left(IVFailureResponse(httpResponse.status))
      }
    }
  }
}
