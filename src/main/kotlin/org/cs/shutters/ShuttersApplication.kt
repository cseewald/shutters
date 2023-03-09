package org.cs.shutters

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class ShuttersApplication

fun main(args: Array<String>) {
    runApplication<ShuttersApplication>(*args)
}
