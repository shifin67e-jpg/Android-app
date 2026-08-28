package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.engine.BotEngine
import com.example.model.ConnectionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class MineflayerBotService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var botEngine: BotEngine

    override fun onCreate() {
        super.onCreate()
        botEngine = BotEngine.getInstance(this)
        createNotificationChannel()
        acquireWakeLock()
        observeBotStatus()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_BOT -> {
                botEngine.disconnectBot()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_SERVICE -> {
                val notification = buildNotification("Connecting to server...", "AFK Bot Initializing")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }
        }
        return START_STICKY
    }

    private fun observeBotStatus() {
        serviceScope.launch {
            botEngine.status.collectLatest { status ->
                val title = when (status.state) {
                    ConnectionState.ONLINE -> "AFK Bot Active • ${status.currentServer}"
                    ConnectionState.CONNECTING -> "Connecting to ${status.currentServer}..."
                    ConnectionState.AUTHENTICATING -> "Authenticating Microsoft Account..."
                    ConnectionState.RECONNECTING -> "Reconnecting to ${status.currentServer}..."
                    ConnectionState.DISCONNECTED -> "AFK Bot Disconnected"
                    ConnectionState.ERROR -> "AFK Bot Error: ${status.lastError ?: "Unknown"}"
                }
                val content = when (status.state) {
                    ConnectionState.ONLINE -> "Uptime: ${status.formattedUptime()} | Ping: ${status.pingMs}ms | HP: ${status.health}/20"
                    else -> status.lastAction
                }

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, buildNotification(title, content))
            }
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MineflayerBotService::class.java).apply {
            action = ACTION_STOP_BOT
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(pendingOpenApp)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", pendingStop)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mineflayer Bot Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the Mineflayer Minecraft AFK Bot active in the background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MinecraftAfkBot::ServiceWakeLock").apply {
            setReferenceCounted(false)
            acquire(24 * 60 * 60 * 1000L) // 24 hours max
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "mc_bot_persistence_channel"
        const val NOTIFICATION_ID = 42565
        const val ACTION_START_SERVICE = "com.example.action.START_SERVICE"
        const val ACTION_STOP_BOT = "com.example.action.STOP_BOT"

        fun start(context: Context) {
            val intent = Intent(context, MineflayerBotService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MineflayerBotService::class.java).apply {
                action = ACTION_STOP_BOT
            }
            context.startService(intent)
        }
    }
}
