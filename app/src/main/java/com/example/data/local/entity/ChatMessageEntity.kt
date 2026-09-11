package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val thinkingProcess: String? = null,
    val thinkingDurationSeconds: Int = 0,
    val isDeepThink: Boolean = false,
    val isWebSearch: Boolean = false,
    val webSearchQueries: String? = null,
    val webSourcesJson: String? = null,
    val modelName: String = "DeepSeek-V3",
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val tokensPerSec: Double = 0.0,
    val timeToFirstTokenMs: Long = 0,
    val totalTimeMs: Long = 0,
    val accelerationEngine: String? = null
)
