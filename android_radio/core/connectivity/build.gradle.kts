plugins {
    id("radio.android.library.core")
}

android {
    namespace = "com.mordva.connectivity"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
