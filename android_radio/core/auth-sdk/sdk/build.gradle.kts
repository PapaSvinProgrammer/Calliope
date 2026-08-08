plugins {
    id("radio.android.library.core")
}

android {
    namespace = "com.mordva.auth_sdk"

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
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.logging.interceptor)
    implementation(libs.tink.android)
    implementation(projects.core.authSdk.datastore)
}