import ir.famastudio.ravan.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A feature module = Android library + Compose + Hilt + type-safe navigation.
 * Features depend on :core:ui / :core:designsystem / :core:domain / :core:data, never on each other.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("ravan.android.library")
                apply("ravan.android.library.compose")
                apply("ravan.hilt")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }
            dependencies {
                "implementation"(project(":core:ui"))
                "implementation"(project(":core:designsystem"))
                "implementation"(project(":core:model"))
                "implementation"(project(":core:common"))

                "implementation"(libs.findLibrary("androidx-hilt-navigation-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "implementation"(libs.findLibrary("androidx-navigation-compose").get())
                "implementation"(libs.findLibrary("kotlinx-serialization-json").get())

                "testImplementation"(libs.findLibrary("turbine").get())
            }
        }
    }
}
