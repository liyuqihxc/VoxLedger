package com.liyuqihxc.voxledger.logic

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Resolves relative date expressions against a base date, e.g. "昨天", "前天",
 * "上周五", "15号", "11月18日", or an ISO "2026-11-18".
 *
 * Pure JVM code (java.time) so it can be unit-tested directly.
 */
object RelativeDateParser {

    private val weekdayMap = mapOf(
        '一' to 1, '二' to 2, '三' to 3, '四' to 4,
        '五' to 5, '六' to 6, '日' to 7, '天' to 7,
    )

    fun parse(text: String, base: LocalDate): LocalDate? {
        val t = text.trim()
        if (t.isEmpty()) return null

        // ISO date.
        try {
            return LocalDate.parse(t)
        } catch (_: DateTimeParseException) {
            // fall through
        }

        when (t) {
            "今天", "当天", "今日" -> return base
            "昨天", "昨日" -> return base.minusDays(1)
            "前天", "前日" -> return base.minusDays(2)
            "大前天" -> return base.minusDays(3)
            "明天", "明日" -> return base.plusDays(1)
            "后天", "后日" -> return base.plusDays(2)
            "大后天" -> return base.plusDays(3)
        }

        // "上周三" / "本周三" / "这周三" / "下周天" and "星期X" variants.
        val relWeek = Regex("(上|本|这|下)周([一二三四五六日天])").find(t)
            ?: Regex("(上|本|这|下)星期([一二三四五六日天])").find(t)
            ?: Regex("星期([一二三四五六日天])").find(t)
        if (relWeek != null) {
            val prefix = relWeek.groupValues.getOrElse(1) { "" }
            val offset = when (prefix) {
                "上" -> -1; "下" -> 1; else -> 0
            }
            val dayNum = weekdayMap[relWeek.groupValues.last().first()] ?: return null
            val currentDayNum = base.dayOfWeek.value // Monday = 1 .. Sunday = 7
            val diff = (dayNum - currentDayNum) + offset * 7
            return base.plusDays(diff.toLong())
        }

        // "11月18日" / "11月18号".
        val md = Regex("(\\d{1,2})月(\\d{1,2})[日号]").find(t)
        if (md != null) {
            val month = md.groupValues[1].toInt()
            val day = md.groupValues[2].toInt()
            return try {
                LocalDate.of(base.year, month, day)
            } catch (_: Exception) {
                null
            }
        }

        // "18号" / "18日" (current month).
        val d = Regex("(\\d{1,2})[日号]").find(t)
        if (d != null) {
            val day = d.groupValues[1].toInt()
            return try {
                base.withDayOfMonth(day)
            } catch (_: Exception) {
                null
            }
        }

        return null
    }
}
