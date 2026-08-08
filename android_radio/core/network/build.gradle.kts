plugins {
    id("radio.android.library.core")
}

android {
    namespace = "com.mordva.network"

    buildTypes {
        release {
            buildConfigField("boolean", "IS_DEBUG", "false")
        }

        debug {
            buildConfigField("boolean", "IS_DEBUG", "true")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(libs.retrofit)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.logging.interceptor)
    implementation(projects.core.authSdk.sdk)
}
