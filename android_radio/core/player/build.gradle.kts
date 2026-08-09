plugins {
    id("radio.android.library.core")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mordva.player"

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
    implementation(projects.core.datastore.api)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.kotlinx.coroutines.guava)
}