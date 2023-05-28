package org.cs.shutters

import mu.KotlinLogging
import org.cs.shutters.apis.WeatherApiClient
import org.shredzone.commons.suncalc.SunPosition
import java.time.ZonedDateTime
import javax.annotation.PostConstruct

/**
 * Lowers roller shutters to block the sun
 */
class SunShadeRule(
    private val latitude: Double,
    private val longitude: Double,
    private val sunShadeConfig: ShuttersProperties.Rules.SunShade,
    private val weatherApiClient: WeatherApiClient,
) : Rule {
    private val log = KotlinLogging.logger {}

    private var state = State.INITIAL

    override fun resolveAction(dateTime: ZonedDateTime): Action {
        val sunPosition = computeSunPosition(dateTime)

        if (state == State.INITIAL || state == State.OUTSIDE_SHADE_POSITION) {
            if (conditionsMetForShadePosition(sunPosition)) {
                state = State.IN_SHADE_POSTION

                return Action.Positioning(sunShadeConfig.deviceIds.map {
                    DevicePosition(
                        sunShadeConfig.targetShadePosition,
                        it
                    )
                })
            }
        } else if (state == State.IN_SHADE_POSTION) {
            if (conditionsMetToExitShadePosition(sunPosition)) {
                state = State.OUTSIDE_SHADE_POSITION

                return Action.Positioning(sunShadeConfig.deviceIds.map { DevicePosition(100, it) })
            }
        }

        return Action.None
    }

    private fun conditionsMetForShadePosition(sunPosition: SunPosition): Boolean {
        val currentWheather = weatherApiClient.getCurrentWeather().current

        return sunPosition.azimuth > sunShadeConfig.minAzimuth &&
                sunPosition.azimuth < sunShadeConfig.maxAzimuth &&
                sunPosition.altitude > sunShadeConfig.minAltitude &&
                currentWheather.tempC > sunShadeConfig.minTempInC &&
                currentWheather.cloud < sunShadeConfig.maxCloudiness
    }

    private fun conditionsMetToExitShadePosition(sunPosition: SunPosition): Boolean {
        return sunPosition.azimuth > sunShadeConfig.maxAzimuth || sunPosition.altitude < sunShadeConfig.minAltitude
    }

    private fun computeSunPosition(dateTime: ZonedDateTime): SunPosition {
        return SunPosition.compute()
            .on(dateTime)
            .at(latitude, longitude)
            .execute()
    }

    private enum class State {
        INITIAL, IN_SHADE_POSTION, OUTSIDE_SHADE_POSITION
    }

    @PostConstruct
    fun logConfig() {
        log.info { "Configuration: latitude: $latitude, longitude: $longitude, sunShadeConfig: $sunShadeConfig" }
    }
}
