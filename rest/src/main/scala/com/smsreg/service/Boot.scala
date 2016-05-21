package com.smsreg.service

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import com.example.service.UserRegistrationServiceHandler
import com.github.izmailoff.logging.AkkaLoggingHelper
import com.github.izmailoff.mongo.connection.MongoConfig
import com.typesafe.config.ConfigFactory
import spray.can.Http
import com.smsreg.config.GlobalAppConfig._

object Boot
  extends App
  with AkkaLoggingHelper {
  import log._

  val banner =
    """
      | ___ _ __ ___  ___       _ __ ___  __ _
      |/ __| '_ ` _ \/ __|_____| '__/ _ \/ _` |
      |\__ \ | | | | \__ \_____| | |  __/ (_| |
      ||___/_| |_| |_|___/     |_|  \___|\__, |
      |                                  |___/
    """.stripMargin

  implicit val system = ActorSystem("user-reg", akkaConf)
  override val globalSystem = system
  system.registerOnTermination {
    system.log.info("User Registration service has been SHUTDOWN!")
  }

  info(banner)
  info("Reading config")
  val conf = ConfigFactory.load.getConfig("application.network")
  val listenInterface = conf.getString("listenInterface")
  val listenPort = conf.getInt("listenPort")
  info("Config has been read successfully.")

  info("Connecting to MongoDB")
  implicit val mongoId = MongoConfig.registerConnection()
  info(s"Connected to Mongo? ${MongoConfig.isConnected}.")

  info("Starting up User Registration service.")
  val service = system.actorOf(Props[UserRegistrationServiceHandler], "registration-service")
  IO(Http) ! Http.Bind(service, interface = listenInterface, port = listenPort)
  info("Everything is up and running.")
}
