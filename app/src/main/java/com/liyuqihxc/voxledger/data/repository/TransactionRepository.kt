package com.liyuqihxc.voxledger.data.repository

import com.liyuqihxc.voxledger.data.local.TransactionDao
import com.liyuqihxc.voxledger.data.local.toDomain
import com.liyuqihxc.voxledger.data.local.toEntity
import com.liyuqihxc.voxledger.model.Categories
import com.liyuqihxc.voxledger.model.Transaction
import com.liyuqihxc.voxledger.model.TransactionDraft
import com.liyuqihxc.voxledger.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TransactionRepository(private val dao: TransactionDao) {

    fun observeAll(): Flow<List<Transaction>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<Transaction>> =
        dao.observeBetween(start.toEpochDay(), end.toEpochDay())
            .map { list -> list.map { it.toDomain() } }

    fun observeReimbursable(): Flow<List<Transaction>> =
        dao.observeReimbursable().map { list -> list.map { it.toDomain() } }

    fun observeByTrip(tripName: String): Flow<List<Transaction>> =
        dao.observeByTrip(tripName).map { list -> list.map { it.toDomain() } }

    suspend fun getTripNames(): List<String> = dao.getTripNames()

    suspend fun add(draft: TransactionDraft, sourceText: String): Long {
        val tx = draft.toTransaction(sourceText)
        return dao.insert(tx.toEntity())
    }

    suspend fun addAll(drafts: List<TransactionDraft>, sourceText: String): List<Long> {
        val entities = drafts
            .filter { it.isComplete }
            .map { it.toTransaction(sourceText).toEntity() }
        return if (entities.isEmpty()) emptyList() else dao.insertAll(entities)
    }

    suspend fun update(tx: Transaction) {
        dao.update(tx.toEntity())
    }

    suspend fun delete(tx: Transaction) {
        dao.delete(tx.toEntity())
    }

    suspend fun summary(start: LocalDate, end: LocalDate): MonthSummary {
        val s = start.toEpochDay()
        val e = end.toEpochDay()
        val income = dao.sumByType(TransactionType.INCOME, s, e) +
            dao.sumByType(TransactionType.REFUND, s, e)
        val expense = dao.sumByType(TransactionType.EXPENSE, s, e)
        return MonthSummary(incomeCents = income, expenseCents = expense)
    }

    private fun TransactionDraft.toTransaction(sourceText: String): Transaction = Transaction(
        type = type,
        amountCents = amountCents ?: 0L,
        category = Categories.validFor(type, category),
        description = description.ifBlank { sourceText },
        date = date ?: LocalDate.now(),
        sourceText = sourceText,
        reimbursable = reimbursable,
        tripName = tripName?.takeIf { it.isNotBlank() },
    )
}

data class MonthSummary(
    val incomeCents: Long,
    val expenseCents: Long,
) {
    val balanceCents: Long get() = incomeCents - expenseCents
}
