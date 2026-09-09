package com.d102.wye.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface EtfPriceCacheSyncDao {

    @Query(
        "SELECT lastSuccessfulSyncEpochMillis FROM etf_price_cache_sync " +
            "WHERE ticker = :ticker"
    )
    suspend fun getLastSuccessfulSyncEpochMillis(ticker: String): Long?

    @Query(
        """
        INSERT INTO etf_price_cache_sync (
            ticker,
            lastSuccessfulSyncEpochMillis,
            lastAccessedAtEpochMillis
        ) VALUES (
            :ticker,
            NULL,
            :accessedAtEpochMillis
        )
        ON CONFLICT(ticker) DO UPDATE SET
            lastAccessedAtEpochMillis = excluded.lastAccessedAtEpochMillis
        """
    )
    suspend fun touch(ticker: String, accessedAtEpochMillis: Long)

    @Query(
        """
        INSERT INTO etf_price_cache_sync (
            ticker,
            lastSuccessfulSyncEpochMillis,
            lastAccessedAtEpochMillis
        ) VALUES (
            :ticker,
            :syncedAtEpochMillis,
            :syncedAtEpochMillis
        )
        ON CONFLICT(ticker) DO UPDATE SET
            lastSuccessfulSyncEpochMillis = excluded.lastSuccessfulSyncEpochMillis,
            lastAccessedAtEpochMillis = excluded.lastAccessedAtEpochMillis
        """
    )
    suspend fun markSyncSuccessful(ticker: String, syncedAtEpochMillis: Long)

    @Query("DELETE FROM etf_price_cache_sync WHERE ticker = :ticker")
    suspend fun deleteByTicker(ticker: String)

    @Query(
        """
        DELETE FROM etf_price_history
        WHERE ticker IN (
            SELECT ticker FROM etf_price_cache_sync
            WHERE lastAccessedAtEpochMillis < :cutoffEpochMillis
        )
        """
    )
    suspend fun deleteUnusedPriceHistories(cutoffEpochMillis: Long)

    @Query(
        "DELETE FROM etf_price_cache_sync " +
            "WHERE lastAccessedAtEpochMillis < :cutoffEpochMillis"
    )
    suspend fun deleteUnusedMetadata(cutoffEpochMillis: Long)

    @Transaction
    suspend fun deleteUnusedBefore(cutoffEpochMillis: Long) {
        deleteUnusedPriceHistories(cutoffEpochMillis)
        deleteUnusedMetadata(cutoffEpochMillis)
    }
}
