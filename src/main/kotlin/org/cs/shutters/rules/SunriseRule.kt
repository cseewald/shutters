package org.cs.shutters.rules

import mu.KotlinLogging
import org.cs.shutters.ShuttersProperties
import org.cs.shutters.apis.SunCalculationService
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.annotation.PostConstruct

@Component
class SunriseRule(
    private val shuttersProperties: ShuttersProperties,
    private val sunCalculationService: SunCalculationService,
) : Rule {
    private val log = KotlinLogging.logger {}

    private var nextPositionChange: ZonedDateTime? = null

    private val earliest = LocalTime.parse(shuttersProperties.rules.sunrise.earliest)
    private val dayOfWeeks = shuttersProperties.rules.sunrise.dayOfWeeks.map { DayOfWeek.valueOf(it.uppercase()) }

    override fun resolveAction(dateTime: ZonedDateTime): Action {
        log.debug { "Resolve action for SunriseRule at $dateTime" }

        if (nextPositionChange == null) {
            nextPositionChange = computeNextPositionChange(dateTime)
        }

        if (dateTime.isAfter(nextPositionChange)) {
            nextPositionChange = computeNextPositionChange(dateTime)

            return Action.Positioning(
                shuttersProperties.rules.sunrise.deviceIds
                    .map { DevicePosition(100, it) },
            )
        }

        return Action.None
    }

    private fun computeNextPositionChange(dateTime: ZonedDateTime): ZonedDateTime {
        var next = computeNextSunrise(dateTime)

        if (next.isBefore(dateTime)) {
            next = computeNextSunrise(dateTime.plusDays(1))
        }

        var plusDays = 1L
        while (!dayOfWeeks.contains(next.dayOfWeek) && plusDays <= 7) {
            next = computeNextSunrise(dateTime.plusDays(plusDays))
            plusDays++
        }

        if (next.toLocalTime().isBefore(earliest)) {
            next = next.with(earliest)
        }

        log.info { "Computed next position change time to $next" }

        return next
    }

    private fun computeNextSunrise(dateTime: ZonedDateTime): ZonedDateTime =
        sunCalculationService.computeSunriseTime(dateTime).plusMinutes(shuttersProperties.rules.sunrise.offsetInMin)

    @PostConstruct
    fun logConfig() {
        require(dayOfWeeks.isNotEmpty()) { "dayOfWeeks must not be empty" }

        log.info { "Configuration: ${shuttersProperties.rules.sunrise}" }
    }
}
