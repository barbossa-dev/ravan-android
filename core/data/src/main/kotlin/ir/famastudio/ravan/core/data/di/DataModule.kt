package ir.famastudio.ravan.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.famastudio.ravan.core.data.repository.BackupRepository
import ir.famastudio.ravan.core.data.repository.DeckRepository
import ir.famastudio.ravan.core.data.repository.FileBackupRepository
import ir.famastudio.ravan.core.data.repository.OfflineDeckRepository
import ir.famastudio.ravan.core.data.repository.OfflineReviewRepository
import ir.famastudio.ravan.core.data.repository.OfflineUserDataRepository
import ir.famastudio.ravan.core.data.repository.OfflineWordRepository
import ir.famastudio.ravan.core.data.repository.ReviewRepository
import ir.famastudio.ravan.core.data.repository.UserDataRepository
import ir.famastudio.ravan.core.data.repository.WordRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {
    @Binds
    fun bindsWordRepository(repository: OfflineWordRepository): WordRepository

    @Binds
    fun bindsReviewRepository(repository: OfflineReviewRepository): ReviewRepository

    @Binds
    fun bindsDeckRepository(repository: OfflineDeckRepository): DeckRepository

    @Binds
    fun bindsUserDataRepository(repository: OfflineUserDataRepository): UserDataRepository

    @Binds
    fun bindsBackupRepository(repository: FileBackupRepository): BackupRepository
}
