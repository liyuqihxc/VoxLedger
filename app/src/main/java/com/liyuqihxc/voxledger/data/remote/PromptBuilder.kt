package com.liyuqihxc.voxledger.data.remote

import com.liyuqihxc.voxledger.model.Categories
import com.liyuqihxc.voxledger.model.ParseContext
import java.time.format.DateTimeFormatter

/**
 * Builds the system prompt that constrains the LLM to a strict JSON schema and
 * to the app's fixed category list.
 */
object PromptBuilder {

    fun buildSystem(context: ParseContext): String {
        val today = context.today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return """
            你是一个记账助手。把用户的一段自然语言描述，解析成一条或多条交易记录。
            今天是 $today。金额单位是 ${context.currency}。

            只能从以下分类中选择，输出分类的 id（括号内）：
            ${Categories.promptHint}

            交易类型 type 只能是：expense（支出）、income（收入）、refund（退款）、transfer（转账）。

            规则：
            1. amount 必须是十进制字符串，例如 "128.00"、"26.50"。无法确定金额时输出 null，绝不能编造。
            2. date 使用 ISO 格式 YYYY-MM-DD；如果用户说"昨天/前天/上周五"等，换算成今天的相对日期；没有明确日期时输出 null（表示今天）。
            3. 描述与出差相关（如"出差""报销""差旅"）时，reimbursable 为 true，并在 tripName 中给出差旅名称（可为 null）。
            4. 区分支出、收入、退款和转账。例如"朋友转给我两百"是收入，"我转给朋友两百"是支出或转账。
            5. 一段话可能包含多笔消费，拆分输出。

            只输出如下 JSON，不要输出任何其他文字：
            {
              "transactions": [
                {
                  "type": "expense",
                  "amount": "128.00",
                  "category": "food",
                  "description": "和朋友吃晚饭",
                  "date": "$today",
                  "reimbursable": false,
                  "tripName": null
                }
              ]
            }
        """.trimIndent()
    }
}
