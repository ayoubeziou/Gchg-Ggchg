package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("deepseek_prefs", Context.MODE_PRIVATE)

    var deepSeekApiKey: String
        get() = prefs.getString("key_deepseek_api_key", "") ?: ""
        set(value) = prefs.edit().putString("key_deepseek_api_key", value.trim()).apply()

    var activeModel: String
        get() = prefs.getString("key_active_model", MODEL_DEEPSEEK_V3) ?: MODEL_DEEPSEEK_V3
        set(value) = prefs.edit().putString("key_active_model", value).apply()

    var isDeepThinkActive: Boolean
        get() = prefs.getBoolean("key_deep_think_active", false)
        set(value) = prefs.edit().putBoolean("key_deep_think_active", value).apply()

    var isWebSearchActive: Boolean
        get() = prefs.getBoolean("key_web_search_active", false)
        set(value) = prefs.edit().putBoolean("key_web_search_active", value).apply()

    var activeSessionId: String?
        get() = prefs.getString("key_active_session_id", null)
        set(value) = prefs.edit().putString("key_active_session_id", value).apply()

    var isUltraSpeedEnabled: Boolean
        get() = prefs.getBoolean("key_ultra_speed_enabled", true)
        set(value) = prefs.edit().putBoolean("key_ultra_speed_enabled", value).apply()

    var isSmartCacheEnabled: Boolean
        get() = prefs.getBoolean("key_smart_cache_enabled", true)
        set(value) = prefs.edit().putBoolean("key_smart_cache_enabled", value).apply()

    var showEfficiencyMetrics: Boolean
        get() = prefs.getBoolean("key_show_efficiency_metrics", true)
        set(value) = prefs.edit().putBoolean("key_show_efficiency_metrics", value).apply()

    companion object {
        const val MODEL_DEEPSEEK_V3 = "deepseek-v3"
        const val MODEL_DEEPSEEK_R1 = "deepseek-r1"
        const val MODEL_DEEPSEEK_V3_ULTRA = "deepseek-v3-ultra"
    }
}

