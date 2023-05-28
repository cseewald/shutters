package org.cs.shutters

import mu.KotlinLogging
import org.cs.shutters.apis.ShellyApiClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.ZonedDateTime

@Component
class RuleOrchestrator(
    private val rules: List<Rule>,
    private val shellyApiClient: ShellyApiClient,
    private val shuttersProperties: ShuttersProperties,
) {

    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "\${shutters.rules-execution.cron-expression}")
    fun executeRules() {
        val now = ZonedDateTime.now(shuttersProperties.zoneId)

        rules.forEach { rule ->
            when (val action = rule.resolveAction(now)) {
                is Action.None -> log.debug { "No action returned from $rule" }
                is Action.Positioning -> {
                    action.devicePositions.forEach { dp ->
                        try {
                            log.info { "Changing to position ${dp.position} for the following device: ${dp.device}" }
                            shellyApiClient.setPosition(dp.position, dp.device)
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
