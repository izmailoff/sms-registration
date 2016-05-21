package com.smsreg.db.datamodel

import com.foursquare.index.IndexedRecord
import com.github.izmailoff.mongo.connection.MongoConfig
import com.smsreg.db.constants.CollectionNames
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{ObjectIdField, ObjectIdPk}
import net.liftweb.mongodb.BsonDSL._
import net.liftweb.record.field.BinaryField

abstract class Avatar
  extends MongoRecord[Avatar]
  with ObjectIdPk[Avatar]
  with IndexedRecord[Avatar] {

  object userId extends ObjectIdField(this)

  object image extends BinaryField(this)
}


trait AvatarMeta
  extends Avatar
  with MongoMetaRecord[Avatar] {
  self: MongoMetaRecord[Avatar] =>

  override def meta = self

  override protected def instantiateRecord: Avatar =
    new Avatar {
      override val meta = self
    }

  override def collectionName = CollectionNames.AVATARS.toString

  if (MongoConfig.isConnected(mongoIdentifier)) {
    ensureIndex(userId.name -> 1)
  }
}
