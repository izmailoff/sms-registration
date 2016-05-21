package com.smsreg.user.avatar

import com.github.izmailoff.logging.AkkaLoggingHelper
import com.smsreg.db.crud.DbCrudProvider
import com.smsreg.db.datamodel.Avatar
import net.liftweb.common.{Full, Box}
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._

trait UserAvatarApi {

  def saveAvatar(avatar: UserAvatar): Box[Avatar]

  def getAvatar(userId: ObjectId): Box[Avatar]

  def genAvatarUrl(avatar: Avatar): String
}

trait UserAvatarApiImpl
  extends UserAvatarApi
  with AkkaLoggingHelper {
  this: DbCrudProvider =>

  import log._

  override def saveAvatar(avatar: UserAvatar): Box[Avatar] = {
    import avatar._
    debug(s"Saving avatar for userId: [${avatar.userId}], N bytes: [${avatar.imageBytes.size}].")
    // TODO: validation of min/max size, byte mark or whatever
    // FIXME: this does not work for now - maybe limited support. Doing 2 queries for now
    //    AVATARS.where(_.userId eqs userId)
    //      .modify(_.image setTo imageBytes)
    //      .upsertOne()
    AVATARS.where(_.userId eqs userId)
      .findAndDeleteOne()
    for {
      newAvatar <- AVATARS.createRecord
        .image(imageBytes)
        .userId(userId)
        .saveTheRecord()
      _ = USERS.where(_.id eqs userId)
        .modify(_.avatarUrl setTo genAvatarUrl(newAvatar))
        .updateOne()
    } yield newAvatar
  }

  override def getAvatar(userId: ObjectId): Box[Avatar] =
    AVATARS.where(_.userId eqs userId)
      .fetch(1).headOption

  //.getOrElse(AVATARS.createRecord.userId(userId))

  override def genAvatarUrl(avatar: Avatar): String =
    s"/api/users/${avatar.userId.get.toString}/avatar/${avatar.id.get.toString}.jpeg"
}
