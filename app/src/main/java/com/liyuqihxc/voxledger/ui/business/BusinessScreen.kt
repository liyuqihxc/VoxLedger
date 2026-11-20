package com.liyuqihxc.voxledger.ui.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.liyuqihxc.voxledger.logic.Money
import com.liyuqihxc.voxledger.ui.components.TransactionRow
import com.liyuqihxc.voxledger.ui.rememberAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessScreen(
    viewModel: BusinessViewModel = viewModel(factory = BusinessViewModel.factory(rememberAppContainer())),
) {
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    val selectedTrip by viewModel.selectedTripName.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val totalCents by viewModel.totalCents.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("出差报销") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("出差支出合计", style = MaterialTheme.typography.labelMedium)
                        Text(
                            Money.formatYuan(totalCents),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            if (trips.isNotEmpty()) {
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            FilterChip(
                                selected = selectedTrip == null,
                                onClick = { viewModel.selectTrip(null) },
                                label = { Text("全部") },
                            )
                        }
                        items(trips) { trip ->
                            FilterChip(
                                selected = selectedTrip == trip,
                                onClick = { viewModel.selectTrip(trip) },
                                label = { Text(trip) },
                            )
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        "暂无出差记录。记账时打开「出差」开关即可归入此处。",
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
