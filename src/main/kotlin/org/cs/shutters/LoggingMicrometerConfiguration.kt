package org.cs.shutters

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!aws")
class LoggingMicrometerConfiguration {
    @Bean
    fun meterRegistry(): MeterRegistry {
        return LoggingMeterRegistry()
    }
}
