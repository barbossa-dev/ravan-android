import java.util.Properties

plugins {
    alias(libs.plugins.ravan.android.application)
    alias(libs.plugins.ravan.android.application.compose)
    alias(libs.plugins.ravan.hilt)
}

/**
 * Release signing is read from `keystore.properties` (git-ignored, see keystore.properties.example
 * and scripts/create-release-keystore.sh). Without it the release build is signed with the debug key
 * so it can still be installed and tested locally — never publish that build.
 */
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}
val hasReleaseKeystore = keystoreProperties.getProperty("storeFile") != null

android {
    namespace = "ir.famastudio.ravan"

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    defaultConfig {
        applicationId = libs.versions.projectApplicationId.get()
        versionCode = libs.versions.projectVersionCode.get().toInt()
        versionName = libs.versions.projectVersionName.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            // R8 full mode (default since AGP 8): shrinking, optimisation and obfuscation.
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        buildConfig = false
    }

    // The UI is Persian-only; drop the translations that AndroidX libraries bring along.
    androidResources {
        localeFilters += listOf("fa", "en")
    }

    bundle {
        language { enableSplit = false }
    }

    lint {
        checkReleaseBuilds = true
        abortOnError = true
        warningsAsErrors = false
        disable += setOf("MissingTranslation", "ExtraTranslation")
    }

    dependenciesInfo {
        // Cafe Bazaar / Myket do not need the Google-encrypted dependency block.
        includeInApk = false
        includeInBundle = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.feature.editor)
    implementation(projects.feature.home)
    implementation(projects.feature.library)
    implementation(projects.feature.review)
    implementation(projects.feature.settings)
    implementation(projects.feature.word)

    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.ui)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Store-screenshot test (src/androidTest/StoreScreenshots.kt).
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Store screenshots from the IDE: if an empty `.store-screenshots` file exists in the project
// root, Build › Assemble Module 'Ravan.app' also runs the screenshot test
// (src/androidTest/StoreScreenshots.kt) on the connected emulator.
if (rootProject.file(".store-screenshots").exists()) {
    tasks.matching { it.name == "assembleRelease" }.configureEach { finalizedBy("connectedDebugAndroidTest") }
}
