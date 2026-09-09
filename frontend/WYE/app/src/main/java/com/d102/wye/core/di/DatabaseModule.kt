package com.d102.wye.core.di

import android.content.Context
import androidx.room.Room
import com.d102.wye.core.app.Constants.DATABASE_NAME
import com.d102.wye.data.local.dao.EtfPriceCacheSyncDao
import com.d102.wye.data.local.dao.EtfPriceHistoryDao
import com.d102.wye.data.local.dao.LikedEtfDao
import com.d102.wye.data.local.database.AppDatabase
import com.d102.wye.data.local.database.MIGRATION_4_5
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(MIGRATION_4_5)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideLikedEtfDao(appDatabase: AppDatabase): LikedEtfDao =
        appDatabase.likedEtfDao()

    @Provides
    @Singleton
    fun provideEtfPriceHistoryDao(appDatabase: AppDatabase): EtfPriceHistoryDao =
        appDatabase.etfPriceHistoryDao()

    @Provides
    @Singleton
    fun provideEtfPriceCacheSyncDao(appDatabase: AppDatabase): EtfPriceCacheSyncDao =
        appDatabase.etfPriceCacheSyncDao()

//    @Provides
//    @Singleton
//    fun provideEtfFundamentalsDao(appDatabase: AppDatabase): EtfFundamentalsDao =
//        appDatabase.etfFundamentalsDao()
}
