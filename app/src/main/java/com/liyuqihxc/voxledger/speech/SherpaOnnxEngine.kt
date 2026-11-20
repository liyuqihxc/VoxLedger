package com.liyuqihxc.voxledger.speech

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig
import java.io.ByteArrayOutputStream

/**
 * Fully offline speech recognition via Sherpa-ONNX + SenseVoice Small (int8).
 * The model is loaded from an absolute file path in the app's private
 * directory (downloaded on first use by [com.liyuqihxc.voxledger.data.model.ModelManager]).
 *
 * SenseVoice is a non-streaming model. Recording and decoding are strictly
 * serialized to avoid a native race: while recording we only collect PCM into
 * memory; only after [stop] do we create a stream, feed all samples via
 * `acceptWaveform`, then `decode`, all on one background thread.
 */
class SherpaOnnxEngine(
    private val context: Context,
    private val modelPath: String,
    private val tokensPath: String,
) : SpeechRecognitionEngine {

    private val mainHandler = Handler(Looper.getMainLooper())

    private var recognizer: OfflineRecognizer? = null
    private var audioRecord: AudioRecord? = null
    private var recordThread: Thread? = null
    private var listener: SpeechRecognitionEngine.Listener? = null

    @Volatile
    private var recording = false

    private val pcm = ByteArrayOutputStream()

    override fun prepare(onReady: () -> Unit, onError: (String) -> Unit) {
        android.util.Log.i("VL", "prepare() enter model=$modelPath")
        Thread {
            try {
                android.util.Log.i(
                    "VL",
                    "loading model start model=$modelPath tokens=$tokensPath " +
                        "javaHeap=${Runtime.getRuntime().maxMemory() / 1024 / 1024}MB",
                )
                val config = OfflineRecognizerConfig(
                    featConfig = FeatureConfig(sampleRate = SAMPLE_RATE, featureDim = FEATURE_DIM),
                    modelConfig = OfflineModelConfig(
                        senseVoice = OfflineSenseVoiceModelConfig(
                            model = modelPath,
                            language = "zh",
                            useInverseTextNormalization = true,
                        ),
                        tokens = tokensPath,
                        numThreads = NUM_THREADS,
                        provider = "cpu",
                        debug = false,
                    ),
                    decodingMethod = "greedy_search",
                )
                // 用文件路径加载模型时，AssetManager 必须传 null（否则原生层会用
                // AssetManager 打开绝对路径失败并 exit(-1)）。见 sherpa-onnx issue #2562。
                recognizer = OfflineRecognizer(null, config)
                android.util.Log.i("VL", "loading model done")
                mainHandler.post(onReady)
            } catch (e: Throwable) {
                android.util.Log.e("VL", "model load failed: ${e.message}", e)
                mainHandler.post { onError("本地识别引擎初始化失败: ${e.message}") }
            }
        }.start()
    }

    override fun start(listener: SpeechRecognitionEngine.Listener) {
        if (recognizer == null) {
            listener.onError("本地识别引擎未就绪，请稍候")
            return
        }
        this.listener = listener

        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
        val bufferSize = maxOf(minBuf * 2, SAMPLE_RATE * 2)
        val recorder = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize)
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            listener.onError("无法初始化麦克风（请检查录音权限）")
            recorder.release()
            return
        }

        pcm.reset()
        audioRecord = recorder
        recording = true
        recorder.startRecording()

        recordThread = Thread {
            val buffer = ShortArray(SAMPLE_RATE / 10) // 100 ms chunks
            try {
                while (recording) {
                    val n = recorder.read(buffer, 0, buffer.size, AudioRecord.READ_NON_BLOCKING)
                    when {
                        n > 0 -> for (i in 0 until n) {
                            val s = buffer[i]
                            pcm.write(s.toInt() and 0xFF)
                            pcm.write((s.toInt() ushr 8) and 0xFF)
                        }
                        n == 0 -> Thread.sleep(20)
                        else -> break
                    }
                }
            } finally {
                runCatching { recorder.stop() }
                recorder.release()
                audioRecord = null
            }
        }.also { it.start() }
    }

    override fun stop() {
        recording = false
        runCatching { audioRecord?.stop() }
        recordThread?.join(2000)
        recordThread = null

        val recognizer = recognizer ?: return
        val currentListener = listener ?: return
        val samples = pcmBytesToFloats(pcm.toByteArray())

        Thread {
            if (samples.isEmpty()) {
                mainHandler.post { currentListener.onError("未录到声音，请重试") }
                return@Thread
            }
            try {
                val stream = recognizer.createStream()
                try {
                    stream.acceptWaveform(samples, SAMPLE_RATE)
                    recognizer.decode(stream)
                    val text = recognizer.getResult(stream).text.orEmpty()
                    mainHandler.post { currentListener.onFinal(text) }
                } finally {
                    runCatching { stream.release() }
                }
            } catch (e: Throwable) {
                mainHandler.post { currentListener.onError("识别失败: ${e.message}") }
            }
        }.start()
    }

    override fun destroy() {
        recording = false
        runCatching { audioRecord?.stop() }
        recordThread?.join(5000)
        recordThread = null
        runCatching { audioRecord?.release() }
        runCatching { recognizer?.release() }
        audioRecord = null
        recognizer = null
        listener = null
    }

    private fun pcmBytesToFloats(bytes: ByteArray): FloatArray {
        val n = bytes.size / 2
        val floats = FloatArray(n)
        for (i in 0 until n) {
            val lo = bytes[2 * i].toInt() and 0xFF
            val hi = bytes[2 * i + 1].toInt() and 0xFF
            val s = (lo or (hi shl 8)).toShort()
            floats[i] = s / 32768f
        }
        return floats
    }

    private companion object {
        const val SAMPLE_RATE = 16000
        const val FEATURE_DIM = 80
        const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val NUM_THREADS = 2
    }
}
