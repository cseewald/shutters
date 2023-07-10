package org.cs.shutters.rules

import java.time.ZonedDateTime

interface Rule {
    fun resolveAction(dateTime: ZonedDateTime): Action
}
