package com.d102.wye.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 가격 이력 API의 마지막 성공 동기화 시각.
 *
 * 휴장일처럼 새 가격 행이 생기지 않는 날에도 성공 시각을 기록해
 * 동일한 빈 구간을 반복 조회하지 않도록 한다.
 */
@Entity(tableName = "etf_price_cache_sync")
data class EtfPriceCacheSyncEntity(
    @PrimaryKey val ticker: String,
    val lastSuccessfulSyncEpochMillis: Long?,
    val lastAccessedAtEpochMillis: Long
)
