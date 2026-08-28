package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.engine.BotEngine
import com.example.model.AntiAfkConfig
import com.example.model.AuthType
import com.example.ui.components.ServerPresetDialog
import com.example.ui.theme.*
import com.example.util.glowBorder

@Composable
fun SessionsScreen(
    engine: BotEngine,
    modifier: Modifier = Modifier
) {
    val config by engine.config.collectAsState()
    val status by engine.status.collectAsState()

    var showEditPreset by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("sessions_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Column {
            Text(
                text = "Session & Engine Grid",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Anti-AFK automation engine and account management",
                color = TextMutedGray,
                fontSize = 13.sp
            )
        }

        // Active Session Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glowBorder(NeonGreen, alpha = 0.3f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedCardFill)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = config.profileName,
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Username: ${config.username}",
                                color = VibrantCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = { showEditPreset = true },
                        modifier = Modifier.testTag("edit_session_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Session", tint = TextMutedGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Divider(color = SubtleBorder)

                Spacer(modifier = Modifier.height(14.dp))

                // Session Meta Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SessionDetailPill(
                        label = "TARGET SERVER",
                        value = "${config.host}:${config.port}"
                    )
                    SessionDetailPill(
                        label = "AUTH TYPE",
                        value = if (config.authType == AuthType.MICROSOFT) "Microsoft MSA" else "Offline UUID"
                    )
                    SessionDetailPill(
                        label = "VERSION",
                        value = config.mcVersion
                    )
                }
            }
        }

        // B. Powerhouse Anti-AFK Automation Engine Grid
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .glowBorder(VibrantCyan, alpha = 0.25f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedCardFill)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Section Title + Master Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = VibrantCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Anti-AFK Engine",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Switch(
                        checked = config.antiAfk.enabled,
                        onCheckedChange = { isEnabled ->
                            engine.updateAntiAfk(config.antiAfk.copy(enabled = isEnabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF003918),
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = TextMutedGray,
                            uncheckedTrackColor = SurfaceContainerHigh
                        ),
                        modifier = Modifier.testTag("anti_afk_master_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-Walk Toggle & Interval
                EngineToggleRow(
                    title = "Auto-Walk Micro-Step",
                    subtitle = "Performs short forward step (300ms) to reset idle timer",
                    checked = config.antiAfk.autoWalk,
                    onCheckedChange = {
                        engine.updateAntiAfk(config.antiAfk.copy(autoWalk = it))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Anti-Kick Head Rotation
                EngineToggleRow(
                    title = "Anti-Kick Rotation (Yaw & Pitch)",
                    subtitle = "Randomizes camera angle jitter to mimic human look behavior",
                    checked = config.antiAfk.antiKickRotation,
                    onCheckedChange = {
                        engine.updateAntiAfk(config.antiAfk.copy(antiKickRotation = it))
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Rotation Interval Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cycle Interval",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${config.antiAfk.rotationIntervalSeconds} seconds",
                            color = VibrantCyan,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = config.antiAfk.rotationIntervalSeconds.toFloat(),
                        onValueChange = {
                            engine.updateAntiAfk(config.antiAfk.copy(rotationIntervalSeconds = it.toInt()))
                        },
                        valueRange = 5f..60f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = VibrantCyan,
                            activeTrackColor = VibrantCyan,
                            inactiveTrackColor = SurfaceContainerHighest
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto-Eat Threshold
                EngineToggleRow(
                    title = "Auto-Eat Threshold",
                    subtitle = "Consumes hotbar food when hunger drops below threshold",
                    checked = config.antiAfk.autoEat,
                    onCheckedChange = {
                        engine.updateAntiAfk(config.antiAfk.copy(autoEat = it))
                    }
                )

                if (config.antiAfk.autoEat) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Eat When Hunger Drops To",
                                color = TextMutedGray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${config.antiAfk.autoEatThreshold}/20 Drumsticks",
                                color = WarningGold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = config.antiAfk.autoEatThreshold.toFloat(),
                            onValueChange = {
                                engine.updateAntiAfk(config.antiAfk.copy(autoEatThreshold = it.toInt()))
                            },
                            valueRange = 6f..18f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = WarningGold,
                                activeTrackColor = WarningGold,
                                inactiveTrackColor = SurfaceContainerHighest
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto-Sneak
                EngineToggleRow(
                    title = "Intermittent Sneaking",
                    subtitle = "Toggles crouch randomly to prevent cliff falling & update status",
                    checked = config.antiAfk.autoSneak,
                    onCheckedChange = {
                        engine.updateAntiAfk(config.antiAfk.copy(autoSneak = it))
                    }
                )
            }
        }

        // Auto-Reconnect Exponential Backoff Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedCardFill)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Auto-Reconnect Queue",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Exponential backoff retry on server kick / restart",
                            color = TextMutedGray,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = config.autoReconnect,
                        onCheckedChange = {
                            engine.updateConfig(config.copy(autoReconnect = it))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF003918),
                            checkedTrackColor = NeonGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerHigh)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("1st: 10s delay", color = NeonGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("2nd: 30s delay", color = WarningGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("3rd+: 60s delay", color = VibrantCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }

    if (showEditPreset) {
        ServerPresetDialog(
            currentConfig = config,
            onDismiss = { showEditPreset = false },
            onConnect = { newConfig ->
                engine.connectBot(newConfig)
            }
        )
    }
}

@Composable
private fun SessionDetailPill(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = TextMutedGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = TextWhite,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EngineToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextMutedGray,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF003918),
                checkedTrackColor = NeonGreen
            )
        )
    }
}
