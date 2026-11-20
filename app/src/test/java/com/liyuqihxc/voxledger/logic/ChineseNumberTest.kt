package com.liyuqihxc.voxledger.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChineseNumberTest {

    @Test
    fun `parses single digits`() {
        assertEquals(0L, ChineseNumber.parse("零"))
        assertEquals(1L, ChineseNumber.parse("一"))
        assertEquals(2L, ChineseNumber.parse("两"))
        assertEquals(9L, ChineseNumber.parse("九"))
    }

    @Test
    fun `parses tens`() {
        assertEquals(10L, ChineseNumber.parse("十"))
        assertEquals(15L, ChineseNumber.parse("十五"))
        assertEquals(35L, ChineseNumber.parse("三十五"))
        assertEquals(99L, ChineseNumber.parse("九十九"))
    }

    @Test
    fun `parses hundreds with implicit trailing unit`() {
        assertEquals(100L, ChineseNumber.parse("一百"))
        assertEquals(350L, ChineseNumber.parse("三百五"))
        assertEquals(250L, ChineseNumber.parse("二百五"))
        assertEquals(108L, ChineseNumber.parse("一百零八"))
        assertEquals(105L, ChineseNumber.parse("一百零五"))
    }

    @Test
    fun `parses thousands and beyond`() {
        assertEquals(1000L, ChineseNumber.parse("一千"))
        assertEquals(1030L, ChineseNumber.parse("一千零三十"))
        assertEquals(12345L, ChineseNumber.parse("一万二千三百四十五"))
        assertEquals(200000000L, ChineseNumber.parse("二亿"))
    }

    @Test
    fun `parses arabic digits`() {
        assertEquals(86L, ChineseNumber.parse("86"))
        assertEquals(0L, ChineseNumber.parse("0"))
    }

    @Test
    fun `rejects non numeric input`() {
        assertNull(ChineseNumber.parse(""))
        assertNull(ChineseNumber.parse("abc"))
        assertNull(ChineseNumber.parse("三五块"))
    }
}
