package ir.famastudio.ravan.core.database.util

import androidx.room.TypeConverter
import ir.famastudio.ravan.core.database.model.ExampleDb
import ir.famastudio.ravan.core.database.model.MeaningDb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Small lists are stored as JSON — they are always read together with their word. */
internal class RavanConverters {
    @TypeConverter
    fun meaningsToJson(value: List<MeaningDb>): String = json.encodeToString(meanings, value)

    @TypeConverter
    fun jsonToMeanings(value: String): List<MeaningDb> = json.decodeFromString(meanings, value)

    @TypeConverter
    fun examplesToJson(value: List<ExampleDb>): String = json.encodeToString(examples, value)

    @TypeConverter
    fun jsonToExamples(value: String): List<ExampleDb> = json.decodeFromString(examples, value)

    @TypeConverter
    fun stringsToJson(value: List<String>): String = json.encodeToString(strings, value)

    @TypeConverter
    fun jsonToStrings(value: String): List<String> = json.decodeFromString(strings, value)

    // Explicit types: KSP (Room) runs before the serialization compiler plugin generates
    // `serializer()`, so it cannot infer these types from their initializers.
    private companion object {
        val json: Json = Json { ignoreUnknownKeys = true }
        val meanings: KSerializer<List<MeaningDb>> = ListSerializer(MeaningDb.serializer())
        val examples: KSerializer<List<ExampleDb>> = ListSerializer(ExampleDb.serializer())
        val strings: KSerializer<List<String>> = ListSerializer(String.serializer())
    }
}
