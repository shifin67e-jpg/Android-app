package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun ServerPresetDialog(
    currentConfig: BotConfig,
    onDismiss: () -> Unit,
    onConnect: (BotConfig) -> Unit
) {
    var host by remember { mutableStateOf(currentConfig.host) }
    var port by remember { mutableStateOf(currentConfig.port.toString()) }
    var username by remember { mutableStateOf(currentConfig.username) }
    var authType by remember { mutableStateOf(currentConfig.authType) }
    var mcVersion by remember { mutableStateOf(currentConfig.mcVersion) }
    var autoReconnect by remember { mutableStateOf(currentConfig.autoReconnect) }

    val versionList = listOf("1.8.9", "1.12.2", "1.16.5", "1.19.4", "1.20.1", "1.20.4", "1.21.0")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("server_preset_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedCardFill)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Connect AFK Bot",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select preset or enter custom server IP",
                            color = TextMutedGray,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Server Presets Grid
                Text(
                    text = "QUICK-LAUNCH PRESETS",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PresetServerList.PRESETS.forEach { preset ->
                        val isSelected = host.equals(preset.host, ignoreCase = true)
                        Surface(
                            onClick = {
                                host = preset.host
                                port = preset.port.toString()
                                mcVersion = preset.recommendedVersion
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SurfaceContainerHighest else SurfaceContainerHigh,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonGreen) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = preset.name,
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = preset.host,
                                        color = VibrantCyan,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DeepCharcoal
                                ) {
                                    Text(
                                        text = "${preset.pingMs}ms",
                                        color = if (preset.pingMs < 50) NeonGreen else WarningGold,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Configuration Fields
                Text(
                    text = "SERVER ADDRESS & PORT",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Server Host / IP", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(2f)
                            .testTag("host_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = SubtleBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("port_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = SubtleBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bot Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Bot Username / Nickname", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = SubtleBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Auth Type Selection
                Text(
                    text = "AUTHENTICATION METHOD",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuthTypeButton(
                        label = "Offline / Cracked",
                        selected = authType == AuthType.OFFLINE,
                        onClick = { authType = AuthType.OFFLINE },
                        modifier = Modifier.weight(1f)
                    )
                    AuthTypeButton(
                        label = "Microsoft OAuth 2.0",
                        selected = authType == AuthType.MICROSOFT,
                        onClick = { authType = AuthType.MICROSOFT },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Protocol Version Selection
                Text(
                    text = "TARGET MINECRAFT VERSION",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    versionList.take(4).forEach { ver ->
                        VersionChip(
                            version = ver,
                            selected = mcVersion == ver,
                            onClick = { mcVersion = ver },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    versionList.drop(4).forEach { ver ->
                        VersionChip(
                            version = ver,
                            selected = mcVersion == ver,
                            onClick = { mcVersion = ver },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Connect Submit Button
                Button(
                    onClick = {
                        val parsedPort = port.toIntOrNull() ?: 25565
                        val newConfig = currentConfig.copy(
                            host = host.trim(),
                            port = parsedPort,
                            username = username.trim().ifEmpty { "Player_AFK" },
                            authType = authType,
                            mcVersion = mcVersion,
                            autoReconnect = autoReconnect
                        )
                        onConnect(newConfig)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_connect_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = Color(0xFF003918)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch Connection", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun AuthTypeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) NeonGreen.copy(alpha = 0.2f) else SurfaceContainerHigh,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, NeonGreen) else androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) NeonGreen else TextMutedGray,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun VersionChip(
    version: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (selected) VibrantCyan.copy(alpha = 0.25f) else SurfaceContainerHigh,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, VibrantCyan) else null,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = version,
                color = if (selected) VibrantCyan else TextMutedGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
