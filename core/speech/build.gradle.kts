plugins {
    alias(libs.plugins.ravan.android.library)
    alias(libs.plugins.ravan.hilt)
}

android {
    namespace = "ir.famastudio.ravan.core.speech"
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.core)
}
