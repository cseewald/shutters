package org.cs.shutters.apis

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import org.cs.shutters.ShuttersProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class ShellyApiClient(
    private val shuttersProperties: ShuttersProperties,
    webClientBuilder: WebClient.Builder,
) {

    private val log = KotlinLogging.logger {}

    // Make sure to only send Shelly commands at a certain rate
    private lateinit var scheduler: Job
    private val taskQueue = Channel<Task>(Channel.RENDEZVOUS)

    private val webClient = webClientBuilder.baseUrl(shuttersProperties.apiWebclients.shellyApi.server).build()

    @PostConstruct
    fun startTaskScheduler() {
        scheduler = GlobalScope.launch(Dispatchers.Default) {
            while (isActive) {
                log.debug { "Waiting for task to execute" }

                try {
                    taskQueue.receive().execute()
                    delay(shuttersProperties.apiWebclients.shellyApi.pauseBetweenRequestsInMs)
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        log.error(e) { "Error at executing shelly request" }
                    }
                }
            }
        }
    }

    @PreDestroy
    fun shutdownTaskScheduler() = runBlocking {
        scheduler.cancelAndJoin()
    }

    fun getStatus(deviceId: String) = runBlocking {
        taskQueue.send(object : Task {
            override suspend fun execute() {
                webClient.post()
                    .uri("/device/status")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("id=$deviceId&auth_key=${shuttersProperties.apiWebclients.shellyApi.authorizationKey}")
                    .retrieve()
                    .awaitBody<String>()
            }
        })
    }

    fun setPosition(position: Int, deviceId: String) = runBlocking {
        taskQueue.send(object : Task {
            override suspend fun execute() {
                webClient.post()
                    .uri("/device/relay/roller/control")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("pos=$position&id=$deviceId&auth_key=$shuttersProperties.apiWebclients.shellyApi.authorizationKey")
                    .retrieve()
                    .awaitBody<String>()
            }
        })
    }

    interface Task {
        suspend fun execute()
    }
}
