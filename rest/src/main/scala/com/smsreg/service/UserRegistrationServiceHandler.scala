package com.example.service

import akka.actor.{Props, Actor}
import com.github.izmailoff.mongo.connection.DefaultDbConnectionIdentifier
import com.smsreg.communication.sms.TwilioProcessor

class UserRegistrationServiceHandler
  extends Actor
  with UserRegistrationServiceImpl
  with DefaultDbConnectionIdentifier
{

  def actorRefFactory = context

  def receive = runRoute(route)

  val globalSystem = actorRefFactory.system
}
