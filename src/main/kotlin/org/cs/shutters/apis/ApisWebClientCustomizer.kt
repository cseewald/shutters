package org.cs.shutters.apis

import io.netty.channel.ChannelOption
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.client.reactive.ReactorResourceFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Component
class ApisWebClientCustomizer(
    @Value("\${shutters.api-webclients.connect-timeout-in-ms}") private val connectTimeoutInMs: Int,
    @Value("\${shutters.api-webclients.read-timeout-in-sec}") private val readTimeoutInSec: Int,
    @Value("\${shutters.api-webclients.write-timeout-in-sec}") private val writeTimeoutInSec: Int,
    private val resourceFactory: ReactorResourceFactory,
) : WebClientCustomizer {
    override fun customize(webClientBuilder: WebClient.Builder?) {
        if (webClientBuilder == null) {
            return
        }

        webClientBuilder
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
                                connection.addHandlerLast(LoggingHandler("apis.http-channel.logger"))
                            }
                        },
                ),
            )
    }
}
