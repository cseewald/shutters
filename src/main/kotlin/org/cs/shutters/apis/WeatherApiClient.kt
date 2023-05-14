package org.cs.shutters.apis

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class WeatherApiClient(
    @Value("\${shutters.webclient.weather-api.key}") private val weatherApiKey: String,
    @Value("\${shutters.latitude}") private val latitude: Double,
    @Value("\${shutters.longitude}") private val longitude: Double,
    webClientBuilder: WebClient.Builder,
) {

    private val log = KotlinLogging.logger {}

    private val webClient = webClientBuilder.baseUrl("https://api.weatherapi.com").build()

    fun getCurrentWeather() = runBlocking {
        return@runBlocking webClient.get()
            .uri("/v1/current.json?key=$weatherApiKey&q=$latitude,$longitude&aqi=no")
            .retrieve()
            .awaitBody<WeatherDataContainer>()
    }

    data class WeatherDataContainer(
        val current: WeatherData,
    ) {
        data class WeatherData(
            @JsonProperty("temp_c")
            val tempC: Double,

            @JsonProperty("cloud")
            val cloud: Int,
        )
    }
}
