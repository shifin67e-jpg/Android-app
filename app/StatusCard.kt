package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BotStatus
import com.example.model.ConnectionState
import com.example.ui.theme.*
import com.example.util.PulsingStatusDot
import com.example.util.glowBorder

@Composable
fun StatusCard(
    status: BotStatus,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onRestartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOnline = status.state == ConnectionState.ONLINE
    val isConnecting = status.state == ConnectionState.CONNECTING || status.state == ConnectionState.AUTHENTICATING || status.state == ConnectionState.RECONNECTING

    val stateColor = when {
        isConnecting -> WarningGold
        isOnline -> NeonGreen
        else -> AlertRed
    }

    val stateLabel = when (status.state) {
        ConnectionState.ONLINE -> "ONLINE"
        ConnectionState.CONNECTING -> "CONNECTING"
        ConnectionState.AUTHENTICATING -> "AUTH (MSA)"
        ConnectionState.RECONNECTING -> "RECONNECTING (${status.reconnectCountdown}s)"
        ConnectionState.DISCONNECTED -> "DISCONNECTED"
        ConnectionState.ERROR -> "ERROR"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .glowBorder(
                color = if (isOnline) NeonGreen else if (isConnecting) WarningGold else AlertRed,
                alpha = if (isOnline) 0.35f else 0.15f
            )
            .testTag("status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElevatedCardFill
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Status Badge + Server Address
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = stateColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, stateColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PulsingStatusDot(
                            isOnline = isOnline,
                            isConnecting = isConnecting,
                            size = 8.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stateLabel,
                            color = stateColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // Server badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Server IP",
                            tint = VibrantCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${status.currentServer}:${status.activePort}",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Row: Uptime | Ping | FPS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHigh)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric: Uptime
                MetricItem(
                    label = "UPTIME",
                    value = status.formattedUptime(),
                    icon = Icons.Default.Timer,
                    valueColor = if (isOnline) NeonGreen else TextMutedGray
                )

                Divider(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp),
                    color = SubtleBorder
                )

                // Metric: Ping
                MetricItem(
                    label = "PING",
                    value = if (isOnline) "${status.pingMs} ms" else "--",
                    icon = Icons.Default.NetworkCheck,
                    valueColor = when {
                        !isOnline -> TextMutedGray
                        status.pingMs < 50 -> NeonGreen
                        status.pingMs < 120 -> WarningGold
                        else -> AlertRed
                    }
                )

                Divider(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp),
                    color = SubtleBorder
                )

                // Metric: FPS / Engine
                MetricItem(
                    label = "FPS",
                    value = if (isOnline) "${status.fps}" else "0",
                    icon = Icons.Default.Speed,
                    valueColor = VibrantCyan
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Controls: Connect / Disconnect / Restart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isOnline || isConnecting) {
                    Button(
                        onClick = onDisconnectClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("disconnect_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertRed.copy(alpha = 0.2f),
                            contentColor = AlertRed
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Disconnect", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onRestartClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("restart_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = WarningGold
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningGold.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restart", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = onConnectClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("connect_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color(0xFF003918)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect AFK Bot", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMutedGray,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = TextMutedGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
