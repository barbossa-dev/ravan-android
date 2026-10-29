plugins {
    alias(libs.plugins.ravan.android.library)
}

android {
    namespace = "ir.famastudio.ravan.core.domain"
}

dependencies {
    api(projects.core.data)
    api(projects.core.model)
    implementation(libs.javax.inject)
}
