plugins {
    alias(libs.plugins.ravan.android.feature)
}

android {
    namespace = "ir.famastudio.ravan.feature.home"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.speech)
}
