package com.liyuqihxc.voxledger

import android.app.Application
import android.util.Log
import com.liyuqihxc.voxledger.di.AppContainer
import java.io.File

class VoxLedgerApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        Log.i("VL", "app onCreate")
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("VL", "Uncaught in ${thread.name}", throwable)
            runCatching {
                File(filesDir, "crash.log").writeText(
                    "${thread.name}: ${throwable}\n${throwable.stackTraceToString()}",
                )
            }
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}
