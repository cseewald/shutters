package org.cs.shutters

import mu.KotlinLogging
import org.shredzone.commons.suncalc.SunTimes
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

/**
 * Closes roller shutters around local sunset time
 */
@Component
class SunsetRule(
    @Value("\${shutters.rules.latitude}") private val latitude: Double,
    @Value("\${shutters.rules.longitude}") private val longitude: Double,
    @Value("\${shutters.rules.sunset.offsetInMin}") private val offsetInMinutes: Long,
    @Value("\${shutters.rules.sunset.deviceIds}") private val deviceIds: List<String>,
    @Value("\${shutters.rules.sunset.overrideTime}") private val overrideTime: String
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

            return Action.Positioning(deviceIds.map { DevicePosition(0, it) })
        }

        return Action.None
    }

    private fun computeNextPositionChange(dateTime: ZonedDateTime): ZonedDateTime {
        var next = computeSunsetTime(dateTime).plusMinutes(offsetInMinutes)

        if (next.isBefore(dateTime)) {
            next = computeSunsetTime(dateTime.plusDays(1)).plusMinutes(offsetInMinutes)
        }

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
            .at(latitude, longitude)
            .fullCycle()
            .execute()
            .set!! // since we set fullCycle we can ignore nullable here
    }
}
