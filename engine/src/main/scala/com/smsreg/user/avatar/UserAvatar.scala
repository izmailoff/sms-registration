package com.smsreg.user.avatar

import org.bson.types.ObjectId

case class UserAvatar(userId: ObjectId, imageBytes: Array[Byte])

case class GetAvatar(userId: ObjectId)
