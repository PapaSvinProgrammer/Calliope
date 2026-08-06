plugins {
    id("radio.android.feature")
}

android {
    namespace = "com.mordva.filter.presentation"
}

dependencies {
    implementation(projects.features.filter.domain)
    implementation(projects.core.datastore.api)
}