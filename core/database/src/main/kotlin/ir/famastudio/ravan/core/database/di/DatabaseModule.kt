package ir.famastudio.ravan.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.famastudio.ravan.core.database.RavanDatabase
import ir.famastudio.ravan.core.database.RoomTransactionRunner
import ir.famastudio.ravan.core.database.TransactionRunner
import ir.famastudio.ravan.core.database.dao.DeckDao
import ir.famastudio.ravan.core.database.dao.ReviewDao
import ir.famastudio.ravan.core.database.dao.WordDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): RavanDatabase =
        Room.databaseBuilder(context, RavanDatabase::class.java, "ravan-database").build()

    @Provides
    fun providesWordDao(database: RavanDatabase): WordDao = database.wordDao()

    @Provides
    fun providesReviewDao(database: RavanDatabase): ReviewDao = database.reviewDao()

    @Provides
    fun providesDeckDao(database: RavanDatabase): DeckDao = database.deckDao()
}

@Module
@InstallIn(SingletonComponent::class)
internal interface TransactionModule {
    @Binds
    fun bindsTransactionRunner(runner: RoomTransactionRunner): TransactionRunner
}
