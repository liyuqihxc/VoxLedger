package com.liyuqihxc.voxledger.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.liyuqihxc.voxledger.VoxLedgerApp
import com.liyuqihxc.voxledger.di.AppContainer

@Composable
fun rememberAppContainer(): AppContainer =
    (LocalContext.current.applicationContext as VoxLedgerApp).container
