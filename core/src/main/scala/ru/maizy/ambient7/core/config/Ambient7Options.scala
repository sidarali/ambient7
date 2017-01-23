package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.nio.file.Path
import ru.maizy.ambient7.core.data.{ AgentTags, Co2Agent, Co2Device, Device, DeviceType, Devices }

case class FromCliDevice(
    agent: String = "main",
    tags: AgentTags = AgentTags.empty,
    deviceType: Option[DeviceType.Type] = None
)

object Ambient7Options {
  val CO2_FROM_CLI_ID = "co2-from-cli-arguments"
}

case class Ambient7Options(
    universalConfigPath: Option[Path] = None,
    influxDb: Option[InfluxDbOptions] = None,
    mainDb: Option[DbOptions] = None,
    devices: Option[Devices] = None,
    selectedDeviceId: Option[String] = None,
    fromCliDevice: Option[FromCliDevice] = None,

    // TODO: fix this little hack
    showHelp: Boolean = false
)
{

  lazy val selectedCo2Device: Option[Co2Device] =
    generatedFromCliDevice match {
      case Some(device: Co2Device) => Some(device)
      case _ => selectedDeviceId.flatMap { selectedId => co2Devices.find(_.id == selectedId) }
    }

  lazy private val generatedFromCliDevice: Option[Device] = {
    fromCliDevice.flatMap { fromCli =>
      if (fromCli.deviceType.contains(DeviceType.CO2)) {
        Some(Co2Device(
          id = Ambient7Options.CO2_FROM_CLI_ID,
          agent = Co2Agent(
            agentName = fromCli.agent,
            tags = fromCli.tags
          )
        ))
      } else {
        None
      }
    }
  }

  lazy val co2Devices: List[Co2Device] = {
    var resList = List.empty[Co2Device]
    generatedFromCliDevice match {
      case Some(device: Co2Device) => resList = device :: resList
      case _ =>
    }
    resList = resList ++ devices.map(_.co2Devices).getOrElse(List.empty)
    resList
  }
}