package org.cs.shutters

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.ZoneId
import java.time.ZonedDateTime

@Component
class RuleOrchestrator(
    private val rules: List<Rule>,
    private val shutterControl: ShutterControl,
    @Value("\${shutters.zone-id}") private val zoneId: String,
) {

    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "\${shutters.rules-execution.cron-expression}")
    fun executeRules() {
        val now = ZonedDateTime.now(ZoneId.of(zoneId))

        rules.forEach { rule ->
            when (val action = rule.resolveAction(now)) {
                is Action.None -> log.debug { "No action returned from $rule" }
                is Action.Positioning -> {
                    action.devicePositions.forEach { dp ->
                        try {
                            log.info { "Changing to position ${dp.position} for the following device: ${dp.device}" }
                            shutterControl.setPosition(dp.position, dp.device)
                        } catch (e: WebClientResponseException) {
                            log.error(e) { "Error at setting position ${e.statusCode}, ${e.responseBodyAsString}" }
                        } catch (e: Exception) {
                            log.error(e) { "Error at setting position" }
                        }
                    }
                }
            }
        }
    }
}
