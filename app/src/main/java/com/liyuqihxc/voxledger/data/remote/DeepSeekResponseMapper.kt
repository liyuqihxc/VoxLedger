package com.liyuqihxc.voxledger.data.remote

import com.liyuqihxc.voxledger.logic.Money
import com.liyuqihxc.voxledger.logic.RelativeDateParser
import com.liyuqihxc.voxledger.model.Categories
import com.liyuqihxc.voxledger.model.ParseContext
import com.liyuqihxc.voxledger.model.ParseResult
import com.liyuqihxc.voxledger.model.TransactionDraft
import com.liyuqihxc.voxledger.model.TransactionType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Maps the raw JSON returned by the LLM into validated [TransactionDraft]s.
 *
 * Pure JVM code (no Android dependencies), so the mapping can be unit-tested
 * with sample JSON strings. It is deliberately lenient: amounts may arrive as
 * JSON strings or numbers, unknown categories produce a warning, and a missing
 * amount becomes a draft the user must complete.
 */
object DeepSeekResponseMapper {

    private val json = Json { ignoreUnknownKeys = true }

    fun map(rawContent: String, context: ParseContext): ParseResult {
        val element = runCatching { json.parseToJsonElement(rawContent) }.getOrNull()
            ?: return ParseResult(emptyList(), listOf("模型返回的内容不是合法 JSON"))

        val root = element as? JsonObject
            ?: return ParseResult(emptyList(), listOf("模型返回的内容不是 JSON 对象"))

        val transactions = (root["transactions"] as? JsonArray)?.filterIsInstance<JsonObject>()
            ?: return ParseResult(emptyList(), listOf("模型返回内容缺少 transactions 数组"))

        val drafts = mutableListOf<TransactionDraft>()
        val warnings = mutableListOf<String>()

        for (item in transactions) {
            drafts += mapTransaction(item, context, warnings)
        }

        return ParseResult(drafts, warnings.distinct())
    }

    private fun mapTransaction(
        item: JsonObject,
        context: ParseContext,
        warnings: MutableList<String>,
    ): TransactionDraft {
        val type = parseType(readString(item, "type"))

        val amountRaw = readAmount(item)
        val amountCents = amountRaw?.let { Money.parseToCents(it) }
        if (!amountRaw.isNullOrBlank() && amountCents == null) {
            warnings += "无法识别金额: $amountRaw"
        } else if (amountRaw.isNullOrBlank()) {
            warnings += "模型未提供金额，请在确认页补全"
        }

        val category = resolveCategory(type, readString(item, "category"), warnings)

        val dateRaw = readString(item, "date")
        val date = dateRaw?.let { RelativeDateParser.parse(it, context.today) } ?: context.today

        val description = readString(item, "description").orEmpty().trim()
        val reimbursable = readBoolean(item, "reimbursable")
        val tripName = readString(item, "tripName")?.takeIf { it.isNotBlank() }

        return TransactionDraft(
            type = type,
            amountCents = amountCents,
            category = category,
            description = description,
            date = date,
            reimbursable = reimbursable,
            tripName = tripName,
        )
    }

    private fun parseType(raw: String?): TransactionType = when (raw?.trim()?.lowercase()) {
        "income", "收入" -> TransactionType.INCOME
        "refund", "退款" -> TransactionType.REFUND
        "transfer", "转账", "转入", "转出" -> TransactionType.TRANSFER
        else -> TransactionType.EXPENSE
    }

    private fun resolveCategory(
        type: TransactionType,
        raw: String?,
        warnings: MutableList<String>,
    ): String? {
        val list = if (type == TransactionType.EXPENSE) Categories.EXPENSE else Categories.INCOME
        val match = raw?.let { r ->
            list.firstOrNull { it.id == r || it.name == r }
        }
        if (!raw.isNullOrBlank() && match == null) {
            warnings += "未知分类: $raw"
        }
        return match?.id
    }

    private fun readString(item: JsonObject, key: String): String? =
        (item[key] as? JsonPrimitive)?.takeIf { it.isString }?.content

    /** Amounts may be JSON strings ("128.00") or numbers (128.0). */
    private fun readAmount(item: JsonObject): String? {
        val prim = item["amount"] as? JsonPrimitive ?: return null
        if (prim is JsonNull) return null
        return prim.content
    }

    private fun readBoolean(item: JsonObject, key: String): Boolean =
        (item[key] as? JsonPrimitive)?.let {
            runCatching { it.content.toBooleanStrict() }.getOrDefault(false)
        } ?: false
}
