plugins {
    alias(libs.plugins.ravan.android.feature)
}

android {
    namespace = "ir.famastudio.ravan.feature.settings"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.speech)
    implementation(libs.androidx.activity.compose)
}
