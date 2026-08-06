plugins {
    id("radio.android.library.core")
}

android {
    namespace = "com.mordva.location.domain"
}

dependencies {
    implementation(projects.core.network)
    implementation(libs.koin.android)
}