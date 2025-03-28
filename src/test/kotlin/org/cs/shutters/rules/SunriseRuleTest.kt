package org.cs.shutters.rules

import io.mockk.mockk
import org.cs.shutters.TestData
import org.cs.shutters.apis.SunCalculationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class SunriseRuleTest {
    @Test
    fun `should open shutters at sunrise time`() {
        val rule = createRule(earliest = "07:00")

        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2024-12-18T01:00:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2024-12-18T07:14:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2024-12-18T07:15:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2024-12-19T07:15:00Z")))
    }

    @Test
    fun `should not open earlier than earliest`() {
        val rule = createRule(earliest = "08:00")

        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2024-12-18T01:00:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2024-12-18T07:14:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2024-12-18T08:01:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2024-12-18T08:15:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2024-12-19T07:15:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2024-12-19T08:01:00Z")))
    }

    @Test
    fun `should open shutters at specified weekdays`() {
        val rule = createRule(earliest = "07:00", weekdays = listOf("Thursday"))

        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2024-12-18T01:00:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2024-12-18T07:14:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2024-12-18T07:15:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2024-12-19T07:15:00Z")))
    }

    private fun expectedPositionAction() = Action.Positioning(listOf(DevicePosition(100, "id-1"), DevicePosition(100, "id-2")))

    private fun createRule(
        earliest: String,
        weekdays: List<String> = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
    ): SunriseRule {
        val configurationProperties =
            TestData.configurationProperties(
                latitude = 52.520008,
                longitude = 13.404954,
                offsetInMinutes = 0,
                deviceIds = listOf("id-1", "id-2"),
                earliest = earliest,
                weekdays = weekdays,
            )

        return SunriseRule(
            configurationProperties,
            sunCalculationService = SunCalculationService(configurationProperties, mockk(relaxed = true)),
        )
    }
}
