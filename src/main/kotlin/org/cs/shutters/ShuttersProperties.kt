package org.cs.shutters

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.ZoneId

@ConstructorBinding
@ConfigurationProperties(prefix = "shutters")
class ShuttersProperties(
    val zoneId: ZoneId,
    val latitude: Double,
    val longitude: Double,
    val apiWebclients: ApiWebclients,
    val rules: Rules,
) {

    class ApiWebclients(
        val connectTimeoutInMs: Int,
        val readTimeoutInMs: Long,
        val writeTimeoutInMs: Long,
        val shellyApi: ShellyApi,
        val weatherApi: WeatherApi,
    ) {
        class ShellyApi(
            val server: String,
            val authorizationKey: String,
            val pauseBetweenRequestsInMs: Long,
        )

        class WeatherApi(
            val key: String,
        )
    }

    class Rules(
        val sunset: Sunset,
        val sunShades: List<SunShade>,
    ) {
        data class Sunset(
            val offsetInMin: Long,
            val deviceIds: List<String>,
            val overrideTime: String,
        )

        data class SunShade(
            val deviceIds: List<String>,
            val targetShadePosition: Int,
            val minAzimuth: Double,
            val maxAzimuth: Double,
            val minAltitude: Double,
            val minTempInC: Double,
            val maxCloudiness: Int, // percentage 0-100
        )
    }
}
