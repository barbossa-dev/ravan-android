plugins {
    alias(libs.plugins.ravan.android.library)
    alias(libs.plugins.ravan.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ir.famastudio.ravan.core.data"
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(libs.kotlinx.serialization.json)
}
