package com.example.model

data class PresetServer(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 25565,
    val description: String,
    val category: String,
    val recommendedVersion: String = "1.20.1",
    val pingMs: Int = 42,
    val tags: List<String> = emptyList()
)

object PresetServerList {
    val PRESETS = listOf(
        PresetServer(
            id = "hypixel",
            name = "Hypixel Network",
            host = "play.hypixel.net",
            port = 25565,
            description = "SkyBlock, BedWars, and SkyWars AFK farming node.",
            category = "Popular",
            recommendedVersion = "1.8.9",
            pingMs = 28,
            tags = listOf("SkyBlock", "Minigames", "1.8.9-1.21")
        ),
        PresetServer(
            id = "2b2t",
            name = "2b2t Anarchy",
            host = "2b2t.org",
            port = 25565,
            description = "The oldest anarchy server. Queue AFK hold & chunk loading.",
            category = "Anarchy",
            recommendedVersion = "1.20.4",
            pingMs = 75,
            tags = listOf("Anarchy", "Queue", "No Rules")
        ),
        PresetServer(
            id = "complex",
            name = "Complex Gaming",
            host = "hub.mc-complex.com",
            port = 25565,
            description = "Pixelmon, Skyblock, Factions, Survival & Creative.",
            category = "SMP & Network",
            recommendedVersion = "1.20.1",
            pingMs = 45,
            tags = listOf("Pixelmon", "SMP", "Survival")
        ),
        PresetServer(
            id = "purity",
            name = "Purity Vanilla",
            host = "purityvanilla.com",
            port = 25565,
            description = "Pure Vanilla survival with no game-altering plugins.",
            category = "SMP",
            recommendedVersion = "1.21.0",
            pingMs = 38,
            tags = listOf("Vanilla", "SMP", "1.21")
        ),
        PresetServer(
            id = "localhost",
            name = "Local Development Server",
            host = "10.0.2.2", // Android emulator local host mapping
            port = 25565,
            description = "Local test server on host machine (or 127.0.0.1 on LAN).",
            category = "Local / LAN",
            recommendedVersion = "1.20.1",
            pingMs = 1,
            tags = listOf("Local", "LAN", "Dev")
        )
    )
}
