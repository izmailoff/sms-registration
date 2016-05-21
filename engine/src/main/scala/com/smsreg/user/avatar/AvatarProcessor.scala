package com.smsreg.user.avatar

import akka.actor.{Actor, Props, Status}
import akka.event.LoggingReceive
import akka.pattern.pipe
import com.github.izmailoff.mongo.connection.DefaultDbConnectionIdentifier
import com.smsreg.db.crud.DbCrudProviderImpl
import com.smsreg.db.datamodel.Avatar
import com.smsreg.user.registration.RegistrationProcessor
import net.liftweb.common.Full
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.bson.types.ObjectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// FIXME: having 2 args like that is ugly, will be fixed later - just a quick hack to make things work
class AvatarProcessor(save: JValue => Unit, fetch: UserAvatar => Unit)
  extends Actor
  with DefaultDbConnectionIdentifier
  with DbCrudProviderImpl
  with UserAvatarApiImpl {

  import log._

  val globalSystem = context.system

  def receive = LoggingReceive {
    case avatar@UserAvatar(userId, imageBytes) =>
      Future {
        saveAvatar(avatar)
      } pipeTo self
      context.become(savingAvatar(userId, imageBytes.size))
    case GetAvatar(userId) =>
      Future {
        getAvatar(userId)
      } pipeTo self
      context.become(fetchingAvatar(userId))
  }

  def savingAvatar(userId: ObjectId, size: Int): Receive = LoggingReceive {
    case Full(avatar: Avatar) =>
      info(s"Updated user avatar for userId: [$userId].")
      val response = RegistrationProcessor.SUCCESS ~
        ("size" -> size) ~
        ("url" -> genAvatarUrl(avatar))
      save(response)
      context.stop(self)
    case Status.Failure(cause) =>
      error(cause, s"Failed to update avatar for userId: [$userId].")
      save(RegistrationProcessor.FAILURE ~ ("size" -> size))
      context.stop(self)
  }

  def fetchingAvatar(userId: ObjectId): Receive = LoggingReceive {
    case Full(avatar: Avatar) =>
      val size = avatar.image.get.size
      info(s"Fetched user avatar for userId: [$userId], bytes size: [$size].")
      fetch(UserAvatar(avatar.userId.get, avatar.image.get))
      context.stop(self)
    case Status.Failure(cause) =>
      error(cause, s"Failed to fetch avatar for userId: [$userId].")
      save(RegistrationProcessor.FAILURE ~ ("size" -> 0))
      context.stop(self)
  }
}

object AvatarProcessor {
  def props(save: JValue => Unit, fetch: UserAvatar => Unit): Props =
    Props(new AvatarProcessor(save, fetch))
}