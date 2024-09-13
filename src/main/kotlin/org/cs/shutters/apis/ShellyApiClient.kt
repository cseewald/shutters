package org.cs.shutters.apis

import mu.KotlinLogging
import org.cs.shutters.ShuttersProperties
import org.cs.shutters.apis.ShellyApiClient.Task
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue

@Service
class ShellyApiClient(
    private val shuttersProperties: ShuttersProperties,
    webClientBuilder: WebClient.Builder,
) {
    private val log = KotlinLogging.logger {}

    private val webClient = webClientBuilder.baseUrl(shuttersProperties.apiWebclients.shellyApi.server).build()

    private val queue = LinkedBlockingQueue<Task>()

    /**
     * Make sure there is a configured delay between each API call as there is a Shelly API rate limiter
     */
    @Scheduled(fixedDelayString = "\${shutters.api-webclients.shelly-api.pause-between-requests-in-ms}")
    private fun processTasks() {
        log.debug { "Checking for Shelly Api Tasks" }

        queue.take().call()
    }

    fun setPosition(
        position: Int,
        deviceId: String,
    ): Future<String> {
        val future = CompletableFuture<String>()

        queue.put(
            Task {
                log.debug { "Invoking Shelly Api endpoint /device/relay/roller/control" }

                try {
                    val body =
                        webClient
                            .post()
                            .uri("/device/relay/roller/control")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .bodyValue("pos=$position&id=$deviceId&auth_key=${shuttersProperties.apiWebclients.shellyApi.authorizationKey}")
                            .retrieve()
                            .bodyToMono(String::class.java)
                            .block()

                    future.complete(body)

                    log.debug { "Finished invoking Shelly Api endpoint /device/relay/roller/control. Response body: $body" }
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            },
        )

        return future
    }

    fun getStatus(deviceId: String): Future<String> {
        val future = CompletableFuture<String>()

        queue.put(
            Task {
                log.debug { "Invoking Shelly Api endpoint /device/status" }

                try {
                    val body =
                        webClient
                            .post()
                            .uri("/device/status")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .bodyValue("id=$deviceId&auth_key=${shuttersProperties.apiWebclients.shellyApi.authorizationKey}")
                            .retrieve()
                            .bodyToMono(String::class.java)
                            .block()

                    log.debug { "Finished invoking Shelly Api endpoint /device/status with response body: $body" }

                    future.complete(body)
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            },
        )

        return future
    }

    private fun interface Task {
        fun call()
    }
}
