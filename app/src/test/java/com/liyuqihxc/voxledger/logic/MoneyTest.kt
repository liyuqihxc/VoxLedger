package com.liyuqihxc.voxledger.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun `parses arabic decimals`() {
        assertEquals(3500L, Money.parseToCents("35"))
        assertEquals(3550L, Money.parseToCents("35.5"))
        assertEquals(3550L, Money.parseToCents("35.50"))
        assertEquals(12800L, Money.parseToCents("128.00"))
        assertEquals(8650L, Money.parseToCents("86.5"))
    }

    @Test
    fun `parses arabic with separators and symbols`() {
        assertEquals(350000L, Money.parseToCents("3,500"))
        assertEquals(3500L, Money.parseToCents("¥35"))
        assertEquals(3500L, Money.parseToCents("￥35"))
    }

    @Test
    fun `parses chinese bare numbers as yuan`() {
        assertEquals(3500L, Money.parseToCents("三十五"))
        assertEquals(35000L, Money.parseToCents("三百五"))
    }

    @Test
    fun `parses chinese money with units`() {
        assertEquals(350L, Money.parseToCents("三块五"))
        assertEquals(8650L, Money.parseToCents("86块5"))
        assertEquals(10860L, Money.parseToCents("一百零八块六毛"))
        assertEquals(355L, Money.parseToCents("三块五毛五分"))
        assertEquals(50L, Money.parseToCents("五毛"))
        assertEquals(5L, Money.parseToCents("五分"))
    }

    @Test
    fun `rejects invalid amounts`() {
        assertNull(Money.parseToCents(""))
        assertNull(Money.parseToCents("abc"))
        assertNull(Money.parseToCents("三十五块钱左右"))
    }

    @Test
    fun `formats cents`() {
        assertEquals("35.00", Money.formatCents(3500))
        assertEquals("0.50", Money.formatCents(50))
        assertEquals("128.00", Money.formatCents(12800))
        assertEquals("¥35.00", Money.formatYuan(3500))
    }

    @Test
    fun `formats signed amounts`() {
        assertEquals("-35.00", Money.formatSigned(-3500))
        assertEquals("+35.00", Money.formatSigned(3500))
    }
}
