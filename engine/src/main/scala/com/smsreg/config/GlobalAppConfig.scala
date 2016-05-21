package com.smsreg.config

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.{FiniteDuration, _}


/**
 * A config that contains all application configuration and related auxiliary applications config.
 * Avoids loading config multiple times and provides more type safe access to conf values.
 */
object GlobalAppConfig {

  private val config = ConfigFactory.load()

  /**
   * Akka and Spray config which is used to create actor system and is also passed to Spray.
   */
  lazy val akkaConf = config

  object Application {

    object Network {
      private lazy val networkConf = config.getConfig("application.network")
      lazy val listenInterface = networkConf.getString("listenInterface")
      lazy val listenPort = networkConf.getInt("listenPort")
    }

    object Mongo {
      private lazy val mongoConf = config.getConfig("application.mongo")
      lazy val host = mongoConf.getString("host")
      lazy val port = mongoConf.getInt("port")
      lazy val dbName = mongoConf.getString("dbName")
    }

    object Twilio {
      private lazy val twilioConf = config.getConfig("application.twilio")
      lazy val accountSid = twilioConf.getString("accountSid")
      lazy val authToken = twilioConf.getString("authToken")
      lazy val from = twilioConf.getString("from")
      lazy val retries = twilioConf.getInt("retries")
    }

    object Settings {
      private lazy val settingsConf = config.getConfig("application.settings")
      lazy val pinExpireDuration: FiniteDuration =
        settingsConf.getDuration("pinExpireDuration", TimeUnit.SECONDS) seconds
    }
  }
}
