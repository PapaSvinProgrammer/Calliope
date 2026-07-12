plugins {
    id("radio.android.feature")
}

android {
    namespace = "com.mordva.feature.home"
}

dependencies {
    implementation(projects.features.home.domain)
}