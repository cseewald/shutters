package org.cs.shutters

import mu.KotlinLogging
import org.shredzone.commons.suncalc.SunTimes
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import javax.annotation.PostConstruct

/**
 * Closes roller shutters around local sunset time
 */
@Component
class SunsetRule(private val shuttersProperties: ShuttersProperties) : Rule {
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
        var next = computeSunsetTime(dateTime).plusMinutes(shuttersProperties.rules.sunset.offsetInMin)

        if (next.isBefore(dateTime)) {
            next = computeSunsetTime(dateTime.plusDays(1)).plusMinutes(shuttersProperties.rules.sunset.offsetInMin)
        }

        val overrideTime = shuttersProperties.rules.sunset.overrideTime
        if (overrideTime.isNotEmpty() && ZonedDateTime.parse(overrideTime).isAfter(dateTime)) {
            next = ZonedDateTime.parse(overrideTime)
        }

        log.info { "Computed next position change time to $next" }

        return next
    }

    private fun computeSunsetTime(dateTime: ZonedDateTime): ZonedDateTime {
        return SunTimes.compute()
            .on(dateTime) // needed to inject the zone
            .on(dateTime.toLocalDate())
            .at(shuttersProperties.latitude, shuttersProperties.longitude)
            .fullCycle()
            .execute()
            .set!! // since we set fullCycle we can ignore nullable here
    }

    @PostConstruct
    fun logConfig() {
        log.info {
            "Configuration: latitude: ${shuttersProperties.latitude}, " +
                    "longitude: ${shuttersProperties.longitude}, " +
                    "${shuttersProperties.rules.sunset}"
        }
    }
}
