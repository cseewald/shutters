package org.cs.shutters.rules

import io.mockk.mockk
import org.cs.shutters.TestData
import org.cs.shutters.apis.SunCalculationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class SunsetRuleTest {
    @Test
    fun `should return no action on first call at daylight`() {
        assertEquals(Action.None, createRule().resolveAction(ZonedDateTime.parse("2023-02-14T12:00:00Z")))
    }

    @Test
    fun `should return no action on first call during nighttime`() {
        assertEquals(Action.None, createRule().resolveAction(ZonedDateTime.parse("2023-02-14T23:00:00Z")))
    }

    @Test
    fun `should return position action after sunset starting during daylight with positive offset`() {
        val rule = createRule(offset = 15)

        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T12:00:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T12:01:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T12:02:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2023-02-14T16:32:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T16:33:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-15T14:12:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2023-02-15T16:34:00Z")))
    }

    @Test
    fun `should return position action after sunset starting during nighttime with positive offset`() {
        val rule = createRule(offset = 15)

        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T22:00:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T22:01:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-15T02:00:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2023-02-15T16:34:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-15T16:35:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-16T12:35:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2023-02-16T16:37:00Z")))
    }

    @Test
    fun `should return position action after sunset starting during daylight with negative offset`() {
        val rule = createRule(offset = -15)

        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T12:00:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T12:01:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T12:02:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2023-02-14T16:02:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T16:03:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T16:04:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T17:04:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-15T12:04:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2023-02-15T16:04:00Z")))
    }

    @Test
    fun `should return position action after sunset starting during nighttime with negative offset`() {
        val rule = createRule(offset = -15)

        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T22:00:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T22:01:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-15T02:00:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2023-02-15T16:04:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-15T16:05:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-15T17:05:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-16T11:05:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2023-02-16T16:07:00Z")))
    }

    @Test
    fun `should return no action if invoked exactly at sunset`() {
        val rule = createRule(offset = 15)

        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T12:00:00Z")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T16:31:49Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2023-02-14T16:32:49Z")))
    }

    @Test
    fun `should return position action after sunset started after sunset but before offset passes`() {
        val rule = createRule(offset = 15)

        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-02-14T16:20:00Z")))
        assertEquals(expectedPositionAction(), rule.resolveAction(ZonedDateTime.parse("2023-02-14T16:32:00Z")))
    }

    @Test
    fun `should return position action after sunset in specific timezone`() {
        val rule = createRule(offset = 0)

        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-03-26T12:00:00+02:00[Europe/Berlin]")))
        assertEquals(Action.None, rule.resolveAction(ZonedDateTime.parse("2023-03-26T18:36:00+02:00[Europe/Berlin]")))
        assertEquals(
            expectedPositionAction(),
            rule.resolveAction(ZonedDateTime.parse("2023-03-28T19:34:00+02:00[Europe/Berlin]")),
        )
    }

    private fun expectedPositionAction() = Action.Positioning(listOf(DevicePosition(0, "id-1"), DevicePosition(0, "id-2")))

    private fun createRule(offset: Long = 15): SunsetRule {
        val configurationProperties =
            TestData.configurationProperties(
                latitude = 52.520008,
                longitude = 13.404954,
                offsetInMinutes = offset,
                deviceIds = listOf("id-1", "id-2"),
            )
        return SunsetRule(
            configurationProperties,
            sunCalculationService = SunCalculationService(configurationProperties, mockk(relaxed = true)),
        )
    }
}
