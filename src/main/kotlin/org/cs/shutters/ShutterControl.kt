package org.cs.shutters

import io.netty.channel.ChannelOption
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.client.reactive.ReactorResourceFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClient

@Service
class ShutterControl(
    @Value("\${shutters.webclient.shelly-api.connect-timeout-in-ms}") connectTimeoutInMs: Int,
    @Value("\${shutters.webclient.shelly-api.read-timeout-in-sec}") readTimeoutInSec: Int,
    @Value("\${shutters.webclient.shelly-api.write-timeout-in-sec}") writeTimeoutInSec: Int,
    @Value("\${shutters.webclient.shelly-api.server}") shellyServer: String,
    @Value("\${shutters.webclient.shelly-api.authorization-key}") private val shellyAuthorizationKey: String,
    webClientBuilder: WebClient.Builder,
    resourceFactory: ReactorResourceFactory
) {

    private val webClient: WebClient

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
                        }
                )
            ).baseUrl(shellyServer).build()
    }

    fun getStatus(deviceId: String) = runBlocking {
        webClient.post()
            .uri("/device/status")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("id=$deviceId&auth_key=$shellyAuthorizationKey")
            .retrieve()
            .awaitBody<String>()
    }

    fun setPosition(position: Int, deviceId: String) = runBlocking {
        webClient.post()
            .uri("/device/relay/roller/control")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue("pos=$position&id=$deviceId&auth_key=$shellyAuthorizationKey")
            .retrieve()
            .awaitBody<String>()
    }
}
