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

package uk.gov.hmrc.vatsignupfrontend.forms

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import uk.gov.hmrc.vatsignupfrontend.forms.EmailForm._
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants.testEmail

class EmailFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  "The emailForm" should {

    val error_key = "error.invalid_email"

    val maxlength_error_key = "error.exceeds_max_length_email"

    "validate that data containing a valid email passes" in {
      val actual = emailForm.bind(Map(email -> testEmail)).value
      actual shouldBe Some(testEmail)
    }

    "validate that data has been entered" in {
      val formWithError = emailForm.bind(Map(email -> ""))
      formWithError.errors should contain(FormError(email, error_key))
    }

    "validate that invalid email fails" in {
      val formWithError = emailForm.bind(Map(email -> "invalid"))
      formWithError.errors should contain(FormError(email, error_key))
    }

    "validate that email does not exceed max length" in {
      val exceed = emailForm.bind(Map(email -> ("a" * (MaxLengthEmail + 1)))).errors
      exceed should contain(FormError(email, maxlength_error_key))
      exceed.seq.size shouldBe 1
    }

    "validate that email allows max length" in {
      val errors = emailForm.bind(Map(email -> ("a" * MaxLengthEmail))).errors
      errors should not contain FormError(email, maxlength_error_key)
    }


  }
}