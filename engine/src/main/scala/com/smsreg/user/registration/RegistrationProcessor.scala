package com.smsreg.user.registration

import akka.actor.{Actor, ActorRef, Props, Status}
import akka.event.LoggingReceive
import akka.pattern.pipe
import com.github.izmailoff.mongo.connection.DefaultDbConnectionIdentifier
import com.smsreg.communication.sms.SMS
import com.smsreg.db.crud.DbCrudProviderImpl
import net.liftweb.common.{Box, Full}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.bson.types.ObjectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.smsreg.utils.ErrorHandlingHelper._


/**
 * Creates an empty user profile and sends a verification message
 * to the user through [[com.smsreg.communication.sms.Twilio]] to verify account.
 * Every subsequent request is treated as user attempt to reinitiate registration
 * and thus generates a new pin which is sent to the user.
 *
 * Request from the app is trusted, i.e. things like device ID, phone
 *                number, etc. can't be validated on the server side and have to be
 *                correctly provided by the client app.
 */
class RegistrationProcessor(twilioHandler: ActorRef, requestCompleter: JValue => Unit)
  extends Actor
  with DefaultDbConnectionIdentifier
  with DbCrudProviderImpl
  with UserApiImpl {
  import com.smsreg.user.registration.RegistrationProcessor._
  import log._

  val globalSystem = context.system

  def receive = LoggingReceive {
    case req: RegistrationRequest =>
      Future { registerUser(req) } pipeTo self
      context.become(waitingForProfile(req))
    case req: RegistrationValidation =>
      Future { verifyUser(req) } pipeTo self
      context.become(waitingForVerification(req))
  }

  def waitingForVerification(request: RegistrationValidation): Receive = LoggingReceive {
    case Full(userId: ObjectId) =>
      requestCompleter(SUCCESS ~ ("userId" -> userId.toString))
      context.stop(self)
    case failure: Box[_] =>
      requestCompleter(FAILURE ~ ("msg" -> getError(failure)))
      context.stop(self)
  }

  def waitingForProfile(request: RegistrationRequest): Receive = LoggingReceive {
    case Full(pin: Int) =>
      twilioHandler ! SMS(request.phoneNumber, registrationMessage(pin))
      context.become(waitingForSms(request))
    case Status.Failure(cause) =>
      error(cause, s"Failed to process user registration for $request.")
      requestCompleter(FAILURE)
      context.stop(self)
  }

  def waitingForSms(request: RegistrationRequest): Receive = LoggingReceive {
    case res@Full(()) =>
      info(s"Finished processing user registration for $request.")
      requestCompleter(SUCCESS)
      context.stop(self)
    case _ =>
      error(s"Failed to process user registration for $request.")
      requestCompleter(FAILURE)
      context.stop(self)
  }

  // FIXME: maybe it does not belong here:
  def registrationMessage(pin: Int) =
    s"[$pin] is your activation code."

}

object RegistrationProcessor {
  def props(twilioHandler: ActorRef, requestCompleter: JValue => Unit): Props =
    Props(new RegistrationProcessor(twilioHandler, requestCompleter))

  // TODO: these will have to go, see BoxMarshalling...
  val SUCCESS: JObject = ("status" -> "SUCCESSFUL")
  val FAILURE: JObject = ("status" -> "FAILED")
}