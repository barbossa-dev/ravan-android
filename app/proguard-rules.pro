# ---------------------------------------------------------------------------------------------
# Ravan release rules (R8 full mode). Most libraries ship their own consumer rules (Hilt, Room,
# Compose, DataStore, kotlinx.coroutines, kotlinx.serialization); only app-specific needs live here.
# ---------------------------------------------------------------------------------------------

# Readable crash reports: keep line numbers, hide original file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# kotlinx.serialization: navigation routes (@Serializable data objects/classes), the bundled
# dictionary DTOs and the backup format. Navigation resolves route serializers by class, so the
# generated serializers, companions and object instances must survive shrinking.
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class ir.famastudio.ravan.**$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ir.famastudio.ravan.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
    <fields>;
}
-keepclasseswithmembers class ir.famastudio.ravan.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Enum names are persisted (DataStore preferences, SavedStateHandle) — keep them stable.
-keepclassmembers enum ir.famastudio.ravan.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Room-generated code is referenced reflectively by RoomDatabase.Builder.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
