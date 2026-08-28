package com.example.model

enum class ChatFilter {
    ALL,
    SYSTEM,
    PLAYER,
    WHISPER
}

enum class MessageType {
    SYSTEM,
    PLAYER,
    WHISPER,
    ACTION,
    COMMAND,
    ERROR
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rawText: String,
    val plainText: String = rawText,
    val sender: String = "System",
    val type: MessageType = MessageType.SYSTEM,
    val timestampMillis: Long = System.currentTimeMillis()
)
