plugins {
    id("radio.android.library.core")
}

android {
    namespace = "com.mordva.auth_sdk.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.tink.android)
}