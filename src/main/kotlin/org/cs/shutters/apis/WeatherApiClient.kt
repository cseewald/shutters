package org.cs.shutters.apis

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.runBlocking
import org.cs.shutters.ShuttersProperties
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class WeatherApiClient(
    private val shuttersProperties: ShuttersProperties,
    webClientBuilder: WebClient.Builder,
) {
    private val webClient = webClientBuilder.baseUrl("https://api.weatherapi.com").build()

    @Cacheable("weather")
    fun getCurrentWeather() = runBlocking {
        return@runBlocking webClient.get()
            .uri("/v1/current.json?key=${shuttersProperties.apiWebclients.weatherApi.key}&q=${shuttersProperties.latitude},${shuttersProperties.longitude}&aqi=no")
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
