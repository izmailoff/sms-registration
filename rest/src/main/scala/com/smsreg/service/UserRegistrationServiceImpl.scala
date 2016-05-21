package com.example.service

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import com.smsreg.communication.sms.TwilioProcessor
import com.smsreg.user.avatar.{AvatarProcessor, GetAvatar, UserAvatar}
import com.smsreg.user.registration.{RegistrationProcessor, RegistrationRequest, RegistrationValidation}
import net.liftweb.json.JsonDSL._
import net.liftweb.json.{Extraction, JValue}
import org.bson.types.ObjectId
import spray.http.HttpHeaders.Allow
import spray.http.HttpMethods._
import spray.http.MultipartFormData

import scala.concurrent.duration._

case class Link(rel: String, href: String)

trait UserRegistrationServiceImpl
  extends UserRegistrationServiceApi {
  self: Actor =>

  val twilioHandler = context.actorOf(Props[TwilioProcessor], "twilio-processor")
  // TODO: check supervisor strategy - twilio should be restarted on exception

  val rootPathLinks: JValue = {
    import net.liftweb.json.JsonDSL._
    implicit val formats = net.liftweb.json.DefaultFormats
    val links =
      Link("self", "/") ::
        Link("register", "/api/users") :: Nil
    ("links" -> links.map(Extraction.decompose(_))) // FIXME: incomplete
  }

  implicit val requestTimeout = Timeout(5 seconds) // TODO: take from Conf - Twilio related

  lazy val route =
    path("") {
      options {
        respondWithHeader(Allow(OPTIONS, GET)) {
          complete {
            rootPathLinks
          }
        }
      }
    } ~
    pathPrefix("api" / "users") {
      pathPrefix("register") {
        post {
          entity(as[RegistrationRequest]) { req =>
            produce(instanceOf[JValue]) { requestCompleter => ctx =>
              registrationProcessorFor(req.deviceId, requestCompleter) ! req
            }
          }
        }
      } ~
      pathPrefix("verify") {
        post {
          entity(as[RegistrationValidation]) { req =>
            produce(instanceOf[JValue]) { requestCompleter => ctx =>
              registrationProcessorFor(req.deviceId, requestCompleter) ! req
            }
          }
        }
      } ~
      pathPrefix(ObjectIdSegment / "avatar") { userId =>
        post {
          entity(as[MultipartFormData]) { data =>
            data.get("avatar") match {
              case Some(image) =>
                val bytes = image.entity.data.toByteArray
                produce(instanceOf[JValue]) { requestCompleter => ctx =>
                  avatarProcessorFor(userId, requestCompleter, _ => ()) ! UserAvatar(userId, bytes)
                }
              case None =>
                complete {
                  RegistrationProcessor.FAILURE ~ ("message" -> "No files found, expected 'avatar'.")
                }
            }
          }
        } ~
        pathPrefix(JpegIdSegment) { avatarId =>
          get {
            produce(instanceOf[UserAvatar]) { requestCompleter => ctx =>
              avatarProcessorFor(userId, _ => (), requestCompleter) ! GetAvatar(userId)
            }
          }
        }
      }
    }

  def registrationProcessorFor(deviceId: String, requestCompleter: JValue => Unit): ActorRef =
    context.actorOf(RegistrationProcessor.props(twilioHandler, requestCompleter), s"REGISTRATION_DID_$deviceId")

  def avatarProcessorFor(userId: ObjectId, save: JValue => Unit, fetch: UserAvatar => Unit): ActorRef =
    context.actorOf(AvatarProcessor.props(save, fetch), s"AVATAR_UID_${userId.toString}")
}
