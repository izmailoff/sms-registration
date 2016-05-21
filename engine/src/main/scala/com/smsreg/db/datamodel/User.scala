package com.smsreg.db.datamodel

import com.foursquare.index.IndexedRecord
import com.github.izmailoff.mongo.connection.MongoConfig
import com.smsreg.db.constants.CollectionNames
import net.liftweb.mongodb.BsonDSL._
import net.liftweb.mongodb.record.field.{DateField, ObjectIdPk}
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.{IntField, BooleanField, EmailField, StringField}
import org.joda.time.DateTime

abstract class User
  extends MongoRecord[User]
  with ObjectIdPk[User]
  with IndexedRecord[User] {

  object username extends StringField(this, 40)

  object deviceId extends StringField(this, 120)

  // TODO: do we need both phone and device id? - how is it unique and secure TBD
  object phoneNumber extends StringField(this, 20)

  object fullName extends StringField(this, 128)

  object email extends EmailField(this, 128)

  object pinGenTime extends DateField(this) {
    override def defaultValue = DateTime.now.toDate
  }

  object pin extends IntField(this)
  //TODO: add expiry date for the pin
  //TODO: add number of activation attempts or event log
  /**
   * If user account is deactivated the value will be false and user can't perform any actions.
   */
  object isActive extends BooleanField(this) {
    override def defaultValue = false
  }

  /**
   * URL of the latest uploaded avatar or empty String if never uploaded.
   */
  object avatarUrl extends StringField(this, 128)
}

trait UserMeta
  extends User
  with MongoMetaRecord[User] {
  self: MongoMetaRecord[User] =>

  override def meta = self

  override protected def instantiateRecord: User =
    new User {
      override val meta = self
    }

  override def collectionName = CollectionNames.USERS.toString

  if (MongoConfig.isConnected(mongoIdentifier)) {
    ensureIndex(username.name -> 1)
    ensureIndex(email.name -> 1)
  }
}