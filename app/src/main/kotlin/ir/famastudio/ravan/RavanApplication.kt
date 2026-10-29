package ir.famastudio.ravan

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ir.famastudio.ravan.core.common.di.ApplicationScope
import ir.famastudio.ravan.core.data.repository.BackupRepository
import ir.famastudio.ravan.core.data.repository.WordRepository
import ir.famastudio.ravan.core.data.seed.DictionarySeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class RavanApplication : Application() {

    @Inject
    lateinit var dictionarySeeder: DictionarySeeder

    @Inject
    lateinit var wordRepository: WordRepository

    @Inject
    lateinit var backupRepository: BackupRepository

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Load the bundled offline dictionary on first launch; the UI observes Room and updates itself.
        applicationScope.launch {
            dictionarySeeder.seedIfNeeded()
            // Debug builds only (no-op in release): sample study history for store screenshots.
            DemoLibrary.installOnce(this@RavanApplication, wordRepository, backupRepository)
        }
    }
}
