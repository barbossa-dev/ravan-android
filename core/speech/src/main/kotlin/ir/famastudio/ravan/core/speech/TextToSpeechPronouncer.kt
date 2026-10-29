package ir.famastudio.ravan.core.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.famastudio.ravan.core.model.Accent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device pronunciation using the platform TTS engine (works offline once the
 * English voice data is installed). The engine is created lazily on first use.
 */
@Singleton
internal class TextToSpeechPronouncer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : Pronouncer {

    private var engine: TextToSpeech? = null
    private var isReady = false
    private var pending: (() -> Unit)? = null

    private val speaking = MutableStateFlow<String?>(null)
    override val speakingText: StateFlow<String?> = speaking.asStateFlow()

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            speaking.value = utteranceId
        }

        override fun onDone(utteranceId: String?) {
            speaking.compareAndSet(utteranceId, null)
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            speaking.compareAndSet(utteranceId, null)
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            speaking.compareAndSet(utteranceId, null)
        }
    }

    override fun speak(text: String, accent: Accent, rate: Float) {
        if (text.isBlank()) return
        val utterance = {
            val tts = requireEngine()
            val locale = when (accent) {
                Accent.US -> Locale.US
                Accent.UK -> Locale.UK
            }
            val result = tts.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.ENGLISH)
            }
            tts.setSpeechRate(rate.coerceIn(0.3f, 2f))
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
        }
        if (isReady) utterance() else {
            pending = { utterance() }
            requireEngine()
        }
    }

    override fun stop() {
        engine?.stop()
        speaking.value = null
    }

    private fun requireEngine(): TextToSpeech = engine ?: TextToSpeech(context.applicationContext) { status ->
        isReady = status == TextToSpeech.SUCCESS
        if (isReady) pending?.invoke()
        pending = null
    }.also { tts ->
        tts.setOnUtteranceProgressListener(progressListener)
        engine = tts
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal interface SpeechModule {
    @Binds
    fun bindsPronouncer(pronouncer: TextToSpeechPronouncer): Pronouncer
}
