package com.example.ui.viewmodel

import com.example.data.api.WebSource
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity

data class ChatUiState(
    val sessions: List<ChatSessionEntity> = emptyList(),
    val currentSessionId: String? = null,
    val currentSessionTitle: String = "محادثة جديدة",
    val messages: List<ChatMessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val isDeepThink: Boolean = false,
    val isWebSearch: Boolean = false,
    val activeModel: String = "DeepSeek-V3",
    val currentThinkingStream: String? = null,
    val currentStreamingAnswer: String? = null,
    val currentLiveSpeedTps: Double = 0.0,
    val apiKey: String = "",
    val errorMessage: String? = null,
    val isDrawerOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isUltraSpeedEnabled: Boolean = true,
    val isSmartCacheEnabled: Boolean = true,
    val showEfficiencyMetrics: Boolean = true
)
