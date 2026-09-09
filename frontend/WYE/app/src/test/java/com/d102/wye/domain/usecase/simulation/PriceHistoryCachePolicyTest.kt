package com.d102.wye.domain.usecase.simulation

import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PriceHistoryCachePolicyTest {

    private val policy = PriceHistoryCachePolicy()

    @Test
    fun unusedCacheCutoff_isThirtyDaysBeforeNow() {
        val now = kst("2026-09-09T19:00:00+09:00")

        val cutoff = policy.unusedCacheCutoffEpochMillis(now)

        assertEquals(
            kst("2026-08-10T19:00:00+09:00").toInstant().toEpochMilli(),
            cutoff
        )
    }

    @Test
    fun cacheMiss_fetchesRecentThreeYears() {
        val plan = policy.createRefreshPlan(
            lastCachedDate = null,
            lastSuccessfulSyncEpochMillis = null,
            now = kst("2026-09-09T19:00:00+09:00")
        )

        assertEquals(
            PriceHistoryRefreshPlan("2023-09-09", "2026-09-09"),
            plan
        )
    }

    @Test
    fun staleCache_fetchesFromDayAfterLastCachedDate() {
        val plan = policy.createRefreshPlan(
            lastCachedDate = "2026-09-07",
            lastSuccessfulSyncEpochMillis =
                kst("2026-09-08T10:00:00+09:00").toInstant().toEpochMilli(),
            now = kst("2026-09-09T19:00:00+09:00")
        )

        assertEquals(
            PriceHistoryRefreshPlan("2026-09-08", "2026-09-09"),
            plan
        )
    }

    @Test
    fun recentSuccessfulSync_suppressesRepeatedHolidayRequest() {
        val plan = policy.createRefreshPlan(
            lastCachedDate = "2026-09-07",
            lastSuccessfulSyncEpochMillis =
                kst("2026-09-09T16:00:00+09:00").toInstant().toEpochMilli(),
            now = kst("2026-09-09T19:00:00+09:00")
        )

        assertNull(plan)
    }

    @Test
    fun fridayCache_isFreshDuringWeekend() {
        val plan = policy.createRefreshPlan(
            lastCachedDate = "2026-09-11",
            lastSuccessfulSyncEpochMillis = null,
            now = kst("2026-09-13T20:00:00+09:00")
        )

        assertNull(plan)
    }

    @Test
    fun beforeMarketDataReady_usesPreviousWeekdayAsLatestExpectedDate() {
        val plan = policy.createRefreshPlan(
            lastCachedDate = "2026-09-11",
            lastSuccessfulSyncEpochMillis = null,
            now = kst("2026-09-14T10:00:00+09:00")
        )

        assertNull(plan)
    }

    @Test
    fun afterMarketDataReady_requiresTodaysData() {
        val plan = policy.createRefreshPlan(
            lastCachedDate = "2026-09-08",
            lastSuccessfulSyncEpochMillis = null,
            now = kst("2026-09-09T19:00:00+09:00")
        )

        assertEquals(
            PriceHistoryRefreshPlan("2026-09-09", "2026-09-09"),
            plan
        )
    }

    private fun kst(value: String): ZonedDateTime = ZonedDateTime.parse(value)
}
