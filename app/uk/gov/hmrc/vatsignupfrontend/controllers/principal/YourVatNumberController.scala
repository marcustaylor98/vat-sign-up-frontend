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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.KnownFactsJourney
import uk.gov.hmrc.vatsignupfrontend.config.{AppConfig, ControllerComponents}
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreVatNumberHttpParser._
import uk.gov.hmrc.vatsignupfrontend.services.StoreVatNumberService
import uk.gov.hmrc.vatsignupfrontend.utils.EnrolmentUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.your_vat_number

import scala.concurrent.Future

@Singleton
class YourVatNumberController @Inject()(val controllerComponents: ControllerComponents,
                                        val storeVatNumberService: StoreVatNumberService)
  extends AuthenticatedController(AdministratorRolePredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised()(Retrievals.allEnrolments) { enrolments =>
      enrolments.vatNumber match {
        case Some(vatNumber) =>
          Future.successful(Ok(your_vat_number(vatNumber, routes.YourVatNumberController.submit())))
        case None if appConfig.isEnabled(KnownFactsJourney) =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
        case None =>
          Future.successful(
            Redirect(routes.CannotUseServiceController.show())
          )
      }
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised()(Retrievals.allEnrolments) { enrolments =>
      enrolments.vatNumber match {
        case Some(vatNumber) =>
          storeVatNumberService.storeVatNumber(vatNumber) map {
            case Right(StoreVatNumberSuccess) =>
              Redirect(routes.CaptureBusinessEntityController.show())
                .addingToSession(SessionKeys.vatNumberKey -> vatNumber)
            case Left(AlreadySubscribed) => Redirect(routes.AlreadySignedUpController.show())
            case Left(IneligibleVatNumber) => Redirect(routes.CannotUseServiceController.show())
            case Left(_) =>
              throw new InternalServerException("storeVatNumber failed")
          }
        case None if appConfig.isEnabled(KnownFactsJourney) =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
        case None =>
          Future.successful(
            Redirect(routes.CannotUseServiceController.show())
          )
      }
    }
  }

}
