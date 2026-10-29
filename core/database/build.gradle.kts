plugins {
    alias(libs.plugins.ravan.android.library)
    alias(libs.plugins.ravan.android.room)
    alias(libs.plugins.ravan.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ir.famastudio.ravan.core.database"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
