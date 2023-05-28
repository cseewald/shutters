package org.cs.shutters

import io.mockk.every
import io.mockk.mockk
import org.cs.shutters.apis.WeatherApiClient
import org.cs.shutters.apis.WeatherApiClient.WeatherDataContainer
import org.cs.shutters.apis.WeatherApiClient.WeatherDataContainer.WeatherData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class SunShadeRuleTest {

    @Test
    fun `should transition from shade position to none shade position`() {
        val sunShadeRule = createRule()

        assertEquals(Action.None, sunShadeRule.resolveAction(ZonedDateTime.parse("2023-05-29T10:00:00+02:00")))
        assertEquals(
            expectedPositionAction(20),
            sunShadeRule.resolveAction(ZonedDateTime.parse("2023-05-29T12:00:00+02:00"))
        )
        assertEquals(Action.None, sunShadeRule.resolveAction(ZonedDateTime.parse("2023-05-29T12:01:00+02:00")))
        assertEquals(
            expectedPositionAction(100),
            sunShadeRule.resolveAction(ZonedDateTime.parse("2023-05-29T15:00:00+02:00"))
        )
        assertEquals(Action.None, sunShadeRule.resolveAction(ZonedDateTime.parse("2023-05-29T15:01:00+02:00")))
        assertEquals(
            expectedPositionAction(20),
            sunShadeRule.resolveAction(ZonedDateTime.parse("2023-05-30T12:00:00+02:00"))
        )
    }

    private fun expectedPositionAction(pos: Int): Action.Positioning {
        return Action.Positioning(listOf(DevicePosition(pos, "id-1"), DevicePosition(pos, "id-2")))
    }

    private fun createRule(): SunShadeRule {
        val weatherApiClientMock = mockk<WeatherApiClient>()

        every { weatherApiClientMock.getCurrentWeather() } returns WeatherDataContainer(WeatherData(20.0, 10))

        return SunShadeRule(
            latitude = 52.520008,
            longitude = 13.404954,
            ShuttersProperties.Rules.SunShade(
                deviceIds = listOf("id-1", "id-2"),
                targetShadePosition = 20,
                minAzimuth = 150.0,
                maxAzimuth = 210.0,
                minAltitude = 20.0,
                minTempInC = 15.0,
                maxCloudiness = 80,
            ),
            weatherApiClient = weatherApiClientMock,
        )
    }
}
