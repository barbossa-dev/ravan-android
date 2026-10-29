package ir.famastudio.ravan.core.data.backup

import ir.famastudio.ravan.core.database.model.DeckEntity
import ir.famastudio.ravan.core.database.model.ExampleDb
import ir.famastudio.ravan.core.database.model.MeaningDb
import ir.famastudio.ravan.core.database.model.ReviewCardEntity
import ir.famastudio.ravan.core.database.model.ReviewLogEntity
import ir.famastudio.ravan.core.database.model.WordEntity

internal fun WordEntity.asBackup() = BackupWord(
    id = id,
    text = text,
    phoneticUs = phoneticUs,
    phoneticUk = phoneticUk,
    partOfSpeech = partOfSpeech,
    level = level,
    meanings = meanings.map { BackupMeaning(it.translation, it.description) },
    examples = examples.map { BackupExample(it.english, it.persian) },
    synonyms = synonyms,
    antonyms = antonyms,
    note = note,
    isFavorite = isFavorite,
    isUserAdded = isUserAdded,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastViewedAt = lastViewedAt,
    frequencyRank = frequencyRank,
)

internal fun ReviewCardEntity.asBackup() = BackupCard(
    wordId = wordId,
    box = box,
    dueAt = dueAt,
    reviewCount = reviewCount,
    correctCount = correctCount,
    lastReviewedAt = lastReviewedAt,
    addedAt = addedAt,
)

internal fun BackupCard.asEntity() = ReviewCardEntity(
    wordId = wordId,
    box = box.coerceIn(1, 6),
    dueAt = dueAt,
    reviewCount = reviewCount,
    correctCount = correctCount,
    lastReviewedAt = lastReviewedAt,
    addedAt = addedAt,
)

internal fun DeckEntity.asBackup(wordIds: List<String>) = BackupDeck(
    id = id,
    name = name,
    colorIndex = colorIndex,
    createdAt = createdAt,
    wordIds = wordIds,
)

internal fun ReviewLogEntity.asBackup() = BackupReviewLog(
    id = id,
    wordId = wordId,
    reviewedAt = reviewedAt,
    correct = correct,
)

internal fun BackupMeaning.asDb() = MeaningDb(translation, description)
internal fun BackupExample.asDb() = ExampleDb(english, persian)
