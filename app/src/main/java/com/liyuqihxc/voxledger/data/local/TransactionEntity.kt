package com.liyuqihxc.voxledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.liyuqihxc.voxledger.model.Transaction
import com.liyuqihxc.voxledger.model.TransactionType
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: TransactionType,
    val amountCents: Long,
    val category: String,
    val description: String,
    val dateEpochDay: Long,
    val sourceText: String,
    val reimbursable: Boolean,
    val tripName: String?,
    val createdAtEpochMilli: Long,
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    type = type,
    amountCents = amountCents,
    category = category,
    description = description,
    date = LocalDate.ofEpochDay(dateEpochDay),
    sourceText = sourceText,
    reimbursable = reimbursable,
    tripName = tripName,
    createdAt = Instant.ofEpochMilli(createdAtEpochMilli),
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    type = type,
    amountCents = amountCents,
    category = category,
    description = description,
    dateEpochDay = date.toEpochDay(),
    sourceText = sourceText,
    reimbursable = reimbursable,
    tripName = tripName,
    createdAtEpochMilli = createdAt.toEpochMilli(),
)
