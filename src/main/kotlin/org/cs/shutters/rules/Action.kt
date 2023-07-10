package org.cs.shutters.rules

sealed interface Action {
    object None : Action
    data class Positioning(val devicePositions: List<DevicePosition>) : Action
}

data class DevicePosition(val position: Int, val device: String)
