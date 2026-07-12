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
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
}
