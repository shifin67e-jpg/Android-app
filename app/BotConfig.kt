package com.example.model

enum class AuthType {
    OFFLINE,
    MICROSOFT
}

data class AntiAfkConfig(
    val enabled: Boolean = true,
    val autoWalk: Boolean = true,
    val autoJump: Boolean = false,
    val autoSneak: Boolean = true,
    val antiKickRotation: Boolean = true,
    val rotationIntervalSeconds: Int = 15,
    val autoEat: Boolean = true,
    val autoEatThreshold: Int = 14
)

data class BotConfig(
    val id: String = "default_session",
    val profileName: String = "Main AFK Session",
    val host: String = "play.hypixel.net",
    val port: Int = 25565,
    val username: String = "Player_AFK",
    val authType: AuthType = AuthType.OFFLINE,
    val mcVersion: String = "1.20.1",
    val autoReconnect: Boolean = true,
    val reconnectDelaySeconds: Int = 10,
    val antiAfk: AntiAfkConfig = AntiAfkConfig()
)
