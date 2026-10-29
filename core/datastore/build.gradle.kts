plugins {
    alias(libs.plugins.ravan.android.library)
    alias(libs.plugins.ravan.hilt)
}

android {
    namespace = "ir.famastudio.ravan.core.datastore"
}

dependencies {
    api(libs.androidx.dataStore.preferences)
    implementation(projects.core.common)
    implementation(projects.core.model)
}
