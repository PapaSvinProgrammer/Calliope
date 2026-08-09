plugins {
    id("radio.android.feature")
}

android {
    namespace = "com.mordva.feature.home"
}

dependencies {
    implementation(projects.features.home.domain)
    implementation(projects.core.connectivity)
    implementation(projects.core.datastore.api)
    implementation(projects.core.player)
}