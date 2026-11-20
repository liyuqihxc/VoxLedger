package com.liyuqihxc.voxledger.model

/**
 * The kind of a transaction. The sign of the amount is derived from [sign].
 */
enum class TransactionType(val label: String) {
    EXPENSE("支出"),
    INCOME("收入"),
    REFUND("退款"),
    TRANSFER("转账");

    /** How this type affects the running balance. */
    fun sign(): Int = when (this) {
        EXPENSE -> -1
        INCOME, REFUND -> 1
        TRANSFER -> 0
    }

    companion object {
        fun fromLabel(label: String): TransactionType? = entries.firstOrNull {
            it.label == label || it.name.equals(label, ignoreCase = true)
        }
    }
}
