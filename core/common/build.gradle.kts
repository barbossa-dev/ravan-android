plugins {
    alias(libs.plugins.ravan.android.library)
    alias(libs.plugins.ravan.hilt)
}

android {
    namespace = "ir.famastudio.ravan.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)
}
