package org.cs.shutters.apis

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.cs.shutters.ShuttersProperties
import org.shredzone.commons.suncalc.SunPosition
import org.shredzone.commons.suncalc.SunTimes
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import javax.annotation.PostConstruct

@Service
class SunCalculationService(
    shuttersProperties: ShuttersProperties,
    meterRegistry: MeterRegistry,
) {
    private val log = KotlinLogging.logger {}

    private val latitude = shuttersProperties.latitude
    private val longitude = shuttersProperties.longitude

    init {
        Gauge
            .builder("sunPosition.azimuth") { computeSunPosition(ZonedDateTime.now()).azimuth }
            .description("Azimuth of the sun")
            .register(meterRegistry)

        Gauge
            .builder("sunPosition.altitude") { computeSunPosition(ZonedDateTime.now()).altitude }
            .description("Altitude of the sun")
            .register(meterRegistry)
    }

    fun computeSunPosition(dateTime: ZonedDateTime): SunPosition =
        SunPosition
            .compute()
            .at(latitude, longitude)
            .on(dateTime)
            .execute()

    fun computeSunsetTime(dateTime: ZonedDateTime): ZonedDateTime = SunTimes
        .compute()
        .at(latitude, longitude)
        .on(dateTime) // needed to inject the zone
        .on(dateTime.toLocalDate())
        .fullCycle()
        .execute()
        .set!! // since we set fullCycle we can ignore nullable here

    fun computeSunriseTime(dateTime: ZonedDateTime): ZonedDateTime = SunTimes
        .compute()
        .at(latitude, longitude)
        .on(dateTime) // needed to inject the zone
        .on(dateTime.toLocalDate())
        .fullCycle()
        .execute()
        .rise!! // since we set fullCycle we can ignore nullable here

    @PostConstruct
    fun logConfig() {
        log.info { "Configuration: latitude: $latitude, longitude: $longitude" }
    }
}
