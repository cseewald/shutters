package org.cs.shutters

import io.netty.channel.ChannelOption
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.client.reactive.ReactorResourceFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class ShutterControl(
    @Value("\${shutters.webclient.shelly-api.connect-timeout-in-ms}") connectTimeoutInMs: Int,
    @Value("\${shutters.webclient.shelly-api.read-timeout-in-sec}") readTimeoutInSec: Int,
    @Value("\${shutters.webclient.shelly-api.write-timeout-in-sec}") writeTimeoutInSec: Int,
    @Value("\${shutters.webclient.shelly-api.server}") shellyServer: String,
    @Value("\${shutters.webclient.shelly-api.authorization-key}") private val shellyAuthorizationKey: String,
    @Value("\${shutters.webclient.shelly-api.pause-between-requests-in-ms}") private val delayInMs: Long,
    webClientBuilder: WebClient.Builder,
    resourceFactory: ReactorResourceFactory,
) {

    private val log = KotlinLogging.logger {}

    // Make sure to only send Shelly commands at a certain rate
    private lateinit var scheduler: Job
    private val taskQueue = Channel<Task>(Channel.RENDEZVOUS)

    private val webClient: WebClient

    @PostConstruct
    fun startTaskScheduler() {
        scheduler = GlobalScope.launch(Dispatchers.Default) {
            while (isActive) {
                log.debug { "Waiting for task to execute" }
                taskQueue.receive().execute()
                delay(delayInMs)
            }
        }
    }

    @PreDestroy
    fun shutdownTaskScheduler() = runBlocking {
        scheduler.cancelAndJoin()
    }

    init {
        webClient = webClientBuilder
            .clientConnector(
                ReactorClientHttpConnector(
                    // see https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-webclient-reactor-netty-customization
                    HttpClient.create(resourceFactory.connectionProvider)
                        .runOn(resourceFactory.loopResources)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutInMs)
                        .doOnConnected { connection ->
                            run {
                                connection.addHandlerLast(ReadTimeoutHandler(readTimeoutInSec))
                                connection.addHandlerLast(WriteTimeoutHandler(writeTimeoutInSec))
                                connection.addHandlerLast(LoggingHandler("shelly-api.http-channel.logger"))
                            }
                        },
                ),
            ).baseUrl(shellyServer).build()
    }

    fun getStatus(deviceId: String) = runBlocking {
        taskQueue.send(object : Task {
            override suspend fun execute() {
                webClient.post()
                    .uri("/device/status")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("id=$deviceId&auth_key=$shellyAuthorizationKey")
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
                    .bodyValue("pos=$position&id=$deviceId&auth_key=$shellyAuthorizationKey")
                    .retrieve()
                    .awaitBody<String>()
            }
        })
    }

    interface Task {
        suspend fun execute()
    }
}
