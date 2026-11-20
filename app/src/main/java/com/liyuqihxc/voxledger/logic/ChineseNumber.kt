package com.liyuqihxc.voxledger.logic

/**
 * Parses Chinese numerals into a [Long], e.g. "三十五" -> 35, "三百五" -> 350,
 * "一百零八" -> 108, "一万二千三百四十五" -> 12345.
 *
 * Pure JVM code (no Android dependencies) so it can be unit-tested directly.
 */
object ChineseNumber {
    private val digits = mapOf(
        '零' to 0L, '〇' to 0L, '一' to 1L, '二' to 2L, '两' to 2L,
        '三' to 3L, '四' to 4L, '五' to 5L, '六' to 6L, '七' to 7L,
        '八' to 8L, '九' to 9L,
    )
    private val units = mapOf('十' to 10L, '百' to 100L, '千' to 1000L)
    private val sections = mapOf('万' to 10000L, '亿' to 100000000L)

    fun isDigitChar(c: Char): Boolean = c in digits

    fun parse(input: String): Long? {
        val s = input.trim()
        if (s.isEmpty()) return null
        if (s.all { it.isDigit() }) return s.toLongOrNull()

        var total = 0L      // value accumulated across 万/亿 sections
        var section = 0L    // value within the current section
        var num = 0L        // pending digit before a unit
        var lastUnit = 1L   // last unit seen; used for "三百五" -> 350

        for (c in s) {
            when {
                c in digits -> {
                    val d = digits.getValue(c)
                    if (d == 0L) {
                        // 零 marks the following digit as a ones-place digit,
                        // e.g. 一百零八 = 108 (not 180).
                        lastUnit = 1L
                    }
                    num = d
                }
                c in units -> {
                    if (num == 0L) num = 1L // "十五" -> 15, "百" -> 100
                    section += num * units.getValue(c)
                    num = 0L
                    lastUnit = units.getValue(c)
                }
                c in sections -> {
                    section = (section + num) * sections.getValue(c)
                    total += section
                    section = 0L
                    num = 0L
                    lastUnit = 1L
                }
                c.isWhitespace() -> Unit
                else -> return null
            }
        }

        // Trailing digit: "三十五" -> 3*10+5, "三百五" -> 300 + 5*10 (lastUnit/10).
        if (num != 0L) {
            section += if (lastUnit >= 10) num * (lastUnit / 10) else num
        }
        return total + section
    }
}
