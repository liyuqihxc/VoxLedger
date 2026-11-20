package com.liyuqihxc.voxledger.data.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * Manages the offline SenseVoice model files, which live in the app's private
 * files directory (not in the APK). Downloads them on first use.
 */
class ModelManager(
    private val context: Context,
    private val client: OkHttpClient,
) {
    private val modelDir: File get() = File(context.filesDir, "sherpa-onnx")
    private val modelFile: File get() = File(modelDir, "model.int8.onnx")
    private val tokensFile: File get() = File(modelDir, "tokens.txt")

    val modelPath: String get() = modelFile.absolutePath
    val tokensPath: String get() = tokensFile.absolutePath

    fun isModelReady(): Boolean = modelFile.isFile && tokensFile.isFile

    suspend fun download(onProgress: (Float) -> Unit): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                modelDir.mkdirs()
                // Model dominates the size (~228MB), tokens is tiny.
                downloadFile(MODEL_URL, modelFile) { p -> onProgress(p * 0.98f) }
                downloadFile(TOKENS_URL, tokensFile) { p -> onProgress(0.98f + p * 0.02f) }
                onProgress(1f)
            }
        }

    private fun downloadFile(url: String, dest: File, onProgress: (Float) -> Unit) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code} for $url")
            val body = response.body ?: throw IOException("empty body for $url")
            val total = body.contentLength()
            val tmp = File(dest.parentFile, dest.name + ".tmp")
            body.byteStream().use { input ->
                tmp.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    var read = 0L
                    var n = input.read(buf)
                    while (n != -1) {
                        output.write(buf, 0, n)
                        read += n
                        if (total > 0) onProgress(read.toFloat() / total)
                        n = input.read(buf)
                    }
                }
            }
            if (!tmp.renameTo(dest)) {
                tmp.copyTo(dest, overwrite = true)
                tmp.delete()
            }
        }
    }

    companion object {
        // 国内网络可把 base 换成 hf-mirror.com（HuggingFace 镜像）：
        // https://hf-mirror.com/csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17/resolve/main/model.int8.onnx
        const val MODEL_URL =
            "https://huggingface.co/csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17/resolve/main/model.int8.onnx"
        const val TOKENS_URL =
            "https://huggingface.co/csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17/resolve/main/tokens.txt"
    }
}
