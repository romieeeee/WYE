package com.d102.wye.domain.usecase.simulation

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

data class PriceHistoryRefreshPlan(
    val startDate: String,
    val endDate: String
)

/**
 * 가격 이력 캐시의 갱신 범위를 결정한다.
 *
 * 거래소 휴장일 전체를 앱에서 예측하지는 않는다. 대신 마지막 성공 동기화를 일정 시간
 * 신뢰해 휴일이나 서버 반영 지연 시 같은 빈 구간을 연속 호출하지 않는다.
 */
class PriceHistoryCachePolicy @Inject constructor() {

    fun unusedCacheCutoffEpochMillis(
        now: ZonedDateTime = ZonedDateTime.now(MARKET_ZONE)
    ): Long = now.toInstant().minus(UNUSED_CACHE_RETENTION).toEpochMilli()

    fun createRefreshPlan(
        lastCachedDate: String?,
        lastSuccessfulSyncEpochMillis: Long?,
        now: ZonedDateTime = ZonedDateTime.now(MARKET_ZONE)
    ): PriceHistoryRefreshPlan? {
        val marketNow = now.withZoneSameInstant(MARKET_ZONE)
        val today = marketNow.toLocalDate()
        val cachedDate = lastCachedDate?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }

        if (cachedDate == null) {
            return PriceHistoryRefreshPlan(
                startDate = today.minusYears(INITIAL_HISTORY_YEARS).toString(),
                endDate = today.toString()
            )
        }

        if (cachedDate >= expectedLatestTradingDate(marketNow)) return null

        val recentlySynced = lastSuccessfulSyncEpochMillis?.let { epochMillis ->
            val elapsed = Duration.between(
                Instant.ofEpochMilli(epochMillis),
                marketNow.toInstant()
            )
            elapsed.isNegative || elapsed < MIN_REFRESH_INTERVAL
        } ?: false

        if (recentlySynced) return null

        return PriceHistoryRefreshPlan(
            startDate = cachedDate.plusDays(1).toString(),
            endDate = today.toString()
        )
    }

    private fun expectedLatestTradingDate(now: ZonedDateTime): LocalDate {
        var expectedDate = if (now.toLocalTime() >= MARKET_DATA_READY_TIME) {
            now.toLocalDate()
        } else {
            now.toLocalDate().minusDays(1)
        }

        while (expectedDate.dayOfWeek == DayOfWeek.SATURDAY ||
            expectedDate.dayOfWeek == DayOfWeek.SUNDAY
        ) {
            expectedDate = expectedDate.minusDays(1)
        }
        return expectedDate
    }

    companion object {
        private val MARKET_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
        private val MARKET_DATA_READY_TIME: LocalTime = LocalTime.of(18, 0)
        private val MIN_REFRESH_INTERVAL: Duration = Duration.ofHours(6)
        private val UNUSED_CACHE_RETENTION: Duration = Duration.ofDays(30)
        private const val INITIAL_HISTORY_YEARS = 3L
    }
}
