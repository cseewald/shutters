package org.cs.shutters

import java.time.ZonedDateTime

interface Rule {
    fun resolveAction(dateTime: ZonedDateTime): Action
}
