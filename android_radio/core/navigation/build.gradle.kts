plugins {
    id("radio.android.library.core")
}

android {
    namespace = "com.mordva.navigation"
}

dependencies {
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.json)
}
