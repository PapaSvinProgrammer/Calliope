plugins {
    id("radio.android.feature")
}

android {
    namespace = "com.mordva.feature.search.presentation"
}

dependencies {
    implementation(projects.features.home.domain)
    implementation(projects.core.connectivity)
    implementation(projects.core.player)
}