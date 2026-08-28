package com.example.model

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    AUTHENTICATING,
    ONLINE,
    RECONNECTING,
    ERROR
}

data class Vec3(
    val x: Double = 0.0,
    val y: Double = 64.0,
    val z: Double = 0.0
)

data class BotStatus(
    val state: ConnectionState = ConnectionState.ONLINE,
    val uptimeSeconds: Long = 1842, // e.g. 00:30:42
    val pingMs: Int = 34,
    val fps: Int = 60,
    val health: Int = 20,
    val maxHealth: Int = 20,
    val food: Int = 19,
    val maxFood: Int = 20,
    val position: Vec3 = Vec3(124.5, 71.0, -420.8),
    val dimension: String = "Overworld",
    val currentServer: String = "play.hypixel.net",
    val activePort: Int = 25565,
    val username: String = "Player_AFK",
    val lastAction: String = "Looking around (Anti-Kick)",
    val lastError: String? = null,
    val isAutoWalking: Boolean = false,
    val reconnectCountdown: Int = 0
) {
    fun formattedUptime(): String {
        val hours = uptimeSeconds / 3600
        val minutes = (uptimeSeconds % 3600) / 60
        val seconds = uptimeSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}

data class OAuthDeviceCodeInfo(
    val userCode: String,
    val deviceCode: String,
    val verificationUrl: String = "https://microsoft.com/link",
    val expiresInSeconds: Int = 900,
    val message: String = "To sign in, use a web browser to open the page https://microsoft.com/link and enter the code."
)
