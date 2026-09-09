package com.d102.wye.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS etf_price_cache_sync (
                ticker TEXT NOT NULL PRIMARY KEY,
                lastSuccessfulSyncEpochMillis INTEGER,
                lastAccessedAtEpochMillis INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT OR IGNORE INTO etf_price_cache_sync (
                ticker,
                lastSuccessfulSyncEpochMillis,
                lastAccessedAtEpochMillis
            )
            SELECT DISTINCT ticker, NULL, CAST(strftime('%s', 'now') AS INTEGER) * 1000
            FROM etf_price_history
            """.trimIndent()
        )
    }
}
