plugins {
    alias(libs.plugins.ravan.android.feature)
}

android {
    namespace = "ir.famastudio.ravan.feature.word"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.speech)
}
