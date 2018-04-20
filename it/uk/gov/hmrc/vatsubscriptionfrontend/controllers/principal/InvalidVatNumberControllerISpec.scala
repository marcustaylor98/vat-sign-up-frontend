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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers.principal

import play.api.http.Status._
import uk.gov.hmrc.http.{InternalServerException, NotFoundException}
import uk.gov.hmrc.vatsubscriptionfrontend.config.featureswitch.{FeatureSwitching, KnownFactsJourney}
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.{ComponentSpecBase, CustomMatchers}
class InvalidVatNumberControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching{

  enable(KnownFactsJourney)

  "GET /could-not-confirm-vat-number" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/could-not-confirm-vat-number")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /could-not-confirm-vat-number" should {
    "return an NOT_IMPLEMENTED" in {
      stubAuth(OK, successfulAuthResponse())

      val res = post("/could-not-confirm-vat-number")()

      res should have(
        httpStatus(NOT_IMPLEMENTED)
      )
    }
  }


  "Making a request to /could-not-confirm-vat-number when not enabled" should {
    "return NotFound" in {
      disable(KnownFactsJourney)

      stubAuth(OK, successfulAuthResponse())

      val res = get("/could-not-confirm-vat-number")

      res should have(
        httpStatus(NOT_FOUND)
      )

    }
  }
}