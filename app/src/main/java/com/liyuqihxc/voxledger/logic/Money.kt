package com.liyuqihxc.voxledger.logic

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Parses a number token that may be Arabic ("86") or Chinese ("八十六").
 */
object NumberParser {
    fun parse(input: String): Long? {
        val s = input.trim()
        if (s.isEmpty()) return null
        if (s.all { it.isDigit() }) return s.toLongOrNull()
        return ChineseNumber.parse(s)
    }
}

/**
 * Money parsing/formatting helpers. Amounts are stored as integer cents to
 * avoid floating point error.
 */
object Money {

    /** A digit char acceptable as a "bare" Chinese/Arabic amount fragment. */
    private fun isNumberFragmentChar(c: Char): Boolean =
        c.isDigit() || ChineseNumber.isDigitChar(c) || c.isWhitespace()

    /**
     * Parse an amount string into cents. Accepts:
     *  - "35", "35.5", "35.50", "128.00", "3,500" -> 3500, 3550, 3550, 12800, 350000
     *  - "三十五" -> 3500
     *  - "三块五", "86块5", "一百零八块六毛", "三块五毛五分" -> 350, 8650, 10860, 355
     * Returns null when the string cannot be interpreted as money.
     */
    fun parseToCents(input: String): Long? {
        val s = input.trim()
        if (s.isEmpty()) return null

        val hasChinese = s.any { ChineseNumber.isDigitChar(it) || it in "块元毛角分" }
        val hasUnit = s.any { it in "块元毛角分" }
        if (hasChinese || hasUnit) return parseChineseMoney(s)

        // Arabic decimal.
        val cleaned = s
            .replace(",", "")
            .replace("¥", "")
            .replace("￥", "")
            .replace(" ", "")
            .trim()
        val m = Regex("^(-?\\d+)(?:\\.(\\d{1,2}))?$").find(cleaned) ?: return null
        val whole = m.groupValues[1].toLongOrNull() ?: return null
        val frac = m.groupValues[2]
        return when {
            frac.isEmpty() -> whole * 100
            frac.length == 1 -> whole * 100 + frac.toLong() * 10
            else -> whole * 100 + frac.take(2).toLong()
        }
    }

    /** Parse a Chinese (or mixed) money string like "三块五" / "86块5". */
    private fun parseChineseMoney(input: String): Long? {
        var s = input
            .replace("元", "块")
            .replace("角", "毛")
            .replace("〇", "零")
            .replace("两", "二")
            .trim()
        if (s.isEmpty()) return null

        var yuan = 0L
        var jiao = 0L
        var fen = 0L

        val kuai = s.indexOf('块')
        val rest = if (kuai >= 0) s.substring(kuai + 1) else s

        // Yuan part.
        if (kuai >= 0) {
            val seg = s.substring(0, kuai)
            if (seg.isNotEmpty()) {
                yuan = NumberParser.parse(seg) ?: return null
            }
        }

        val mao = rest.indexOf('毛')
        val fenIdx = rest.indexOf('分')

        when {
            mao >= 0 -> {
                val mSeg = rest.substring(0, mao)
                if (mSeg.isNotEmpty()) jiao = NumberParser.parse(mSeg) ?: return null
                val afterMao = rest.substring(mao + 1)
                val f = afterMao.indexOf('分')
                if (f >= 0) {
                    val fSeg = afterMao.substring(0, f)
                    if (fSeg.isNotEmpty()) fen = NumberParser.parse(fSeg) ?: return null
                } else if (afterMao.isNotEmpty() && afterMao.all(::isNumberFragmentChar)) {
                    fen = NumberParser.parse(afterMao) ?: return null
                } else if (afterMao.isNotEmpty()) {
                    return null
                }
            }
            fenIdx >= 0 -> {
                val fSeg = rest.substring(0, fenIdx)
                if (fSeg.isNotEmpty()) fen = NumberParser.parse(fSeg) ?: return null
            }
            kuai >= 0 && rest.isNotEmpty() && rest.all(::isNumberFragmentChar) -> {
                // "三块五" / "86块5": trailing number without a unit means 毛.
                jiao = NumberParser.parse(rest) ?: return null
            }
            kuai >= 0 && rest.isNotEmpty() -> return null
            kuai < 0 -> {
                // No 块/毛/分 at all: treat the whole thing as yuan ("三十五", "三百五").
                yuan = NumberParser.parse(s) ?: return null
            }
        }

        return yuan * 100 + jiao * 10 + fen
    }

    /** Format cents as a plain decimal string, e.g. 3500 -> "35.00". */
    fun formatCents(cents: Long): String {
        val bd = BigDecimal(cents).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        return bd.toPlainString()
    }

    /** Format cents with a currency symbol, e.g. 3500 -> "¥35.00". */
    fun formatYuan(cents: Long, symbol: String = "¥"): String = "$symbol${formatCents(cents)}"

    /** Format a signed amount for display, e.g. -3500 -> "-35.00". */
    fun formatSigned(cents: Long): String {
        val sign = if (cents < 0) "-" else "+"
        return sign + formatCents(kotlin.math.abs(cents))
    }
}
