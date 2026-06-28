plugins {
    id("radio.android.application")
}

android {
    namespace = "com.mordva.radio"

    defaultConfig {
        applicationId = "com.mordva.radio"
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(projects.core.network)
    implementation(projects.core.navigation)
    implementation(projects.features.home.presentation)
}
