package com.example.data.api

data class WebSource(
    val title: String,
    val url: String,
    val snippet: String,
    val domain: String
)

data class AiResponseResult(
    val text: String,
    val thinkingProcess: String? = null,
    val thinkingDurationSeconds: Int = 0,
    val searchQueries: List<String> = emptyList(),
    val sources: List<WebSource> = emptyList(),
    val modelUsed: String = "DeepSeek-V3",
    val isDeepThink: Boolean = false,
    val isWebSearch: Boolean = false,
    val metrics: EfficiencyMetrics? = null
)

