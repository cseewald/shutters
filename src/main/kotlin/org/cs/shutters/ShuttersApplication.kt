package org.cs.shutters

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableCaching
@ConfigurationPropertiesScan
@SpringBootApplication
class ShuttersApplication

fun main(args: Array<String>) {
    runApplication<ShuttersApplication>(*args)
}
