package com.smsreg.communication.sms

import com.github.izmailoff.logging.AkkaLoggingHelper
import com.smsreg.config.GlobalAppConfig.Application.Twilio._
import com.smsreg.utils.ConditionalExecution
import net.liftweb.common.{Box, Full}
import net.liftweb.util.Props.RunModes
import org.apache.http.message.BasicNameValuePair

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}


trait Twilio
  extends AkkaLoggingHelper {

  import com.twilio.sdk._
  import log._

  def sendMessage(phoneNumber: String, text: String): Box[Unit] =
    Try {
      ConditionalExecution.runModeExecute((), sendDevMode(phoneNumber, text), sendProdMode(phoneNumber, text))
    }
    match {
      case Success(_) =>
        Full(())
      case Failure(cause) =>
        error(s"Failed to send SMS to phone [$phoneNumber], caused by: [${cause.toString}].")
        net.liftweb.common.Failure("SMS was not sent.")
    }

  private def sendDevMode(phoneNumber: String, text: String): PartialFunction[RunModes.Value, Unit] = {
    case RunModes.Development =>
      info(s"NOT sending SMS to client because we are in DEVELOPMENT MODE! Phone: [$phoneNumber], text: [$text].")
  }

  private val FROM = "From"
  private val TO = "To"
  private val BODY = "Body"

  private val messageFactory = {
    val client = new TwilioRestClient(accountSid, authToken)
    client.setNumRetries(retries)
    client.getAccount.getMessageFactory
  }

  // TODO: consider potential improvement: provide callback REST API so that it does not block during 3rd party request
  // see more here: https://www.twilio.com/docs/api/rest/sending-sms

  private def sendProdMode(phoneNumber: String, text: String): PartialFunction[RunModes.Value, Unit] = {
    case RunModes.Production =>
      info(s"SENDING SMS to client because we are in PRODUCTION MODE! Phone: [$phoneNumber], text: [$text].")
      val params = List(new BasicNameValuePair(FROM, from),
        new BasicNameValuePair(TO, phoneNumber),
        new BasicNameValuePair(BODY, text))
      val message = messageFactory.create(params)
      info(s"Message with SID [${message.getSid}] to phone [$phoneNumber] has been sent successfully.")
  }

}
