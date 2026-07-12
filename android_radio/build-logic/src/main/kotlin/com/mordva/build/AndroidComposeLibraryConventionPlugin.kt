package com.mordva.build

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension>("android") {
                compileSdk = 37

                defaultConfig {
                    minSdk = 26

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    consumerProguardFiles("consumer-rules.pro")
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }

                buildFeatures {
                    compose = true
                }
            }

            val libs = extensions
                .getByType(VersionCatalogsExtension::class.java)
                .named("libs")

            dependencies {
                add("implementation", platform(libs.findLibrary("androidx-compose-bom").get()))
                add("implementation", libs.findLibrary("androidx-compose-material3").get())
                add("implementation", libs.findLibrary("androidx-compose-ui").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            }
        }
    }
}
