package com.liyuqihxc.voxledger.model

import java.time.LocalDate

/** Context passed to the parser so it can resolve relative dates and pick categories. */
data class ParseContext(
    val today: LocalDate,
    val currency: String = "CNY",
)

/** The result of parsing one utterance. */
data class ParseResult(
    val drafts: List<TransactionDraft>,
    val warnings: List<String> = emptyList(),
)
