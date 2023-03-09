package org.cs.shutters

sealed interface Action {
    object None : Action
    data class Positioning(val devicePositions: List<DevicePosition>) : Action
}

data class DevicePosition(val position: Int, val device: String)
