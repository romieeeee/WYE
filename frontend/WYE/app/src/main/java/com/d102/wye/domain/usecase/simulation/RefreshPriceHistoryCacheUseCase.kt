package com.d102.wye.domain.usecase.simulation

import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.repository.SimulationRepository
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class RefreshPriceHistoryCacheUseCase @Inject constructor(
    private val simulationRepository: SimulationRepository,
    private val cachePolicy: PriceHistoryCachePolicy
) {
    private val refreshMutex = Mutex()
    private var lastCleanupAtEpochMillis: Long? = null

    suspend operator fun invoke(
        tickers: List<String>,
        now: ZonedDateTime = ZonedDateTime.now(MARKET_ZONE)
    ): BaseResult<Unit> = refreshMutex.withLock {
        val distinctTickers = tickers.distinct()
        val nowEpochMillis = now.toInstant().toEpochMilli()

        simulationRepository.markPriceHistoryCacheAccessed(
            tickers = distinctTickers,
            accessedAtEpochMillis = nowEpochMillis
        )
        cleanUnusedCacheIfNeeded(now, nowEpochMillis)

        distinctTickers.forEach { ticker ->
            val refreshPlan = cachePolicy.createRefreshPlan(
                lastCachedDate = simulationRepository.getLastCachedDate(ticker),
                lastSuccessfulSyncEpochMillis =
                    simulationRepository.getLastSuccessfulPriceHistorySync(ticker),
                now = now
            ) ?: return@forEach

            when (val result = simulationRepository.getEtfPriceHistories(
                tickers = listOf(ticker),
                startDate = refreshPlan.startDate,
                endDate = refreshPlan.endDate
            )) {
                is BaseResult.Error -> return@withLock result
                is BaseResult.Success -> {
                    val history = result.data[ticker]
                        ?: return@withLock BaseResult.Error(
                            ApiError.unknownError("$ticker 가격 이력 조회에 실패했습니다")
                        )
                    simulationRepository.savePriceHistories(mapOf(ticker to history))
                    simulationRepository.markPriceHistorySyncSuccessful(
                        ticker = ticker,
                        syncedAtEpochMillis = nowEpochMillis
                    )
                }
            }
        }

        BaseResult.Success(Unit)
    }

    private suspend fun cleanUnusedCacheIfNeeded(
        now: ZonedDateTime,
        nowEpochMillis: Long
    ) {
        val lastCleanup = lastCleanupAtEpochMillis
        if (lastCleanup != null && nowEpochMillis - lastCleanup < CLEANUP_INTERVAL_MILLIS) return

        simulationRepository.deleteUnusedPriceHistoryCache(
            cutoffEpochMillis = cachePolicy.unusedCacheCutoffEpochMillis(now)
        )
        lastCleanupAtEpochMillis = nowEpochMillis
    }

    companion object {
        private val MARKET_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
        private const val CLEANUP_INTERVAL_MILLIS = 24L * 60 * 60 * 1_000
    }
}
