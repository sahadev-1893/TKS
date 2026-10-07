package com.example.data.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class SupabaseConfig(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("supabase_config_prefs", Context.MODE_PRIVATE)

    var supabaseUrl: String
        get() {
            val saved = prefs.getString("supabase_url", "") ?: ""
            if (saved.isNotBlank()) return saved
            val envUrl = runCatching { BuildConfig.SUPABASE_URL }.getOrDefault("")
            return if (envUrl.isNotBlank() && !envUrl.contains("your-project.supabase.co")) envUrl else ""
        }
        set(value) = prefs.edit().putString("supabase_url", value.trim()).apply()

    var supabaseKey: String
        get() {
            val saved = prefs.getString("supabase_key", "") ?: ""
            if (saved.isNotBlank()) return saved
            val envKey = runCatching { BuildConfig.SUPABASE_ANON_KEY }.getOrDefault("")
            return if (envKey.isNotBlank() && !envKey.contains("your-supabase-anon-key") && !envKey.contains("your-anon-key-here")) envKey else ""
        }
        set(value) = prefs.edit().putString("supabase_key", value.trim()).apply()

    var isConnected: Boolean
        get() = prefs.getBoolean("supabase_is_connected", false)
        set(value) = prefs.edit().putBoolean("supabase_is_connected", value).apply()

    var lastSyncTimestamp: Long
        get() = prefs.getLong("supabase_last_sync", 0L)
        set(value) = prefs.edit().putLong("supabase_last_sync", value).apply()

    var autoSyncEnabled: Boolean
        get() = prefs.getBoolean("supabase_auto_sync", true)
        set(value) = prefs.edit().putBoolean("supabase_auto_sync", value).apply()

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()

    val hasEnvConfig: Boolean
        get() {
            val envUrl = runCatching { BuildConfig.SUPABASE_URL }.getOrDefault("")
            val envKey = runCatching { BuildConfig.SUPABASE_ANON_KEY }.getOrDefault("")
            return envUrl.isNotBlank() && !envUrl.contains("your-project.supabase.co") &&
                   envKey.isNotBlank() && !envKey.contains("your-supabase-anon-key")
        }

    fun clearSavedCredentials() {
        prefs.edit().clear().apply()
    }
}
