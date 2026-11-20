package com.liyuqihxc.voxledger.model

import java.time.LocalDate

/**
 * An editable, not-yet-saved transaction produced by the parser and confirmed
 * by the user before being persisted.
 */
data class TransactionDraft(
    val type: TransactionType,
    val amountCents: Long?,
    val category: String?,
    val description: String,
    val date: LocalDate?,
    val reimbursable: Boolean = false,
    val tripName: String? = null,
) {
    val isComplete: Boolean
        get() = amountCents != null && amountCents > 0 && !description.isBlank()
}
