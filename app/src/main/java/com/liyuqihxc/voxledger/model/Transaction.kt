package com.liyuqihxc.voxledger.model

import java.time.Instant
import java.time.LocalDate

/**
 * A persisted transaction. [amountCents] is always a non-negative magnitude;
 * the direction comes from [type].
 */
data class Transaction(
    val id: Long = 0L,
    val type: TransactionType,
    val amountCents: Long,
    val category: String,
    val description: String,
    val date: LocalDate,
    val sourceText: String,
    val reimbursable: Boolean = false,
    val tripName: String? = null,
    val createdAt: Instant = Instant.now(),
) {
    /** Signed amount in cents. */
    val signedCents: Long get() = amountCents * type.sign()
}
