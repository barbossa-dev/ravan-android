package ir.famastudio.ravan.core.model

/** A user-made collection of words ("دسته") that can be reviewed on its own. */
data class Deck(
    val id: String,
    val name: String,
    val colorIndex: Int,
    val createdAt: Long,
    val wordCount: Int = 0,
)
