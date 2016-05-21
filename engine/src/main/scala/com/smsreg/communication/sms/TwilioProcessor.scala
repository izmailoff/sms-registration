package com.smsreg.communication.sms

import akka.actor.Actor
import akka.event.LoggingReceive

class TwilioProcessor
  extends Actor
  with Twilio {

  val globalSystem = context.system

  def receive = LoggingReceive {
    case SMS(phone, msg) =>
      sender ! sendMessage(phone, msg)
  }

}
