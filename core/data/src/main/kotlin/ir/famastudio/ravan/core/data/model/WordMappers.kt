package ir.famastudio.ravan.core.data.model

import ir.famastudio.ravan.core.common.text.SearchNormalizer
import ir.famastudio.ravan.core.database.model.ExampleDb
import ir.famastudio.ravan.core.database.model.MeaningDb
import ir.famastudio.ravan.core.database.model.ReviewCardEntity
import ir.famastudio.ravan.core.database.model.WordEntity
import ir.famastudio.ravan.core.model.CefrLevel
import ir.famastudio.ravan.core.model.Example
import ir.famastudio.ravan.core.model.Meaning
import ir.famastudio.ravan.core.model.PartOfSpeech
import ir.famastudio.ravan.core.model.ReviewCard
import ir.famastudio.ravan.core.model.Word

internal fun WordEntity.asExternalModel() = Word(
    id = id,
    text = text,
    phoneticUs = phoneticUs,
    phoneticUk = phoneticUk,
    partOfSpeech = PartOfSpeech.fromKey(partOfSpeech),
    level = CefrLevel.fromKey(level),
    meanings = meanings.map { Meaning(it.translation, it.description) },
    examples = examples.map { Example(it.english, it.persian) },
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

internal fun Word.asEntity() = WordEntity(
    id = id,
    text = text.trim(),
    textLower = SearchNormalizer.normalize(text),
    searchText = searchText(),
    phoneticUs = phoneticUs.trim(),
    phoneticUk = phoneticUk.trim(),
    partOfSpeech = partOfSpeech.key,
    level = level?.name,
    meanings = meanings.map { MeaningDb(it.translation.trim(), it.description.trim()) },
    examples = examples.map { ExampleDb(it.english.trim(), it.persian.trim()) },
    synonyms = synonyms.map(String::trim).filter(String::isNotEmpty).distinct(),
    antonyms = antonyms.map(String::trim).filter(String::isNotEmpty).distinct(),
    note = note.trim(),
    isFavorite = isFavorite,
    isUserAdded = isUserAdded,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastViewedAt = lastViewedAt,
    frequencyRank = frequencyRank,
)

/** Everything a user may type to find this word: the headword, its Persian meanings and synonyms. */
private fun Word.searchText(): String =
    (listOf(text) + meanings.map { it.translation } + synonyms)
        .joinToString(separator = " | ") { SearchNormalizer.normalize(it) }

internal fun ReviewCardEntity.asExternalModel() = ReviewCard(
    wordId = wordId,
    box = box,
    dueAt = dueAt,
    reviewCount = reviewCount,
    correctCount = correctCount,
    lastReviewedAt = lastReviewedAt,
    addedAt = addedAt,
)

internal fun ReviewCard.asEntity() = ReviewCardEntity(
    wordId = wordId,
    box = box,
    dueAt = dueAt,
    reviewCount = reviewCount,
    correctCount = correctCount,
    lastReviewedAt = lastReviewedAt,
    addedAt = addedAt,
)

/** Stable ids for bundled words, identical on Android and iOS so backups move between platforms. */
internal fun seedWordId(text: String): String = "seed-" + text.trim().lowercase().replace(' ', '-')
