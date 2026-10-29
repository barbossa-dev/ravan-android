plugins {
    alias(libs.plugins.ravan.android.feature)
}

android {
    namespace = "ir.famastudio.ravan.feature.review"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.speech)
}
