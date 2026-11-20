package com.liyuqihxc.voxledger.model

/**
 * A user-facing category. Categories are managed by the app; the LLM only
 * picks from this fixed list to keep names consistent.
 */
data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val type: TransactionType,
)

object Categories {
    val EXPENSE = listOf(
        Category("food", "餐饮", "🍜", TransactionType.EXPENSE),
        Category("transport", "交通", "🚗", TransactionType.EXPENSE),
        Category("shopping", "购物", "🛒", TransactionType.EXPENSE),
        Category("housing", "住房", "🏠", TransactionType.EXPENSE),
        Category("medical", "医疗", "💊", TransactionType.EXPENSE),
        Category("entertainment", "娱乐", "🎬", TransactionType.EXPENSE),
        Category("travel", "出行", "✈️", TransactionType.EXPENSE),
        Category("lodging", "住宿", "🏨", TransactionType.EXPENSE),
        Category("communication", "通讯", "📱", TransactionType.EXPENSE),
        Category("education", "教育", "📚", TransactionType.EXPENSE),
        Category("other", "其他", "📦", TransactionType.EXPENSE),
    )

    val INCOME = listOf(
        Category("salary", "工资", "💰", TransactionType.INCOME),
        Category("bonus", "奖金", "🧧", TransactionType.INCOME),
        Category("investment", "理财", "📈", TransactionType.INCOME),
        Category("other_income", "其他收入", "💵", TransactionType.INCOME),
    )

    val ALL: List<Category> = EXPENSE + INCOME

    fun byId(id: String): Category? = ALL.firstOrNull { it.id == id }

    /** Human-readable list for the LLM prompt. */
    val promptHint: String = buildString {
        append("支出分类: ")
        append(EXPENSE.joinToString("、") { "${it.name}(${it.id})" })
        append("\n收入分类: ")
        append(INCOME.joinToString("、") { "${it.name}(${it.id})" })
    }

    /** A valid category id for a given type, falling back to a sensible default. */
    fun validFor(type: TransactionType, candidate: String?): String {
        val list = if (type == TransactionType.EXPENSE) EXPENSE else INCOME
        val match = list.firstOrNull { it.id == candidate }
        return match?.id ?: list.first().id
    }
}
