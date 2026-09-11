package com.example.data.api

data class EfficiencyMetrics(
    val tokensPerSecond: Double = 0.0,
    val timeToFirstTokenMs: Long = 0,
    val totalTimeMs: Long = 0,
    val totalTokens: Int = 0,
    val memoryCacheHits: Int = 0,
    val compressionRatio: Double = 1.0,
    val hardwareAcceleration: String = "Hexagon NPU / Vulkan Pipeline"
)
