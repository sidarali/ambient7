package ru.maizy.ambient7.webapp.bootstrap

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.immutable.ListMap
import scala.util.{ Failure, Success, Try }
import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config.{ Config, ConfigFactory }
import ru.maizy.ambient7.core.data.{ AgentTags, Co2AgentId }
import ru.maizy.ambient7.webapp.AppConfig
import ru.maizy.ambient7.webapp.data.Co2Device


// TODO: safe config parsing with readable errors
trait AppConfigInit extends LazyLogging {

  private var _rawConfig: Option[Config] = None
  protected var _appConfig: Option[AppConfig] = None

  def loadAppConfig(): AppConfig = {
    _rawConfig = Some(ConfigFactory.load)

    var appConfig = AppConfig()
    appConfig = readDbConfig(appConfig)
    appConfig = readCo2DevicesConfig(appConfig)

    _appConfig = Some(appConfig)
    appConfig
  }

  private def readDbConfig(appConfig: AppConfig) = {
    val dbUrl = rawConfig.getString("db.url")
    val dbUser = rawConfig.getString("db.user")
    logger.info(s"start with db config: url=$dbUrl user=$dbUser")
    appConfig.copy(
      dbUrl = dbUrl,
      dbUser = dbUser,
      dbPassword = rawConfig.getString("db.password")
    )
  }

  private def readCo2DevicesConfig(appConfig: AppConfig) = {
    val devicesConfig = rawConfig.getObjectList("co2-devices")
    val devicesList = devicesConfig.asScala
      .toIndexedSeq
      .flatMap { deviceConfigObject =>
        val deviceConfig = deviceConfigObject.toConfig
        val id = deviceConfig.getString("id")
          val tagsString = Try(deviceConfig.getString("agent-tags")) match {
            case Failure(_) => ""
            case Success(t) => t
          }
          val tags = AgentTags.tryParseFromString(tagsString) match {
            case Right(t) => t
            case Left(error) =>
              throw new RuntimeException(s"Unable to parse agent-tags '$tagsString' for id=$id, skipping tags: $error")
          }
          Seq(id -> Co2Device(id, agentId = Co2AgentId(deviceConfig.getString("agent-name"), tags)))
        }

    appConfig.copy(co2Devices = ListMap(devicesList: _*))
  }

  def appConfig: AppConfig = _appConfig match {
    case None => throw new RuntimeException("uninitialized app config")
    case Some(c) => c
  }

  def rawConfig: Config = _rawConfig match {
    case None => throw new RuntimeException("uninitialized config")
    case Some(c) => c
  }

}
