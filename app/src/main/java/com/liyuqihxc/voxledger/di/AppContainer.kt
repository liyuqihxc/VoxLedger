package com.liyuqihxc.voxledger.di

import android.content.Context
import com.liyuqihxc.voxledger.data.local.AppDatabase
import com.liyuqihxc.voxledger.data.model.ModelManager
import com.liyuqihxc.voxledger.data.remote.DeepSeekClient
import com.liyuqihxc.voxledger.data.remote.DeepSeekTransactionParser
import com.liyuqihxc.voxledger.data.remote.TransactionParser
import com.liyuqihxc.voxledger.data.repository.SettingsRepository
import com.liyuqihxc.voxledger.data.repository.TransactionRepository
import com.liyuqihxc.voxledger.data.security.ApiKeyStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Simple manual dependency container (no DI framework). Owns all long-lived
 * singletons and is created once in the Application.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: AppDatabase = AppDatabase.getInstance(appContext)
    val apiKeyStore: ApiKeyStore = ApiKeyStore()
    val settingsRepository: SettingsRepository = SettingsRepository(appContext, apiKeyStore)
    val transactionRepository: TransactionRepository =
        TransactionRepository(database.transactionDao())

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        // Level.BASIC logs request/response lines only, never headers or bodies,
        // so the Authorization header (API key) is not written to logcat.
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val deepSeekClient: DeepSeekClient = DeepSeekClient(okHttpClient)

    val modelManager: ModelManager = ModelManager(appContext, okHttpClient)

    val transactionParser: TransactionParser =
        DeepSeekTransactionParser(deepSeekClient, settingsRepository)
}
