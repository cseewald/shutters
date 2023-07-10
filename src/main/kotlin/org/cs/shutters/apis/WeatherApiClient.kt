package org.cs.shutters.apis

import com.fasterxml.jackson.annotation.JsonProperty
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.cs.shutters.ShuttersProperties
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class WeatherApiClient(
    private val shuttersProperties: ShuttersProperties,
    webClientBuilder: WebClient.Builder,
    meterRegistry: MeterRegistry,
) {
    private val webClient = webClientBuilder.baseUrl("https://api.weatherapi.com").build()

    init {
        Gauge.builder("weather.cloudiness") { getCurrentWeather().current.cloud }
            .description("Cloudiness in %")
            .register(meterRegistry)

        Gauge.builder("weather.temperature") { getCurrentWeather().current.tempC }
            .description("Temperature in C°")
            .register(meterRegistry)
    }

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
