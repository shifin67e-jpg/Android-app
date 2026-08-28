package com.example.engine

import android.content.Context
import android.util.Log
import com.example.model.*
import com.example.util.MinecraftColorParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class BotEngine private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _status = MutableStateFlow(
        BotStatus(
            state = ConnectionState.ONLINE,
            uptimeSeconds = 1842,
            pingMs = 28,
            fps = 60,
            health = 20,
            maxHealth = 20,
            food = 19,
            maxFood = 20,
            position = Vec3(124.5, 71.0, -420.8),
            dimension = "Overworld",
            currentServer = "play.hypixel.net",
            activePort = 25565,
            username = "Player_AFK",
            lastAction = "Anti-AFK: Look rotation jittered (yaw: 42°, pitch: -4°)"
        )
    )
    val status: StateFlow<BotStatus> = _status.asStateFlow()

    private val _config = MutableStateFlow(BotConfig())
    val config: StateFlow<BotConfig> = _config.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _oauthDeviceCode = MutableStateFlow<OAuthDeviceCodeInfo?>(null)
    val oauthDeviceCode: StateFlow<OAuthDeviceCodeInfo?> = _oauthDeviceCode.asStateFlow()

    private var webSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    private var tickerJob: Job? = null
    private var afkLoopJob: Job? = null
    private var chatSimJob: Job? = null

    init {
        initializeSampleChatHistory()
        startEngineLoops()
    }

    private fun initializeSampleChatHistory() {
        val initialMessages = listOf(
            ChatMessage(
                rawText = "§6§l[SERVER] §r§fWelcome back to §b§lHypixel Network§r§f! You are connected to §eSkyBlock Mini72B§f.",
                plainText = "[SERVER] Welcome back to Hypixel Network! You are connected to SkyBlock Mini72B.",
                sender = "Server",
                type = MessageType.SYSTEM,
                timestampMillis = System.currentTimeMillis() - 120000
            ),
            ChatMessage(
                rawText = "§a[Mineflayer Bridge] §r§7Spawned at §eX: 124 Y: 71 Z: -420 §7in §bOverworld§7. Anti-AFK initialized.",
                plainText = "[Mineflayer Bridge] Spawned at X: 124 Y: 71 Z: -420 in Overworld. Anti-AFK initialized.",
                sender = "Engine",
                type = MessageType.SYSTEM,
                timestampMillis = System.currentTimeMillis() - 110000
            ),
            ChatMessage(
                rawText = "§7<§bMVP§c+ §fNotch_Fan§7> §fSelling Tier 11 Clay Minions at my island! /visit me",
                plainText = "<MVP+ Notch_Fan> Selling Tier 11 Clay Minions at my island! /visit me",
                sender = "Notch_Fan",
                type = MessageType.PLAYER,
                timestampMillis = System.currentTimeMillis() - 85000
            ),
            ChatMessage(
                rawText = "§dFrom §e[VIP] Diamond_Miner§d: §fHey, are you AFK farming sugar cane?",
                plainText = "From [VIP] Diamond_Miner: Hey, are you AFK farming sugar cane?",
                sender = "Diamond_Miner",
                type = MessageType.WHISPER,
                timestampMillis = System.currentTimeMillis() - 45000
            ),
            ChatMessage(
                rawText = "§2[Anti-AFK] §r§7Auto-ate 1x §eCooked Steak §7(Food level: 20/20).",
                plainText = "[Anti-AFK] Auto-ate 1x Cooked Steak (Food level: 20/20).",
                sender = "AntiAFK",
                type = MessageType.ACTION,
                timestampMillis = System.currentTimeMillis() - 20000
            ),
            ChatMessage(
                rawText = "§7<§6VIP§a+ §fSteve_99§7> §fDragons party in hub 1, 4/8 eyes placed! §a§lJOIN NOW",
                plainText = "<VIP+ Steve_99> Dragons party in hub 1, 4/8 eyes placed! JOIN NOW",
                sender = "Steve_99",
                type = MessageType.PLAYER,
                timestampMillis = System.currentTimeMillis() - 8000
            )
        )
        _chatMessages.value = initialMessages
    }

    private fun startEngineLoops() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(1000)
                if (_status.value.state == ConnectionState.ONLINE) {
                    val currentUptime = _status.value.uptimeSeconds + 1
                    val jitteredPing = (25..45).random()
                    val jitteredFps = (58..62).random()
                    _status.value = _status.value.copy(
                        uptimeSeconds = currentUptime,
                        pingMs = jitteredPing,
                        fps = jitteredFps
                    )
                } else if (_status.value.state == ConnectionState.RECONNECTING) {
                    val countdown = _status.value.reconnectCountdown
                    if (countdown > 1) {
                        _status.value = _status.value.copy(reconnectCountdown = countdown - 1)
                    } else {
                        connectBot(_config.value)
                    }
                }
            }
        }

        startAfkEngine()
        startLiveChatSimulation()
    }

    private fun startAfkEngine() {
        afkLoopJob?.cancel()
        afkLoopJob = scope.launch {
            while (isActive) {
                val intervalSec = _config.value.antiAfk.rotationIntervalSeconds.coerceAtLeast(5)
                delay(intervalSec * 1000L)

                if (_status.value.state == ConnectionState.ONLINE && _config.value.antiAfk.enabled) {
                    val yaw = Random.nextInt(-180, 180)
                    val pitch = Random.nextInt(-30, 30)

                    val actionLog = StringBuilder("Anti-AFK: Jittered head (yaw: ${yaw}°, pitch: ${pitch}°)")

                    // Check auto-walk
                    if (_config.value.antiAfk.autoWalk) {
                        actionLog.append(" + 300ms micro-step")
                        _status.value = _status.value.copy(
                            isAutoWalking = true,
                            position = Vec3(
                                _status.value.position.x + (Random.nextDouble(-0.3, 0.3)),
                                _status.value.position.y,
                                _status.value.position.z + (Random.nextDouble(-0.3, 0.3))
                            )
                        )
                        delay(350)
                        _status.value = _status.value.copy(isAutoWalking = false)
                    }

                    // Check auto-eat
                    if (_config.value.antiAfk.autoEat && _status.value.food <= _config.value.antiAfk.autoEatThreshold) {
                        _status.value = _status.value.copy(food = 20)
                        addChatMessage(
                            ChatMessage(
                                rawText = "§2[Anti-AFK] §r§7Auto-ate §eGolden Carrot§7. Food restored to §a20/20§7.",
                                plainText = "[Anti-AFK] Auto-ate Golden Carrot. Food restored to 20/20.",
                                sender = "AntiAFK",
                                type = MessageType.ACTION
                            )
                        )
                    }

                    _status.value = _status.value.copy(lastAction = actionLog.toString())
                }
            }
        }
    }

    private fun startLiveChatSimulation() {
        chatSimJob?.cancel()
        chatSimJob = scope.launch {
            val randomChatBank = listOf(
                Pair("§7<§bMVP§f Alex_01§7> §fAnyone have Enchanted Sugar Cane for trade?", "Alex_01"),
                Pair("§7<§fCreeperHunter§7> §a§lGG §r§7on that dungeon run!", "CreeperHunter"),
                Pair("§c[Auto-Mod] §r§7Please avoid spamming caps in public chat.", "Auto-Mod"),
                Pair("§7<§6VIP §fRedstoneGuy§7> §fAFK cobble generator is at maximum speed ⚡", "RedstoneGuy"),
                Pair("§dFrom §e[MVP+] SkyLover§d: §fHey AFK bot, is your melon farm active?", "SkyLover"),
                Pair("§e§l[EVENT] §r§fSpooky Festival begins in §65 minutes§f!", "Server"),
                Pair("§7<§fPixelMaster§7> §fSelling 64x Enchanted Diamonds at spawn warp.", "PixelMaster"),
                Pair("§a[Anti-Cheat Watchdog] §r§7Status: §aCLEAN§7. 0 flags detected in chunk.", "Watchdog")
            )

            while (isActive) {
                delay(Random.nextLong(12000, 25000))
                if (_status.value.state == ConnectionState.ONLINE) {
                    val sample = randomChatBank.random()
                    val type = when {
                        sample.first.startsWith("§dFrom") -> MessageType.WHISPER
                        sample.first.startsWith("§e§l[EVENT]") || sample.first.startsWith("§c[Auto-Mod]") -> MessageType.SYSTEM
                        sample.first.startsWith("§a[Anti-Cheat") -> MessageType.ACTION
                        else -> MessageType.PLAYER
                    }
                    addChatMessage(
                        ChatMessage(
                            rawText = sample.first,
                            plainText = MinecraftColorParser.stripCodes(sample.first),
                            sender = sample.second,
                            type = type
                        )
                    )
                }
            }
        }
    }

    fun connectBot(config: BotConfig) {
        _config.value = config

        if (config.authType == AuthType.MICROSOFT) {
            _status.value = _status.value.copy(
                state = ConnectionState.AUTHENTICATING,
                currentServer = config.host,
                activePort = config.port,
                username = config.username,
                lastAction = "Requesting Microsoft OAuth Device Code..."
            )

            // Trigger Device Code flow
            val code = "MC-" + (1000..9999).random() + "-" + (1000..9999).random()
            _oauthDeviceCode.value = OAuthDeviceCodeInfo(
                userCode = code,
                deviceCode = java.util.UUID.randomUUID().toString(),
                verificationUrl = "https://microsoft.com/link",
                expiresInSeconds = 900
            )

            scope.launch {
                delay(4000) // Simulated OAuth confirmation
                _oauthDeviceCode.value = null
                proceedToConnect(config)
            }
        } else {
            proceedToConnect(config)
        }
    }

    private fun proceedToConnect(config: BotConfig) {
        _status.value = _status.value.copy(
            state = ConnectionState.CONNECTING,
            currentServer = config.host,
            activePort = config.port,
            username = config.username,
            uptimeSeconds = 0,
            lastAction = "Negotiating Minecraft protocol handshake (${config.mcVersion})..."
        )

        addChatMessage(
            ChatMessage(
                rawText = "§e[Mineflayer Engine] §r§7Connecting to §b${config.host}:${config.port} §7as §f${config.username}§7...",
                plainText = "[Mineflayer Engine] Connecting to ${config.host}:${config.port} as ${config.username}...",
                sender = "Engine",
                type = MessageType.SYSTEM
            )
        )

        // Try WebSocket bridge if available
        tryConnectIpcBridge(config)

        scope.launch {
            delay(1500)
            _status.value = _status.value.copy(
                state = ConnectionState.ONLINE,
                uptimeSeconds = 0,
                health = 20,
                food = 20,
                lastAction = "Connected successfully. Anti-AFK active."
            )

            addChatMessage(
                ChatMessage(
                    rawText = "§a§l[SUCCESS] §r§fJoined server §b${config.host}§f. Mineflayer bot instance active.",
                    plainText = "[SUCCESS] Joined server ${config.host}. Mineflayer bot instance active.",
                    sender = "Engine",
                    type = MessageType.SYSTEM
                )
            )
        }
    }

    private fun tryConnectIpcBridge(config: BotConfig) {
        try {
            val request = Request.Builder()
                .url("ws://127.0.0.1:8080")
                .build()

            webSocket?.close(1000, "Reconnecting")
            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(ws: WebSocket, response: Response) {
                    Log.d("MineflayerIPC", "WebSocket IPC bridge connected")
                    val payload = JSONObject().apply {
                        put("action", "CONNECT_BOT")
                        put("payload", JSONObject().apply {
                            put("host", config.host)
                            put("port", config.port)
                            put("username", config.username)
                            put("auth", if (config.authType == AuthType.MICROSOFT) "microsoft" else "offline")
                            put("version", config.mcVersion)
                            put("antiAFK", config.antiAfk.enabled)
                            put("autoWalk", config.antiAfk.autoWalk)
                            put("autoEat", config.antiAfk.autoEat)
                        })
                    }
                    ws.send(payload.toString())
                }

                override fun onMessage(ws: WebSocket, text: String) {
                    handleIpcMessage(text)
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    Log.d("MineflayerIPC", "WebSocket bridge fallback to internal engine: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.d("MineflayerIPC", "IPC bridge socket not available, running standalone engine.")
        }
    }

    private fun handleIpcMessage(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            when (json.optString("action")) {
                "STATUS_CHANGED" -> {
                    val payload = json.optJSONObject("payload") ?: return
                    val stateStr = payload.optString("state", "ONLINE")
                    val stateEnum = when (stateStr) {
                        "ONLINE" -> ConnectionState.ONLINE
                        "CONNECTING" -> ConnectionState.CONNECTING
                        "AUTHENTICATING" -> ConnectionState.AUTHENTICATING
                        "RECONNECTING" -> ConnectionState.RECONNECTING
                        else -> ConnectionState.DISCONNECTED
                    }
                    _status.value = _status.value.copy(
                        state = stateEnum,
                        health = payload.optInt("health", _status.value.health),
                        food = payload.optInt("food", _status.value.food),
                        pingMs = payload.optInt("ping", _status.value.pingMs)
                    )
                }
                "CHAT_MESSAGE" -> {
                    val payload = json.optJSONObject("payload") ?: return
                    val raw = payload.optString("rawText", "")
                    val sender = payload.optString("sender", "System")
                    val typeStr = payload.optString("type", "PLAYER")
                    val msgType = try { MessageType.valueOf(typeStr) } catch (e: Exception) { MessageType.PLAYER }
                    addChatMessage(
                        ChatMessage(
                            rawText = raw,
                            plainText = MinecraftColorParser.stripCodes(raw),
                            sender = sender,
                            type = msgType
                        )
                    )
                }
                "OAUTH_DEVICE_CODE" -> {
                    val payload = json.optJSONObject("payload") ?: return
                    _oauthDeviceCode.value = OAuthDeviceCodeInfo(
                        userCode = payload.optString("userCode", "XXXX-XXXX"),
                        deviceCode = payload.optString("deviceCode", ""),
                        verificationUrl = payload.optString("verificationUrl", "https://microsoft.com/link"),
                        expiresInSeconds = payload.optInt("expiresIn", 900),
                        message = payload.optString("message", "")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MineflayerIPC", "Error parsing IPC message: ${e.message}")
        }
    }

    fun disconnectBot() {
        webSocket?.send(JSONObject().apply { put("action", "DISCONNECT_BOT") }.toString())
        webSocket?.close(1000, "User disconnected")
        webSocket = null

        _status.value = _status.value.copy(
            state = ConnectionState.DISCONNECTED,
            lastAction = "Disconnected by user",
            reconnectCountdown = 0
        )

        addChatMessage(
            ChatMessage(
                rawText = "§c[Mineflayer Engine] §r§7Bot disconnected from §b${_status.value.currentServer}§7.",
                plainText = "[Mineflayer Engine] Bot disconnected from ${_status.value.currentServer}.",
                sender = "Engine",
                type = MessageType.SYSTEM
            )
        )
    }

    fun forceRestart() {
        disconnectBot()
        scope.launch {
            delay(800)
            connectBot(_config.value)
        }
    }

    fun sendChatMessage(messageText: String) {
        val trimmed = messageText.trim()
        if (trimmed.isEmpty()) return

        val isCommand = trimmed.startsWith("/")

        // Send via WebSocket if connected
        webSocket?.send(JSONObject().apply {
            put("action", "SEND_CHAT")
            put("payload", JSONObject().apply {
                put("message", trimmed)
            })
        }.toString())

        // Add local echo to chat monitor
        val displayColor = if (isCommand) "§b" else "§f"
        val formatted = if (isCommand) {
            "§7[CMD] §b${trimmed}"
        } else {
            "§7<§a${_config.value.username}§7> $displayColor${trimmed}"
        }

        addChatMessage(
            ChatMessage(
                rawText = formatted,
                plainText = trimmed,
                sender = _config.value.username,
                type = if (isCommand) MessageType.COMMAND else MessageType.PLAYER
            )
        )

        // Handle quick command responses locally
        if (isCommand) {
            handleLocalCommandResponse(trimmed)
        }
    }

    private fun handleLocalCommandResponse(command: String) {
        scope.launch {
            delay(300)
            when (command.lowercase().split(" ")[0]) {
                "/afk" -> {
                    addChatMessage(
                        ChatMessage(
                            rawText = "§6[Hypixel] §r§fYou are now marked as §aAFK§f.",
                            plainText = "[Hypixel] You are now marked as AFK.",
                            sender = "Server",
                            type = MessageType.SYSTEM
                        )
                    )
                }
                "/spawn" -> {
                    addChatMessage(
                        ChatMessage(
                            rawText = "§aTeleporting to spawn in §e3 seconds§a... Do not move!",
                            plainText = "Teleporting to spawn in 3 seconds... Do not move!",
                            sender = "Server",
                            type = MessageType.SYSTEM
                        )
                    )
                    delay(3000)
                    _status.value = _status.value.copy(
                        position = Vec3(0.0, 65.0, 0.0),
                        lastAction = "Teleported to Spawn"
                    )
                    addChatMessage(
                        ChatMessage(
                            rawText = "§aTeleported to Spawn (X: 0, Y: 65, Z: 0)!",
                            plainText = "Teleported to Spawn (X: 0, Y: 65, Z: 0)!",
                            sender = "Server",
                            type = MessageType.SYSTEM
                        )
                    )
                }
                "/help" -> {
                    addChatMessage(
                        ChatMessage(
                            rawText = "§6§l=== Mineflayer Bot Commands ===§r\n§b/afk §7- Toggle AFK status\n§b/spawn §7- Warp to spawn point\n§b/home §7- Warp to home location\n§b/pay <player> <amt> §7- Send money\n§b/stats §7- Print player metrics",
                            plainText = "=== Mineflayer Bot Commands ===\n/afk - Toggle AFK status\n/spawn - Warp to spawn point\n/home - Warp to home location\n/pay <player> <amt> - Send money\n/stats - Print player metrics",
                            sender = "BotEngine",
                            type = MessageType.SYSTEM
                        )
                    )
                }
                "/stats" -> {
                    val s = _status.value
                    addChatMessage(
                        ChatMessage(
                            rawText = "§e§lBOT METRICS§r\n§7Server: §b${s.currentServer}\n§7Uptime: §a${s.formattedUptime()}\n§7Ping: §e${s.pingMs}ms §7| FPS: §e${s.fps}\n§7Health: §c${s.health}/20 §7| Food: §6${s.food}/20\n§7Position: §f${String.format("%.1f, %.1f, %.1f", s.position.x, s.position.y, s.position.z)}",
                            plainText = "BOT METRICS\nServer: ${s.currentServer}\nUptime: ${s.formattedUptime()}\nPing: ${s.pingMs}ms | FPS: ${s.fps}\nHealth: ${s.health}/20 | Food: ${s.food}/20",
                            sender = "BotEngine",
                            type = MessageType.SYSTEM
                        )
                    )
                }
                else -> {
                    addChatMessage(
                        ChatMessage(
                            rawText = "§7[Server] Executed command: §e$command",
                            plainText = "[Server] Executed command: $command",
                            sender = "Server",
                            type = MessageType.SYSTEM
                        )
                    )
                }
            }
        }
    }

    fun updateConfig(newConfig: BotConfig) {
        _config.value = newConfig
    }

    fun updateAntiAfk(antiAfkConfig: AntiAfkConfig) {
        _config.value = _config.value.copy(antiAfk = antiAfkConfig)
        startAfkEngine()
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    fun dismissOAuthDialog() {
        _oauthDeviceCode.value = null
    }

    private fun addChatMessage(msg: ChatMessage) {
        val current = _chatMessages.value.toMutableList()
        if (current.size >= 300) {
            current.removeAt(0)
        }
        current.add(msg)
        _chatMessages.value = current
    }

    companion object {
        @Volatile
        private var INSTANCE: BotEngine? = null

        fun getInstance(context: Context): BotEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BotEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
