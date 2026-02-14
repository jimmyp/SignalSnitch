package com.signalsnitch.app.data

import java.util.Date

data class NetworkEvent(
    val timestamp: Date,
    val type: EventType,
    val message: String
)

enum class EventType {
    LOST,
    RESTORED,
    SERVICE_STARTED,
    SERVICE_STOPPED
}
