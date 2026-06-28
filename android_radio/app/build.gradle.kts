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
    implementation(project(":core:network"))
    implementation(project(":core:navigation"))
    implementation(project(":features:home"))
}
