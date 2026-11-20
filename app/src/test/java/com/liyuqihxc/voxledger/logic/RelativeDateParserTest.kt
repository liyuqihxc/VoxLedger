package com.liyuqihxc.voxledger.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RelativeDateParserTest {

    private val base = LocalDate.of(2026, 11, 20) // a Friday

    @Test
    fun `parses absolute and relative day names`() {
        assertEquals(base, RelativeDateParser.parse("今天", base))
        assertEquals(base.minusDays(1), RelativeDateParser.parse("昨天", base))
        assertEquals(base.minusDays(2), RelativeDateParser.parse("前天", base))
        assertEquals(base.minusDays(3), RelativeDateParser.parse("大前天", base))
        assertEquals(base.plusDays(1), RelativeDateParser.parse("明天", base))
        assertEquals(base.plusDays(2), RelativeDateParser.parse("后天", base))
    }

    @Test
    fun `parses week offsets`() {
        // base is Friday (dayOfWeek = 5). 上周五 = -7 days, 下周一 = +3 days.
        assertEquals(base.minusDays(7), RelativeDateParser.parse("上周五", base))
        assertEquals(base.plusDays(3), RelativeDateParser.parse("下周一", base))
        assertEquals(base, RelativeDateParser.parse("这周五", base))
    }

    @Test
    fun `parses month day`() {
        assertEquals(LocalDate.of(2026, 11, 18), RelativeDateParser.parse("11月18日", base))
        assertEquals(LocalDate.of(2026, 11, 18), RelativeDateParser.parse("11月18号", base))
        assertEquals(base.withDayOfMonth(15), RelativeDateParser.parse("15号", base))
    }

    @Test
    fun `parses iso date`() {
        assertEquals(LocalDate.of(2026, 11, 18), RelativeDateParser.parse("2026-11-18", base))
    }

    @Test
    fun `returns null for unknown`() {
        assertNull(RelativeDateParser.parse("", base))
        assertNull(RelativeDateParser.parse("随便", base))
        assertNull(RelativeDateParser.parse("上月", base))
    }
}
