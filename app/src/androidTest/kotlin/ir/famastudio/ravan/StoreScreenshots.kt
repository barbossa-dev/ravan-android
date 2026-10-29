package ir.famastudio.ravan

import android.graphics.Bitmap
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Captures the Google Play screenshots in light and dark theme.
 *
 * Run `./gradlew :app:connectedDebugAndroidTest` with an emulator running. The debug build
 * installs a sample study history (see src/debug/DemoLibrary.kt). The PNGs are pulled to
 * `app/build/outputs/connected_android_test_additional_output/debugAndroidTest/connected/<device>/`.
 */
@RunWith(AndroidJUnit4::class)
class StoreScreenshots {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    private val outputDir: File by lazy {
        val base = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")?.let(::File)
            ?: File(instrumentation.targetContext.getExternalFilesDir(null), "screenshots")
        File(base, "ravan-screenshots").apply { mkdirs() }
    }

    @Test
    fun captureStoreScreenshots() {
        // First launch imports the dictionary and the demo library; wait for "recently viewed".
        compose.waitUntil(timeoutMillis = 300_000) { exists(hasText("brave") and hasClickAction()) }
        settle()

        for (theme in listOf("light", "dark")) {
            openTab(SETTINGS)
            click(hasText(if (theme == "dark") "تیره" else "روشن") and hasClickAction())
            settle()

            openTab(HOME)
            capture(theme, "01-home")

            click(hasText("brave") and hasClickAction())
            capture(theme, "02-word")
            back()

            val field = compose.onNode(hasSetTextAction())
            field.performTextInput("res")
            field.performImeAction() // clears focus, hides the keyboard
            capture(theme, "03-search")
            compose.onNode(hasSetTextAction()).performTextClearance()
            compose.onNode(hasSetTextAction()).performImeAction()

            openTab(REVIEW)
            capture(theme, "05-progress")

            click(hasText("شروع مرور") and hasClickAction())
            click(hasText("نمایش پاسخ") and hasClickAction())
            capture(theme, "04-review-card")
            back()

            openTab(LIBRARY)
            capture(theme, "06-library")
        }
    }

    private fun exists(matcher: SemanticsMatcher) =
        compose.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()

    private fun click(matcher: SemanticsMatcher) {
        compose.waitUntil(timeoutMillis = 15_000) { exists(matcher) }
        compose.onAllNodes(matcher).onFirst().performClick()
        settle()
    }

    private fun openTab(label: String) = click(hasText(label) and hasClickAction())

    private fun back() {
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        settle()
    }

    private fun settle() {
        compose.waitForIdle()
        Thread.sleep(1_500)
    }

    private fun capture(theme: String, name: String) {
        settle()
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        val file = File(outputDir, "$theme/$name.png").apply { parentFile?.mkdirs() }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private companion object {
        const val HOME = "جستجو"
        const val LIBRARY = "واژه‌ها"
        const val REVIEW = "مرور"
        const val SETTINGS = "تنظیمات"
    }
}
