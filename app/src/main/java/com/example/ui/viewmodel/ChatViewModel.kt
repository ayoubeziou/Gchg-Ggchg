package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiRepository
import com.example.data.api.WebSource
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.preferences.UserPreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val chatDao = database.chatDao()
    private val prefs = UserPreferencesManager(application)
    private val aiRepository = AiRepository()

    private val _uiState = MutableStateFlow(
        ChatUiState(
            isDeepThink = prefs.isDeepThinkActive,
            isWebSearch = prefs.isWebSearchActive,
            activeModel = prefs.activeModel,
            apiKey = prefs.deepSeekApiKey,
            isUltraSpeedEnabled = prefs.isUltraSpeedEnabled,
            isSmartCacheEnabled = prefs.isSmartCacheEnabled,
            showEfficiencyMetrics = prefs.showEfficiencyMetrics
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var activeMessagesJob: Job? = null
    private var generationJob: Job? = null

    init {
        observeSessions()
    }

    private fun observeSessions() {
        viewModelScope.launch {
            chatDao.getAllSessions().collectLatest { sessionsList ->
                _uiState.update { it.copy(sessions = sessionsList) }

                val currentId = _uiState.value.currentSessionId
                if (currentId == null || sessionsList.none { it.id == currentId }) {
                    if (sessionsList.isNotEmpty()) {
                        selectSession(sessionsList.first().id)
                    } else {
                        createNewSession()
                    }
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        val session = _uiState.value.sessions.find { it.id == sessionId }
        _uiState.update {
            it.copy(
                currentSessionId = sessionId,
                currentSessionTitle = session?.title ?: "محادثة",
                isDrawerOpen = false
            )
        }
        prefs.activeSessionId = sessionId

        activeMessagesJob?.cancel()
        activeMessagesJob = viewModelScope.launch {
            chatDao.getMessagesForSession(sessionId).collectLatest { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            val newSession = ChatSessionEntity(
                id = UUID.randomUUID().toString(),
                title = "محادثة جديدة"
            )
            chatDao.insertSession(newSession)
            selectSession(newSession.id)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatDao.deleteSessionById(sessionId)
            if (_uiState.value.currentSessionId == sessionId) {
                _uiState.update { it.copy(currentSessionId = null, messages = emptyList()) }
            }
        }
    }

    fun toggleDeepThink() {
        val nextVal = !_uiState.value.isDeepThink
        _uiState.update {
            it.copy(
                isDeepThink = nextVal,
                activeModel = if (nextVal) "DeepSeek-R1" else "DeepSeek-V3"
            )
        }
        prefs.isDeepThinkActive = nextVal
    }

    fun toggleWebSearch() {
        val nextVal = !_uiState.value.isWebSearch
        _uiState.update { it.copy(isWebSearch = nextVal) }
        prefs.isWebSearchActive = nextVal
    }

    fun selectModel(model: String) {
        val isR1 = model.contains("R1", ignoreCase = true)
        _uiState.update {
            it.copy(
                activeModel = model,
                isDeepThink = isR1
            )
        }
        prefs.activeModel = model
        prefs.isDeepThinkActive = isR1
    }

    fun toggleDrawer(isOpen: Boolean? = null) {
        _uiState.update { it.copy(isDrawerOpen = isOpen ?: !it.isDrawerOpen) }
    }

    fun toggleSettings(isOpen: Boolean? = null) {
        _uiState.update { it.copy(isSettingsOpen = isOpen ?: !it.isSettingsOpen) }
    }

    fun updateApiKey(newKey: String) {
        prefs.deepSeekApiKey = newKey
        _uiState.update { it.copy(apiKey = newKey, isSettingsOpen = false) }
    }

    fun updateEfficiencySettings(ultraSpeed: Boolean, smartCache: Boolean, showMetrics: Boolean) {
        prefs.isUltraSpeedEnabled = ultraSpeed
        prefs.isSmartCacheEnabled = smartCache
        prefs.showEfficiencyMetrics = showMetrics
        _uiState.update {
            it.copy(
                isUltraSpeedEnabled = ultraSpeed,
                isSmartCacheEnabled = smartCache,
                showEfficiencyMetrics = showMetrics,
                isSettingsOpen = false
            )
        }
    }

    fun clearCurrentChat() {
        val sessionId = _uiState.value.currentSessionId ?: return
        viewModelScope.launch {
            chatDao.clearMessagesForSession(sessionId)
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        generationJob = null
        _uiState.update {
            it.copy(
                isGenerating = false,
                currentThinkingStream = null,
                currentStreamingAnswer = null,
                currentLiveSpeedTps = 0.0
            )
        }
    }

    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank() || _uiState.value.isGenerating) return

        var sessionId = _uiState.value.currentSessionId
        if (sessionId == null) {
            val newSession = ChatSessionEntity(
                id = UUID.randomUUID().toString(),
                title = trimmed.take(30)
            )
            viewModelScope.launch {
                chatDao.insertSession(newSession)
                selectSession(newSession.id)
                performSendMessage(newSession.id, trimmed)
            }
            return
        }

        performSendMessage(sessionId, trimmed)
    }

    private fun performSendMessage(sessionId: String, prompt: String) {
        val isDeepThink = _uiState.value.isDeepThink
        val isWebSearch = _uiState.value.isWebSearch
        val activeModel = _uiState.value.activeModel
        val apiKey = _uiState.value.apiKey

        generationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    currentThinkingStream = if (isDeepThink) "جاري بدء التفكير والتحليل المنطقي..." else null,
                    currentStreamingAnswer = null,
                    currentLiveSpeedTps = 0.0,
                    errorMessage = null
                )
            }

            // 1. Insert User Message
            val userMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "user",
                content = prompt,
                isDeepThink = isDeepThink,
                isWebSearch = isWebSearch,
                modelName = activeModel
            )
            chatDao.insertMessage(userMsg)

            // Update session title if first message
            val currentMsgs = chatDao.getMessagesListForSession(sessionId)
            if (currentMsgs.size <= 1) {
                val title = if (prompt.length > 30) prompt.take(30) + "..." else prompt
                val session = chatDao.getSessionById(sessionId)
                if (session != null) {
                    chatDao.updateSession(session.copy(title = title, updatedAt = System.currentTimeMillis()))
                }
            }

            // 2. Prepare History
            val history = currentMsgs.map { it.role to it.content }

            try {
                // 3. Call AI repository with Real-Time Streaming and Efficiency Callbacks
                val result = aiRepository.generateResponse(
                    prompt = prompt,
                    history = history,
                    isDeepThink = isDeepThink,
                    isWebSearch = isWebSearch,
                    customApiKey = apiKey,
                    activeModel = activeModel,
                    onThinkingUpdate = { updateText ->
                        _uiState.update { it.copy(currentThinkingStream = updateText) }
                    },
                    onTokenUpdate = { streamText ->
                        _uiState.update { it.copy(currentStreamingAnswer = streamText) }
                    },
                    onMetricsUpdate = { metrics ->
                        _uiState.update { it.copy(currentLiveSpeedTps = metrics.tokensPerSecond) }
                    }
                )

                // 4. Save Assistant Response to Database including efficiency metrics
                val sourcesJson = serializeSources(result.sources)
                val queriesString = result.searchQueries.joinToString(", ")

                val assistantMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = "assistant",
                    content = result.text,
                    thinkingProcess = result.thinkingProcess,
                    thinkingDurationSeconds = result.thinkingDurationSeconds,
                    isDeepThink = isDeepThink,
                    isWebSearch = isWebSearch,
                    webSearchQueries = queriesString.ifBlank { null },
                    webSourcesJson = sourcesJson,
                    modelName = result.modelUsed,
                    timestamp = System.currentTimeMillis(),
                    tokensPerSec = result.metrics?.tokensPerSecond ?: 0.0,
                    timeToFirstTokenMs = result.metrics?.timeToFirstTokenMs ?: 0L,
                    totalTimeMs = result.metrics?.totalTimeMs ?: 0L,
                    accelerationEngine = result.metrics?.hardwareAcceleration
                )
                chatDao.insertMessage(assistantMsg)

                // Update session timestamp
                val session = chatDao.getSessionById(sessionId)
                if (session != null) {
                    chatDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = "assistant",
                    content = "حدث خطأ أثناء معالجة الطلب: ${e.message ?: "يرجى المحاولة مرة أخرى"}",
                    isDeepThink = isDeepThink,
                    isWebSearch = isWebSearch,
                    modelName = activeModel,
                    isError = true
                )
                chatDao.insertMessage(errorMsg)
                _uiState.update { it.copy(errorMessage = e.message) }
            } finally {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        currentThinkingStream = null,
                        currentStreamingAnswer = null,
                        currentLiveSpeedTps = 0.0
                    )
                }
            }
        }
    }

    private fun serializeSources(sources: List<WebSource>): String? {
        if (sources.isEmpty()) return null
        val array = JSONArray()
        for (source in sources) {
            array.put(JSONObject().apply {
                put("title", source.title)
                put("url", source.url)
                put("snippet", source.snippet)
                put("domain", source.domain)
            })
        }
        return array.toString()
    }

    fun deserializeSources(json: String?): List<WebSource> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<WebSource>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WebSource(
                        title = obj.optString("title", ""),
                        url = obj.optString("url", ""),
                        snippet = obj.optString("snippet", ""),
                        domain = obj.optString("domain", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
