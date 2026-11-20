package com.liyuqihxc.voxledger.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.liyuqihxc.voxledger.data.repository.SettingsRepository
import com.liyuqihxc.voxledger.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settings: SettingsRepository) : ViewModel() {

    val apiKey: StateFlow<String?> = settings.apiKey
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val model: StateFlow<String> = settings.model
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsRepository.DEFAULT_MODEL)

    val speechEngine: StateFlow<String> = settings.speechEngine
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsRepository.ENGINE_SYSTEM)

    private val _apiKeyInput = MutableStateFlow("")
    val apiKeyInput: StateFlow<String> = _apiKeyInput.asStateFlow()

    private val _modelInput = MutableStateFlow("")
    val modelInput: StateFlow<String> = _modelInput.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun updateApiKeyInput(value: String) {
        _apiKeyInput.value = value
    }

    fun updateModelInput(value: String) {
        _modelInput.value = value
    }

    fun saveApiKey() {
        viewModelScope.launch {
            settings.setApiKey(_apiKeyInput.value)
            _apiKeyInput.value = ""
            _message.value = "API Key 已保存"
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            settings.setApiKey("")
            _message.value = "API Key 已清除"
        }
    }

    fun saveModel() {
        viewModelScope.launch {
            settings.setModel(_modelInput.value.ifBlank { SettingsRepository.DEFAULT_MODEL })
            _message.value = "模型已保存"
        }
    }

    fun selectSpeechEngine(engine: String) {
        viewModelScope.launch {
            settings.setSpeechEngine(engine)
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(container.settingsRepository) }
        }
    }
}
