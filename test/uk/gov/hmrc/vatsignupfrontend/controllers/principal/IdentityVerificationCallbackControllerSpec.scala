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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreIdentityVerificationHttpParser.{IdentityVerificationFailure, IdentityVerified}
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{LimitedCompany, Other, SoleTrader}
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreIdentityVerificationService
import uk.gov.hmrc.vatsignupfrontend.Constants.skipIvJourneyValue

import scala.concurrent.Future

class IdentityVerificationCallbackControllerSpec extends UnitSpec with GuiceOneAppPerSuite
  with MockControllerComponents with MockStoreIdentityVerificationService {

  object TestIdentityVerificationCallbackController extends IdentityVerificationCallbackController(
    mockControllerComponents,
    mockStoreIdentityVerificationService
  )

  "Calling the continue action of the Identity Verification call back controller" when {
    "there is a VAT number, business entity and Identity Verification continue url in session" when {
      "the service returns IdentityVerified" should {
        "return a redirect to the identity verification success controller" when {
          "when confidence level is below 200" in {
            mockAuthAdminRole()
            mockStoreIdentityVerification(testVatNumber, testUri)(Future.successful(Right(IdentityVerified)))

            val request = FakeRequest() withSession(
              vatNumberKey -> testVatNumber,
              businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader),
              identityVerificationContinueUrlKey -> testUri
            )

            val result = await(TestIdentityVerificationCallbackController.continue(request))
            status(result) shouldBe Status.SEE_OTHER
            redirectLocation(result) should contain(routes.IdentityVerificationSuccessController.show().url)
          }
        }

        "return a redirect to the agree to receive emails controller" when {
          "business Entity is Sole Trader" when {
            "when confidence level is 200 or above" in {
              mockAuthAdminRole()
              mockStoreIdentityVerification(testVatNumber, skipIvJourneyValue)(Future.successful(Right(IdentityVerified)))

              val request = FakeRequest() withSession(
                vatNumberKey -> testVatNumber,
                businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader),
                identityVerificationContinueUrlKey -> skipIvJourneyValue
              )

              val result = await(TestIdentityVerificationCallbackController.continue(request))
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.AgreeCaptureEmailController.show().url)
            }
          }
        }

        "return a redirect to the capture company number controller" when {
          "business Entity is Limited Company" when {
            "when confidence level is 200 or above" in {
              mockAuthAdminRole()
              mockStoreIdentityVerification(testVatNumber, skipIvJourneyValue)(Future.successful(Right(IdentityVerified)))

              val request = FakeRequest() withSession(
                vatNumberKey -> testVatNumber,
                businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedCompany),
                identityVerificationContinueUrlKey -> skipIvJourneyValue
              )

              val result = await(TestIdentityVerificationCallbackController.continue(request))
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CaptureCompanyNumberController.show().url)
            }
          }
        }

        "return a redirect to the capture business entity controller" when {
          "business Entity is Other" when {
            "when confidence level is 200 or above" in {
              mockAuthAdminRole()
              mockStoreIdentityVerification(testVatNumber, skipIvJourneyValue)(Future.successful(Right(IdentityVerified)))

              val request = FakeRequest() withSession(
                vatNumberKey -> testVatNumber,
                businessEntityKey -> BusinessEntitySessionFormatter.toString(Other),
                identityVerificationContinueUrlKey -> skipIvJourneyValue
              )

              val result = await(TestIdentityVerificationCallbackController.continue(request))
              status(result) shouldBe Status.SEE_OTHER
              redirectLocation(result) should contain(routes.CaptureBusinessEntityController.show().url)
            }
          }
        }
      }

      "the service returns a failure" should {
        "go to failed identity verification" in {
          mockAuthAdminRole()
          mockStoreIdentityVerification(testVatNumber, testUri)(Future.successful(Left(IdentityVerificationFailure)))

          val request = FakeRequest() withSession(
            vatNumberKey -> testVatNumber,
            businessEntityKey -> BusinessEntitySessionFormatter.toString(LimitedCompany),
            identityVerificationContinueUrlKey -> testUri
          )

          val result = await(TestIdentityVerificationCallbackController.continue(request))
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) should contain(routes.FailedIdentityVerificationController.show().url)
        }
      }
    }
    "there is no VAT number in session" should {
      "redirect to the vat number controller" in {
        mockAuthAdminRole()
        val result = await(TestIdentityVerificationCallbackController.continue(FakeRequest()))

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.YourVatNumberController.show().url)
      }
    }

    "there is no business entity in session" should {
      "redirect to the capture business entity" in {
        mockAuthAdminRole()
        val result = await(
          TestIdentityVerificationCallbackController
            .continue(FakeRequest() withSession (vatNumberKey -> testVatNumber))
        )

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.CaptureBusinessEntityController.show().url)
      }
    }

    "there is no Identity Verification continue url in session" should {
      "redirect to capture your details controller" in {
        mockAuthAdminRole()
        val result = await(
          TestIdentityVerificationCallbackController
            .continue(FakeRequest() withSession(
              vatNumberKey -> testVatNumber,
              identityVerificationContinueUrlKey -> testUri
            ))
        )

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.CaptureBusinessEntityController.show().url)
      }
    }
  }

}
