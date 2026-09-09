package com.d102.wye.domain.usecase.simulation

import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.AiReviewResult
import com.d102.wye.domain.model.EtfBundle
import com.d102.wye.domain.model.EtfBundleDetail
import com.d102.wye.domain.model.EtfDividendHistory
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.Portfolio
import com.d102.wye.domain.repository.SimulationRepository
import com.d102.wye.domain.state.InvestmentType
import java.time.ZonedDateTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshPriceHistoryCacheUseCaseTest {

    @Test
    fun emptyInitialHistory_returnsErrorWithoutMarkingSuccessfulSync() = runBlocking {
        val repository = FakeSimulationRepository(lastCachedDate = null)
        val now = ZonedDateTime.parse("2026-09-09T19:00:00+09:00")
        val useCase = RefreshPriceHistoryCacheUseCase(repository, PriceHistoryCachePolicy())

        val result = useCase(listOf(TICKER), now)

        assertTrue(result is BaseResult.Error)
        assertNull(repository.savedHistories)
        assertNull(repository.successfulSyncEpochMillis)
        assertEquals(listOf(TICKER), repository.accessedTickers)
        assertEquals(
            ZonedDateTime.parse("2026-08-10T19:00:00+09:00").toInstant().toEpochMilli(),
            repository.cleanupCutoffEpochMillis
        )
    }

    @Test
    fun emptyIncrementalHistory_keepsExistingCacheAndMarksSuccessfulSync() = runBlocking {
        val repository = FakeSimulationRepository(lastCachedDate = "2026-09-08")
        val now = ZonedDateTime.parse("2026-09-09T19:00:00+09:00")
        val useCase = RefreshPriceHistoryCacheUseCase(repository, PriceHistoryCachePolicy())

        val result = useCase(listOf(TICKER), now)

        assertTrue(result is BaseResult.Success)
        assertEquals(TICKER, repository.savedHistories?.keys?.single())
        assertEquals(now.toInstant().toEpochMilli(), repository.successfulSyncEpochMillis)
    }

    private class FakeSimulationRepository(
        private val lastCachedDate: String?
    ) : SimulationRepository {
        var savedHistories: Map<String, EtfPriceHistory>? = null
        var successfulSyncEpochMillis: Long? = null
        var accessedTickers: List<String> = emptyList()
        var cleanupCutoffEpochMillis: Long? = null

        override suspend fun getEtfPriceHistories(
            tickers: List<String>,
            startDate: String?,
            endDate: String?,
            page: Int
        ): BaseResult<Map<String, EtfPriceHistory>> = BaseResult.Success(
            mapOf(
                TICKER to EtfPriceHistory(
                    ticker = TICKER,
                    content = emptyList(),
                    totalElements = 0,
                    totalPages = 0,
                    last = true
                )
            )
        )

        override suspend fun savePriceHistories(histories: Map<String, EtfPriceHistory>) {
            savedHistories = histories
        }

        override suspend fun getCachedPriceHistories(
            tickers: List<String>
        ): Map<String, EtfPriceHistory> = emptyMap()

        override suspend fun deleteCachedPriceHistory(ticker: String) = Unit

        override suspend fun getLastCachedDate(ticker: String): String? = lastCachedDate

        override suspend fun getLastSuccessfulPriceHistorySync(ticker: String): Long? = null

        override suspend fun markPriceHistorySyncSuccessful(
            ticker: String,
            syncedAtEpochMillis: Long
        ) {
            successfulSyncEpochMillis = syncedAtEpochMillis
        }

        override suspend fun markPriceHistoryCacheAccessed(
            tickers: List<String>,
            accessedAtEpochMillis: Long
        ) {
            accessedTickers = tickers
        }

        override suspend fun deleteUnusedPriceHistoryCache(cutoffEpochMillis: Long) {
            cleanupCutoffEpochMillis = cutoffEpochMillis
        }

        override suspend fun getEtfDividendHistories(
            tickers: List<String>,
            startDate: String?,
            endDate: String?
        ): BaseResult<Map<String, EtfDividendHistory>> = unused()

        override suspend fun getAiPortfolioReview(
            totalAmount: Long,
            investmentType: InvestmentType,
            portfolios: List<Portfolio>
        ): BaseResult<AiReviewResult> = unused()

        override suspend fun getPresetList(): BaseResult<List<EtfBundle>> = unused()

        override suspend fun getPresetDetail(presetId: Int): BaseResult<EtfBundleDetail> = unused()

        private fun unused() = BaseResult.Error(ApiError.unknownError("테스트에서 사용하지 않음"))
    }

    companion object {
        private const val TICKER = "069500"
    }
}
