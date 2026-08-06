plugins {
    id("radio.android.feature")
}

android {
    namespace = "com.mordva.location.presentation"
}

dependencies {
    implementation(projects.features.location.domain)
    implementation(projects.core.datastore.api)
}