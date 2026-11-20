package com.liyuqihxc.voxledger.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liyuqihxc.voxledger.logic.Money
import com.liyuqihxc.voxledger.ui.components.TransactionRow
import com.liyuqihxc.voxledger.ui.rememberAppContainer
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecord: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(rememberAppContainer())),
) {
    val yearMonth by viewModel.yearMonth.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val monthLabel = yearMonth.format(DateTimeFormatter.ofPattern("yyyy年M月"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("言账") },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = viewModel::previousMonth) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上个月")
                        }
                        Text(monthLabel, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = viewModel::nextMonth) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下个月")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onRecord) {
                Icon(Icons.Default.Add, contentDescription = "记一笔")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        ) {
            item { SummaryCard(summary) }
            item { Spacer(Modifier.height(16.dp)) }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        "本月还没有记录，点右下角 + 开始记账",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TransactionRow(tx = tx)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: com.liyuqihxc.voxledger.data.repository.MonthSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("本月结余", style = MaterialTheme.typography.labelMedium)
            Text(
                Money.formatYuan(summary.balanceCents),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("支出", style = MaterialTheme.typography.labelMedium)
                    Text(
                        Money.formatYuan(summary.expenseCents),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("收入", style = MaterialTheme.typography.labelMedium)
                    Text(
                        Money.formatYuan(summary.incomeCents),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
