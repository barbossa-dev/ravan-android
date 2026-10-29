package ir.famastudio.ravan.core.speech

import ir.famastudio.ravan.core.model.Accent
import kotlinx.coroutines.flow.StateFlow

/** Speaks English text with an American or British voice. */
interface Pronouncer {
    /** The text currently being spoken, or null — lets the UI animate the right speaker button. */
    val speakingText: StateFlow<String?>

    fun speak(text: String, accent: Accent, rate: Float = 1f)

    fun stop()
}
