package org.cs.shutters.rules

import mu.KotlinLogging
import org.cs.shutters.ShuttersProperties
import org.cs.shutters.apis.SunCalculationService
import org.shredzone.commons.suncalc.SunTimes
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import javax.annotation.PostConstruct

/**
 * Closes roller shutters around local sunset time
 */
@Component
class SunsetRule(
    private val shuttersProperties: ShuttersProperties,
    private val sunCalculationService: SunCalculationService,
) : Rule {
    private val log = KotlinLogging.logger {}

    private var nextPositionChange: ZonedDateTime? = null

    override fun resolveAction(dateTime: ZonedDateTime): Action {
        log.debug { "Resolve action for SunsetRule at $dateTime" }

        if (nextPositionChange == null) {
            nextPositionChange = computeNextPositionChange(dateTime)
        }

        if (dateTime.isAfter(nextPositionChange)) {
            nextPositionChange = computeNextPositionChange(dateTime)

            return Action.Positioning(shuttersProperties.rules.sunset.deviceIds.map { DevicePosition(0, it) })
        }

        return Action.None
    }

    private fun computeNextPositionChange(dateTime: ZonedDateTime): ZonedDateTime {
        var next = sunCalculationService.computeSunsetTime(dateTime).plusMinutes(shuttersProperties.rules.sunset.offsetInMin)

        if (next.isBefore(dateTime)) {
            next = sunCalculationService.computeSunsetTime(dateTime.plusDays(1)).plusMinutes(shuttersProperties.rules.sunset.offsetInMin)
        }

        val overrideTime = shuttersProperties.rules.sunset.overrideTime
        if (overrideTime.isNotEmpty() && ZonedDateTime.parse(overrideTime).isAfter(dateTime)) {
            next = ZonedDateTime.parse(overrideTime)
        }

        log.info { "Computed next position change time to $next" }

        return next
    }

    @PostConstruct
    fun logConfig() {
        log.info { "Configuration: ${shuttersProperties.rules.sunset}" }
    }
}
