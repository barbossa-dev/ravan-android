plugins {
    alias(libs.plugins.ravan.android.library)
    alias(libs.plugins.ravan.android.library.compose)
}

android {
    namespace = "ir.famastudio.ravan.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
    implementation(projects.core.common)
}
