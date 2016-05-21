package com.smsreg.db.crud

import com.github.izmailoff.mongo.connection.DbConnectionIdentifier
import com.smsreg.db.datamodel.{AvatarMeta, Avatar, UserMeta, User}
import net.liftweb.mongodb.record.MongoMetaRecord

/**
 * Defines all available CRUD interfaces for collections. This has far more flexible configuration
 * than regular `object MetaRecord` which is especially useful for tests.
 */
trait DbCrudProvider {
  this: DbConnectionIdentifier =>

  val USERS: User with MongoMetaRecord[User]

  val AVATARS: Avatar with MongoMetaRecord[Avatar]
}

trait DbCrudProviderImpl
  extends DbCrudProvider {
  this: DbConnectionIdentifier =>

  override val USERS = new UserMeta {
    override implicit def mongoIdentifier = currentMongoId
  }

  override val AVATARS = new AvatarMeta {
    override implicit def mongoIdentifier = currentMongoId
  }
}
