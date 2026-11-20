package com.liyuqihxc.voxledger.ui.record

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.liyuqihxc.voxledger.data.model.ModelManager
import com.liyuqihxc.voxledger.data.remote.TransactionParser
import com.liyuqihxc.voxledger.data.repository.TransactionRepository
import com.liyuqihxc.voxledger.di.AppContainer
import com.liyuqihxc.voxledger.model.ParseContext
import com.liyuqihxc.voxledger.model.TransactionDraft
import com.liyuqihxc.voxledger.speech.SherpaOnnxEngine
import com.liyuqihxc.voxledger.speech.SpeechRecognitionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class RecordViewModel(
    private val parser: TransactionParser,
    private val repository: TransactionRepository,
    private val modelManager: ModelManager,
    private val appContext: Context,
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing.asStateFlow()

    private val _drafts = MutableStateFlow<List<TransactionDraft>>(emptyList())
    val drafts: StateFlow<List<TransactionDraft>> = _drafts.asStateFlow()

    private val _warnings = MutableStateFlow<List<String>>(emptyList())
    val warnings: StateFlow<List<String>> = _warnings.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _parseCount = MutableStateFlow(0)
    val parseCount: StateFlow<Int> = _parseCount.asStateFlow()

    private val _modelReady = MutableStateFlow(false)
    val modelReady: StateFlow<Boolean> = _modelReady.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _engineReady = MutableStateFlow(false)
    val engineReady: StateFlow<Boolean> = _engineReady.asStateFlow()

    private var engine: SpeechRecognitionEngine? = null

    init {
        android.util.Log.i("VL", "init start")
        viewModelScope.launch {
            val ready = modelManager.isModelReady()
            android.util.Log.i("VL", "model ready=$ready")
            if (ready) {
                _modelReady.value = true
                prepareEngine()
            } else {
                _modelReady.value = false
            }
        }
    }

    private fun prepareEngine() {
        android.util.Log.i("VL", "prepareEngine start")
        val e = SherpaOnnxEngine(
            context = appContext,
            modelPath = modelManager.modelPath,
            tokensPath = modelManager.tokensPath,
        )
        android.util.Log.i("VL", "engine constructed")
        engine = e
        _engineReady.value = false
        e.prepare(
            onReady = { _engineReady.value = true },
            onError = { _message.value = it },
        )
        android.util.Log.i("VL", "prepare called")
    }

    fun downloadModel() {
        if (_downloadProgress.value != null) return // already downloading
        viewModelScope.launch {
            _downloadProgress.value = 0f
            val result = modelManager.download { progress ->
                _downloadProgress.value = progress
            }
            _downloadProgress.value = null
            result.onSuccess {
                _modelReady.value = true
                prepareEngine()
            }.onFailure { error ->
                _message.value = "模型下载失败: ${error.message ?: "未知错误"}"
            }
        }
    }

    fun updateInputText(value: String) {
        _inputText.value = value
    }

    fun toggleRecording() {
        if (_isRecording.value) stopRecording() else startRecording()
    }

    fun startRecording() {
        if (_isRecording.value) return
        if (!_engineReady.value) {
            _message.value = "离线识别引擎加载中，请稍候…"
            return
        }
        val engine = engine ?: return
        _partialText.value = ""
        _isRecording.value = true
        engine.start(object : SpeechRecognitionEngine.Listener {
            override fun onPartial(text: String) {
                _partialText.value = text
            }

            override fun onFinal(text: String) {
                _inputText.value = text
                _partialText.value = ""
                _isRecording.value = false
            }

            override fun onError(message: String) {
                _message.value = message
                _isRecording.value = false
            }
        })
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        val engine = engine ?: return
        engine.stop()
        _isRecording.value = false
    }

    fun parse() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) {
            _message.value = "请先输入或说出记账内容"
            return
        }
        viewModelScope.launch {
            _isParsing.value = true
            _warnings.value = emptyList()
            _drafts.value = emptyList()
            val result = parser.parse(text, ParseContext(today = LocalDate.now()))
            _drafts.value = result.drafts
            _warnings.value = result.warnings
            _parseCount.value += 1
            _isParsing.value = false
            if (result.drafts.isEmpty()) {
                _message.value = result.warnings.firstOrNull() ?: "未识别到交易"
            }
        }
    }

    fun updateDraft(index: Int, draft: TransactionDraft) {
        val list = _drafts.value.toMutableList()
        if (index in list.indices) list[index] = draft
        _drafts.value = list
    }

    fun removeDraft(index: Int) {
        val list = _drafts.value.toMutableList()
        if (index in list.indices) list.removeAt(index)
        _drafts.value = list
    }

    fun save(onSaved: () -> Unit) {
        val complete = _drafts.value.filter { it.isComplete }
        if (complete.isEmpty()) {
            _message.value = "没有可保存的记录，请补全金额"
            return
        }
        viewModelScope.launch {
            repository.addAll(complete, _inputText.value.trim())
            onSaved()
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    override fun onCleared() {
        engine?.destroy()
        super.onCleared()
    }

    companion object {
        fun factory(container: AppContainer, appContext: Context): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    RecordViewModel(
                        parser = container.transactionParser,
                        repository = container.transactionRepository,
                        modelManager = container.modelManager,
                        appContext = appContext,
                    )
                }
            }
    }
}
