package org.cs.shutters

import io.micrometer.cloudwatch2.CloudWatchConfig
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient


@Configuration
class MicrometerConfiguration {

    @Bean
    fun meterRegistry(): MeterRegistry {
        val cloudWatchConfig: CloudWatchConfig = object : CloudWatchConfig {
            private val configs = mapOf(
                "${prefix()}.namespace" to "shutters-app",
            )

            override fun get(key: String): String? {
                return configs[key]
            }
        }

        return CloudWatchMeterRegistry(cloudWatchConfig, Clock.SYSTEM, CloudWatchAsyncClient.create())
    }
}
