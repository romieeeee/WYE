package com.d102.wye.domain.usecase.portfolio

import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.EtfPricePoint
import com.d102.wye.domain.model.PortfolioCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatePortfolioChartUseCaseTest {

    @Test
    fun chart_calculatesRecentAndPastReturnsFromBaseDates() {
        val result = CalculatePortfolioChartUseCase().invoke(
            counts = listOf(PortfolioCount(ticker = "AAA", counts = 2.0, etfName = "Alpha ETF")),
            priceHistories = mapOf(
                "AAA" to priceHistory(
                    ticker = "AAA",
                    dates = listOf("2024-07-17", "2025-07-17", "2026-07-17"),
                    prices = listOf(50L, 100L, 150L)
                )
            ),
            createdAt = "2025-07-17"
        )

        assertEquals(2, result.recentPoints.size)
        assertEquals(0.0, result.recentPoints.first().value, 0.0001)
        assertEquals(50.0, result.recentReturn, 0.0001)

        assertEquals(2, result.pastPoints.size)
        assertEquals(100.0, result.pastReturn, 0.0001)
        assertEquals(300L, result.estimatedFinalValue)
    }

    @Test
    fun chart_returnsEmptyResultWhenAllCountsAreZero() {
        val result = CalculatePortfolioChartUseCase().invoke(
            counts = listOf(PortfolioCount(ticker = "AAA", counts = 0.0, etfName = "Alpha ETF")),
            priceHistories = mapOf(
                "AAA" to priceHistory("AAA", listOf("2025-07-17"), listOf(100L))
            ),
            createdAt = "2025-07-17"
        )

        assertTrue(result.recentPoints.isEmpty())
        assertTrue(result.pastPoints.isEmpty())
        assertEquals(0.0, result.recentReturn, 0.0001)
        assertEquals(0.0, result.pastReturn, 0.0001)
        assertEquals(0L, result.estimatedFinalValue)
    }

    private fun priceHistory(
        ticker: String,
        dates: List<String>,
        prices: List<Long>,
    ) = EtfPriceHistory(
        ticker = ticker,
        content = dates.zip(prices).map { (date, price) ->
            EtfPricePoint(date = date, stockPrice = price, dailyReturn = 0.0)
        },
        totalElements = dates.size,
        totalPages = 1,
        last = true
    )
}
