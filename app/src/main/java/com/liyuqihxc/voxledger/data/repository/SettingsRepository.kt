package com.liyuqihxc.voxledger.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.liyuqihxc.voxledger.data.security.ApiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Holds user settings in DataStore. The API key is stored AES-GCM encrypted
 * (via [ApiKeyStore]) and decrypted on demand.
 */
class SettingsRepository(
    private val context: Context,
    private val apiKeyStore: ApiKeyStore,
) {

    private object Keys {
        val API_KEY = stringPreferencesKey("api_key_encrypted")
        val MODEL = stringPreferencesKey("model")
        val SPEECH_ENGINE = stringPreferencesKey("speech_engine")
    }

    /** Decrypted DeepSeek API key, or null when unset. */
    val apiKey: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[Keys.API_KEY]?.let { apiKeyStore.decrypt(it) } }
        .flowOn(Dispatchers.Default)

    val model: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[Keys.MODEL] ?: DEFAULT_MODEL }

    val speechEngine: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[Keys.SPEECH_ENGINE] ?: ENGINE_SYSTEM }

    suspend fun setApiKey(key: String) {
        val trimmed = key.trim()
        context.dataStore.edit { prefs ->
            if (trimmed.isEmpty()) prefs.remove(Keys.API_KEY)
            else prefs[Keys.API_KEY] = apiKeyStore.encrypt(trimmed)
        }
    }

    suspend fun setModel(model: String) {
        context.dataStore.edit { it[Keys.MODEL] = model.trim() }
    }

    suspend fun setSpeechEngine(engine: String) {
        context.dataStore.edit { it[Keys.SPEECH_ENGINE] = engine }
    }

    companion object {
        const val DEFAULT_MODEL = "deepseek-chat"
        const val ENGINE_SYSTEM = "system"
        const val ENGINE_SHERPA = "sherpa"
    }
}
