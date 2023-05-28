package org.cs.shutters

import java.time.ZoneId

object TestData {
    fun configuration(
        zoneId: ZoneId = ZoneId.of("UTC"),
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        offsetInMinutes: Long = 0,
        deviceIds: List<String> = listOf(),
        sunShades: List<ShuttersProperties.Rules.SunShade> = listOf(),
    ): ShuttersProperties {
        return ShuttersProperties(
            zoneId = zoneId,
            latitude = latitude,
            longitude = longitude,
            apiWebclients = ShuttersProperties.ApiWebclients(
                connectTimeoutInMs = 1000,
                readTimeoutInMs = 1000,
                writeTimeoutInMs = 1000,
                shellyApi = ShuttersProperties.ApiWebclients.ShellyApi(
                    server = "http://host.com",
                    authorizationKey = "key",
                    pauseBetweenRequestsInMs = 1000,
                ),
                weatherApi = ShuttersProperties.ApiWebclients.WeatherApi(
                    key = "key",
                ),
            ),
            rules = ShuttersProperties.Rules(
                sunset = ShuttersProperties.Rules.Sunset(
                    offsetInMin = offsetInMinutes,
                    deviceIds = deviceIds,
                    overrideTime = ""
                ),
                sunShades = sunShades,
            )
        )
    }
}
