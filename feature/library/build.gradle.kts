plugins {
    alias(libs.plugins.ravan.android.feature)
}

android {
    namespace = "ir.famastudio.ravan.feature.library"
}

dependencies {
    implementation(projects.core.data)
}
