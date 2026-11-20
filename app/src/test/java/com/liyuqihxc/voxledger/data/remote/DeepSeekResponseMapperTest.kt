package com.liyuqihxc.voxledger.data.remote

import com.liyuqihxc.voxledger.model.ParseContext
import com.liyuqihxc.voxledger.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DeepSeekResponseMapperTest {

    private val context = ParseContext(today = LocalDate.of(2026, 11, 20))

    @Test
    fun `maps a valid response with multiple transactions`() {
        val json = """
            {"transactions":[
              {"type":"expense","amount":"128.00","category":"food","description":"和朋友吃晚饭","date":"2026-11-18","reimbursable":false,"tripName":null},
              {"type":"expense","amount":"26.50","category":"transport","description":"打车","date":"2026-11-18","reimbursable":false}
            ]}
        """.trimIndent()

        val result = DeepSeekResponseMapper.map(json, context)

        assertEquals(2, result.drafts.size)
        assertEquals(12800L, result.drafts[0].amountCents)
        assertEquals(TransactionType.EXPENSE, result.drafts[0].type)
        assertEquals("food", result.drafts[0].category)
        assertEquals(LocalDate.of(2026, 11, 18), result.drafts[0].date)
        assertEquals(2650L, result.drafts[1].amountCents)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun `accepts numeric amounts`() {
        val json = """{"transactions":[{"type":"expense","amount":35.5,"category":"food"}]}"""
        val result = DeepSeekResponseMapper.map(json, context)
        assertEquals(3550L, result.drafts[0].amountCents)
    }

    @Test
    fun `resolves relative dates`() {
        val json = """{"transactions":[{"type":"expense","amount":"10","date":"昨天","category":"food"}]}"""
        val result = DeepSeekResponseMapper.map(json, context)
        assertEquals(LocalDate.of(2026, 11, 19), result.drafts[0].date)
    }

    @Test
    fun `flags missing amount but keeps draft`() {
        val json = """{"transactions":[{"type":"expense","amount":null,"category":"food","description":"吃饭"}]}"""
        val result = DeepSeekResponseMapper.map(json, context)
        assertEquals(1, result.drafts.size)
        assertNull(result.drafts[0].amountCents)
        assertTrue(result.warnings.isNotEmpty())
    }

    @Test
    fun `warns on unknown category`() {
        val json = """{"transactions":[{"type":"expense","amount":"10","category":"spaceship"}]}"""
        val result = DeepSeekResponseMapper.map(json, context)
        assertNull(result.drafts[0].category)
        assertTrue(result.warnings.any { it.contains("spaceship") })
    }

    @Test
    fun `returns empty and a warning for invalid json`() {
        val result = DeepSeekResponseMapper.map("not json at all", context)
        assertTrue(result.drafts.isEmpty())
        assertTrue(result.warnings.isNotEmpty())
    }

    @Test
    fun `maps income and refund types`() {
        val json = """
            {"transactions":[
              {"type":"income","amount":"200","category":"salary","description":"工资"},
              {"type":"refund","amount":"30","category":"other_income","description":"退款"}
            ]}
        """.trimIndent()
        val result = DeepSeekResponseMapper.map(json, context)
        assertEquals(TransactionType.INCOME, result.drafts[0].type)
        assertEquals(TransactionType.REFUND, result.drafts[1].type)
    }
}
