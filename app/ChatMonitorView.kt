package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.ChatFilter
import com.example.model.ChatMessage
import com.example.model.MessageType
import com.example.ui.theme.*
import com.example.util.MinecraftColorParser
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatMonitorView(
    messages: List<ChatMessage>,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(ChatFilter.ALL) }
    var autoScrollEnabled by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    val filteredMessages = remember(messages, selectedFilter) {
        when (selectedFilter) {
            ChatFilter.ALL -> messages
            ChatFilter.SYSTEM -> messages.filter { it.type == MessageType.SYSTEM || it.type == MessageType.ACTION }
            ChatFilter.PLAYER -> messages.filter { it.type == MessageType.PLAYER || it.type == MessageType.COMMAND }
            ChatFilter.WHISPER -> messages.filter { it.type == MessageType.WHISPER }
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(filteredMessages.size, autoScrollEnabled) {
        if (autoScrollEnabled && filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ElevatedCardFill)
            .padding(12.dp)
            .testTag("chat_monitor_card")
    ) {
        // Controls Row: Filter Chips + Auto-Scroll + Clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                ChatFilterChip(
                    label = "All (${messages.size})",
                    selected = selectedFilter == ChatFilter.ALL,
                    onClick = { selectedFilter = ChatFilter.ALL }
                )
                ChatFilterChip(
                    label = "System",
                    selected = selectedFilter == ChatFilter.SYSTEM,
                    onClick = { selectedFilter = ChatFilter.SYSTEM }
                )
                ChatFilterChip(
                    label = "Players",
                    selected = selectedFilter == ChatFilter.PLAYER,
                    onClick = { selectedFilter = ChatFilter.PLAYER }
                )
                ChatFilterChip(
                    label = "Whispers",
                    selected = selectedFilter == ChatFilter.WHISPER,
                    onClick = { selectedFilter = ChatFilter.WHISPER }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Auto Scroll Toggle
                IconButton(
                    onClick = { autoScrollEnabled = !autoScrollEnabled },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (autoScrollEnabled) Icons.Default.VerticalAlignBottom else Icons.Default.Pause,
                        contentDescription = "Toggle Auto-Scroll",
                        tint = if (autoScrollEnabled) NeonGreen else TextMutedGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Clear Chat
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Chat",
                        tint = TextMutedGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Stream Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(DeepCharcoal)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            if (filteredMessages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = TextSubtle,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No chat messages in this view",
                            color = TextMutedGray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("chat_message_list"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredMessages, key = { it.id }) { msg ->
                        ChatMessageRow(msg = msg)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) NeonGreen.copy(alpha = 0.2f) else SurfaceContainerHigh,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)) else null
    ) {
        Text(
            text = label,
            color = if (selected) NeonGreen else TextMutedGray,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ChatMessageRow(msg: ChatMessage) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeString = remember(msg.timestampMillis) { timeFormat.format(Date(msg.timestampMillis)) }

    val parsedText = remember(msg.rawText) {
        MinecraftColorParser.parse(msg.rawText)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timestamp
        Text(
            text = "[$timeString]",
            color = TextSubtle,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 1.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Styled Minecraft text
        Text(
            text = parsedText,
            fontSize = 12.sp,
            fontFamily = FontFamily.Default,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
