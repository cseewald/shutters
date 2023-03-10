package org.cs.shutters

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class RuleOrchestrator(
    private val rules: List<Rule>,
    private val shutterControl: ShutterControl,
    @Value("\${shutters.zone-id}") private val zoneId: String,
    @Value("\${shutters.webclient.shelly-api.pause-between-commands-in-ms}") private val pauseBetweenShellyCommandsInMs: Long
) {

    private val log = KotlinLogging.logger {}
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    @Scheduled(fixedDelay = 1000 * 60 * 1, initialDelay = 1000)
    fun executeRules() {
        val now = ZonedDateTime.now(ZoneId.of(zoneId))

        rules.forEach { rule ->
            when (val action = rule.resolveAction(now)) {
                is Action.None -> log.debug { "No action returned from $rule" }
                is Action.Positioning -> {
                    action.devicePositions.forEachIndexed { i, dp ->
                        scheduler.schedule({
                            try {
                                log.info { "Changing to position ${dp.position} for the following device: ${dp.device}" }
                                shutterControl.setPosition(dp.position, dp.device)
                            } catch (e: WebClientResponseException) {
                                log.error(e) { "Error at setting position ${e.statusCode}, ${e.responseBodyAsString}" }
                            } catch (e: Exception) {
                                log.error(e) { "Error at setting position" }
                            }
                        }, i * pauseBetweenShellyCommandsInMs, TimeUnit.MILLISECONDS)
                    }
                }
            }
        }
    }
}
