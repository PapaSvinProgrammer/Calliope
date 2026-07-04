plugins {
    id("radio.android.compose.library")
}

android {
    namespace = "com.mordva.system_ui"
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.core.ktx)

}