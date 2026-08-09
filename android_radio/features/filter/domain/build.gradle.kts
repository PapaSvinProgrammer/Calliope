plugins {
    id("radio.android.library.core")
}

android {
    namespace = "com.mordva.filter.domain"
}

dependencies {
    implementation(projects.core.network)
    implementation(projects.core.connectivity)
    implementation(libs.koin.android)
}