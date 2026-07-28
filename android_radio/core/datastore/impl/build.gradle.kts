plugins {
    id("radio.android.library.core")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mordva.datastore.impl"
}

dependencies {
    api(projects.core.datastore.api)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)
    implementation(libs.tink.android)
}