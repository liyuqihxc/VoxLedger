package com.liyuqihxc.voxledger.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.liyuqihxc.voxledger.model.TransactionType
import com.liyuqihxc.voxledger.ui.components.TransactionRow
import com.liyuqihxc.voxledger.ui.rememberAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(rememberAppContainer())),
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("明细") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("全部") },
                )
                FilterChip(
                    selected = filterType == TransactionType.EXPENSE,
                    onClick = { viewModel.setFilter(TransactionType.EXPENSE) },
                    label = { Text("支出") },
                )
                FilterChip(
                    selected = filterType == TransactionType.INCOME,
                    onClick = { viewModel.setFilter(TransactionType.INCOME) },
                    label = { Text("收入") },
                )
            }

            if (transactions.isEmpty()) {
                Text(
                    "暂无记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionRow(
                            tx = tx,
                            trailing = {
                                IconButton(onClick = { viewModel.delete(tx) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
