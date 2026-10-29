plugins {
    alias(libs.plugins.ravan.android.feature)
}

android {
    namespace = "ir.famastudio.ravan.feature.editor"
}

dependencies {
    implementation(projects.core.data)
}
