package com.liyuqihxc.voxledger.data.remote

import com.liyuqihxc.voxledger.model.ParseContext
import com.liyuqihxc.voxledger.model.ParseResult

/**
 * Abstraction over the "turn natural language into transaction drafts" step.
 * Swapping DeepSeek for another provider or a local model only touches
 * implementations of this interface.
 */
interface TransactionParser {
    suspend fun parse(text: String, context: ParseContext): ParseResult
}
