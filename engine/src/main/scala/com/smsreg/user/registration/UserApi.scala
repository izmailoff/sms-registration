package com.smsreg.user.registration

import java.util.Date

import com.foursquare.rogue.LiftRogue._
import com.github.izmailoff.logging.AkkaLoggingHelper
import com.smsreg.communication.sms.Twilio
import com.smsreg.config
import com.smsreg.config.GlobalAppConfig.Application.Settings._
import com.smsreg.db.crud.DbCrudProvider
import com.smsreg.db.datamodel.User
import net.liftweb.common.{Failure, Box, Full}
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._

trait UserApi {

  def registerUser(request: RegistrationRequest): Box[Int]

  def verifyUser(request: RegistrationValidation): Box[ObjectId]
}

trait UserApiImpl
  extends UserApi
  with AkkaLoggingHelper {
  this: DbCrudProvider =>

  import log._

  override def registerUser(request: RegistrationRequest): Box[Int] = {
    debug(s"Received user registration request: $request.")
    val pin = generatePin
    for {
      _ <- createIfNotExists(request, pin)
      _ = info(s"Created user profile with properties: $request.")
    } yield pin
  }

  private def generatePin = 1000 + scala.util.Random.nextInt(8999)

  private def createIfNotExists(request: RegistrationRequest, pin: Int): Box[Unit] = {
    import request._
    Full(USERS.where(_.deviceId eqs deviceId)
      .modify(_.deviceId setOnInsertTo deviceId)
      .modify(_.phoneNumber setOnInsertTo phoneNumber)
      .modify(_.isActive setOnInsertTo false)
      .modify(_.pin setTo pin)
      .modify(_.pinGenTime setTo DateTime.now.toDate)
      .upsertOne()) // TODO: check write concern
  }

  override def verifyUser(request: RegistrationValidation): Box[ObjectId] = {
    import request._
    val errMsg = Failure("Verification failed.")

    def findUserByDevice: Box[User] =
      USERS.where(_.deviceId eqs deviceId)
        .limit(1)
        .fetch()
        .headOption

//    def checkNotActivatedAlready(user: User) =
//      if(user.isActive.get) Failure("Already activated") else Full(())

    def checkPinMatch(dbPin: Int) =
      if (dbPin == pin) Full(()) else errMsg

    def checkPinExpiry(creationTime: Date) =
      if ((new DateTime(creationTime).plus(pinExpireDuration.toMillis)) isAfterNow)
        Full(())
      else Failure("PIN expired.")

    for {
      user <- findUserByDevice or errMsg
//      _ <- checkNotActivatedAlready(user)
      _ <- checkPinMatch(user.pin.get)
      _ <- checkPinExpiry(user.pinGenTime.get)
      _ = user.isActive(true).update
    } yield user.id.get
  }

}
