plugins {
    id("radio.android.feature")
}

android {
    namespace = "com.mordva.control_bar"
}
dependencies {
    implementation(projects.core.systemUi)
}