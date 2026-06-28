plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.application)
    compileOnly(libs.android.library)
    compileOnly(libs.kotlin.android)
    compileOnly(libs.kotlin.compose)
    compileOnly(libs.kotlin.serialization)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "radio.android.application"
            implementationClass = "com.mordva.build.AndroidApplicationConventionPlugin"
        }
        register("androidLibraryCore") {
            id = "radio.android.library.core"
            implementationClass = "com.mordva.build.AndroidLibraryCoreConventionPlugin"
        }
        register("androidFeature") {
            id = "radio.android.feature"
            implementationClass = "com.mordva.build.AndroidFeatureConventionPlugin"
        }
        register("androidComposeLibrary") {
            id = "radio.android.compose.library"
            implementationClass = "com.mordva.build.AndroidComposeLibraryConventionPlugin"
        }
    }
}
