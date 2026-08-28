package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.BotEngine
import com.example.model.BotConfig
import com.example.ui.components.*

@Composable
fun DashboardScreen(
    engine: BotEngine,
    modifier: Modifier = Modifier
) {
    val status by engine.status.collectAsState()
    val config by engine.config.collectAsState()
    val chatMessages by engine.chatMessages.collectAsState()
    val oauthDeviceCode by engine.oauthDeviceCode.collectAsState()

    var showPresetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("dashboard_screen")
    ) {
        // A. Status Card Header (Status, metrics, action buttons)
        StatusCard(
            status = status,
            onConnectClick = { showPresetDialog = true },
            onDisconnectClick = { engine.disconnectBot() },
            onRestartClick = { engine.forceRestart() }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Vitals Bar (Health, Hunger, Coords, Last Action)
        PlayerVitalsBar(status = status)

        Spacer(modifier = Modifier.height(10.dp))

        // Live In-Game Chat Monitor (Takes remaining space)
        ChatMonitorView(
            messages = chatMessages,
            onClearChat = { engine.clearChat() },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Command & Chat Input Bar with macro chips
        CommandDock(
            onSendMessage = { text -> engine.sendChatMessage(text) }
        )
    }

    // Default IP Helper / Server Preset Modal
    if (showPresetDialog) {
        ServerPresetDialog(
            currentConfig = config,
            onDismiss = { showPresetDialog = false },
            onConnect = { newConfig ->
                engine.connectBot(newConfig)
            }
        )
    }

    // Microsoft OAuth 2.0 Device Code Modal
    oauthDeviceCode?.let { codeInfo ->
        OAuthDeviceCodeDialog(
            info = codeInfo,
            onDismiss = { engine.dismissOAuthDialog() }
        )
    }
}
