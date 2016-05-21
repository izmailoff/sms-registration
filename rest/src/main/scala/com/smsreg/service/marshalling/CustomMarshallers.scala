package com.smsreg.service.marshalling

import java.text.SimpleDateFormat

import com.smsreg.user.avatar.UserAvatar
import com.smsreg.user.registration.{RegistrationRequest, RegistrationValidation}
import net.liftweb.json._
import org.bson.types.ObjectId
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import spray.routing.PathMatcher

// TODO: this might be temporary to work around LiftJsonSupport - that makes all requests match json
// the fix is to remove LiftJsonSuport from MongoMarshallingSupport.

trait CustomMarshallers {
  implicit def liftJsonFormats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  }

  implicit val RegistrationRequestUnmarshaller: Unmarshaller[RegistrationRequest] =
    liftJsonUnmarshaller[RegistrationRequest]

  implicit val RegistrationValidationUnmarshaller: Unmarshaller[RegistrationValidation] =
    liftJsonUnmarshaller[RegistrationValidation]

  def liftJsonUnmarshaller[T: Manifest] =
    Unmarshaller[T](`application/json`) {
      case HttpEntity.NonEmpty(contentType, data) =>
        val raw = data.asString(HttpCharsets.`UTF-8`)
        //        try
        parse(raw).extract[T]
      //        catch {
      //          case MappingException("unknown error", ite: InvocationTargetException) =>
      //            throw ite.getCause
      //        }
    }

  implicit def liftJsonMarshaller[T <: AnyRef] =
    Marshaller.delegate[T, String](ContentTypes.`application/json`)(Serialization.write(_))

  // OR:
  //  implicit val liftJsonMarshaller =
  //    Marshaller.delegate[JValue, String](ContentTypes.`application/json`)(json => pretty(render(json)))

  implicit val AvatarMarshaller = Marshaller.of[UserAvatar](`image/jpeg`) { (value, contentType, ctx) =>
    val UserAvatar(uid, bytes) = value
    ctx.marshalTo(HttpEntity(contentType, bytes))
  }

  val JpegIdSegment = objectIdSegmentWithSuffix("\\.jpeg")

  def objectIdSegmentWithSuffix(suffix: String) =
    PathMatcher(("""^[0-9a-fA-F]{24}""" + suffix + "$").r).flatMap { str =>
      val idRegex = """^([0-9a-fA-F]{24})""".r
      for {
        id <- idRegex.findFirstIn(str)
        res <- if (ObjectId.isValid(id))
          Some(new ObjectId(id))
        else
          None
      } yield res
    }
}
