package com.liyuqihxc.voxledger.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liyuqihxc.voxledger.logic.Money
import com.liyuqihxc.voxledger.model.Categories
import com.liyuqihxc.voxledger.model.Transaction
import com.liyuqihxc.voxledger.model.TransactionType
import com.liyuqihxc.voxledger.ui.theme.ExpenseRed
import com.liyuqihxc.voxledger.ui.theme.IncomeGreen

/** A single transaction row used by the home, history and business screens. */
@Composable
fun TransactionRow(
    tx: Transaction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null,
) {
    val category = Categories.byId(tx.category)
    val amountColor = when (tx.type) {
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.INCOME, TransactionType.REFUND -> IncomeGreen
        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val amountText = when (tx.type) {
        TransactionType.EXPENSE -> "-${Money.formatYuan(tx.amountCents)}"
        TransactionType.INCOME, TransactionType.REFUND -> "+${Money.formatYuan(tx.amountCents)}"
        TransactionType.TRANSFER -> Money.formatYuan(tx.amountCents)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = category?.emoji ?: "📦", fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = tx.description.ifBlank { category?.name ?: tx.category },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category?.name ?: tx.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (tx.reimbursable) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "出差",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                tx.tripName?.takeIf { it.isNotBlank() }?.let { name ->
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = amountText, color = amountColor, style = MaterialTheme.typography.titleMedium)
            Text(
                text = tx.date.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing?.invoke()
    }
}
