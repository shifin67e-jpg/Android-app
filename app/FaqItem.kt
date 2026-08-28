package com.example.model

data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: String,
    val tags: List<String> = emptyList()
)

data class SupporterNode(
    val id: String,
    val region: String,
    val location: String,
    val flag: String,
    val status: String = "ACTIVE",
    val pingMs: Int,
    val loadPercent: Int,
    val isPro: Boolean = false,
    val throughput: String = "1.0 Gbps"
)

object GuideData {
    val FAQ_LIST = listOf(
        FaqItem(
            id = "port_forwarding",
            question = "How does Port Forwarding & Local Connection work?",
            answer = "If you are running a local Minecraft server or connecting over LAN, standard port is 25565. On an Android emulator, '10.0.2.2' routes to your computer's localhost. If connecting to a remote home server, forward TCP port 25565 in your router's gateway settings or use a tunnel like Playit.gg or Ngrok.",
            category = "Networking",
            tags = listOf("Ports", "LAN", "IP Setup")
        ),
        FaqItem(
            id = "msa_safety",
            question = "Is Microsoft OAuth 2.0 Device Code safe?",
            answer = "Yes! The Microsoft Device Flow generates an official 8-digit verification code. You authenticate directly on https://microsoft.com/link using your browser. The app never receives or stores your raw password. It only receives a cryptographic session token scoped to Xbox Live authentication.",
            category = "Security",
            tags = listOf("OAuth", "Microsoft", "Safety")
        ),
        FaqItem(
            id = "anti_cheat",
            question = "How to avoid Anti-Cheat detection & kick timers?",
            answer = "Most servers kick players after 5-15 minutes of inactivity or if repetitive unhuman movements are detected. Our Anti-AFK engine introduces randomized micro-jitters: variable yaw/pitch look angles, periodic randomized forward steps (300ms pulse), intermittent sneaking, and auto-eating when hunger falls below 14/20.",
            category = "Anti-AFK",
            tags = listOf("Anti-Cheat", "Rotation", "Stealth")
        ),
        FaqItem(
            id = "version_matrix",
            question = "Minecraft Java Edition Version Compatibility Matrix",
            answer = "Mineflayer includes protocol auto-negotiation supporting Java Edition from 1.8.8 all the way through 1.21.x. For combat/minigame servers (like Hypixel), version 1.8.9 is recommended. For modern SMPs and Caves & Cliffs features, 1.20.1 to 1.21.x are fully supported.",
            category = "Compatibility",
            tags = listOf("1.8.9", "1.20", "1.21", "Versions")
        ),
        FaqItem(
            id = "background_service",
            question = "How does 24/7 background persistence work?",
            answer = "The app utilizes an Android ForegroundService with a persistent notification and partial WakeLock. This keeps the network socket alive even when your phone screen turns off or when switching apps. Disable OS battery optimization for Minecraft AFK Bot for best 24/7 reliability.",
            category = "Android Service",
            tags = listOf("ForegroundService", "Keep-Alive", "Battery")
        )
    )

    val SUPPORTER_NODES = listOf(
        SupporterNode(
            id = "us_east",
            region = "US East (N. Virginia)",
            location = "Ashburn, VA",
            flag = "🇺🇸",
            status = "OPTIMAL",
            pingMs = 24,
            loadPercent = 38,
            isPro = false,
            throughput = "10 Gbps"
        ),
        SupporterNode(
            id = "us_west",
            region = "US West (Oregon)",
            location = "Boardman, OR",
            flag = "🇺🇸",
            status = "OPTIMAL",
            pingMs = 45,
            loadPercent = 52,
            isPro = false,
            throughput = "10 Gbps"
        ),
        SupporterNode(
            id = "eu_central",
            region = "Europe Central (Frankfurt)",
            location = "Frankfurt, DE",
            flag = "🇩🇪",
            status = "OPTIMAL",
            pingMs = 31,
            loadPercent = 41,
            isPro = true,
            throughput = "10 Gbps"
        ),
        SupporterNode(
            id = "ap_east",
            region = "Asia Pacific (Tokyo)",
            location = "Tokyo, JP",
            flag = "🇯🇵",
            status = "ACTIVE",
            pingMs = 82,
            loadPercent = 64,
            isPro = true,
            throughput = "5 Gbps"
        ),
        SupporterNode(
            id = "sa_east",
            region = "South America (São Paulo)",
            location = "São Paulo, BR",
            flag = "🇧🇷",
            status = "ACTIVE",
            pingMs = 110,
            loadPercent = 29,
            isPro = false,
            throughput = "2.5 Gbps"
        )
    )
}
