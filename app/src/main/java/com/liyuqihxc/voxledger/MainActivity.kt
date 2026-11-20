package com.liyuqihxc.voxledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.liyuqihxc.voxledger.ui.business.BusinessScreen
import com.liyuqihxc.voxledger.ui.history.HistoryScreen
import com.liyuqihxc.voxledger.ui.home.HomeScreen
import com.liyuqihxc.voxledger.ui.record.RecordScreen
import com.liyuqihxc.voxledger.ui.settings.SettingsScreen
import com.liyuqihxc.voxledger.ui.theme.VoxLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoxLedgerTheme {
                VoxLedgerRoot()
            }
        }
    }
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination("home", "首页", Icons.Default.Home),
    BottomDestination("history", "明细", Icons.AutoMirrored.Filled.List),
    BottomDestination("business", "出差", Icons.Default.Place),
    BottomDestination("settings", "设置", Icons.Default.Settings),
)

@Composable
private fun VoxLedgerRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomDestinations.map { it.route }) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
        ) {
            composable("home") {
                HomeScreen(onRecord = {
                    android.util.Log.i("VL", "navigate record")
                    navController.navigate("record")
                })
            }
            composable("history") { HistoryScreen() }
            composable("business") { BusinessScreen() }
            composable("settings") { SettingsScreen() }
            composable("record") {
                android.util.Log.i("VL", "record composable enter")
                RecordScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
        }
    }
}
