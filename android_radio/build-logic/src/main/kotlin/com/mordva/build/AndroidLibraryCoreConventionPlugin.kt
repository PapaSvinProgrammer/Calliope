package com.mordva.build

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryCoreConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            extensions.configure<LibraryExtension>("android") {
                compileSdk = 37

                defaultConfig {
                    minSdk = 26

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                    val consumerRules = project.file("consumer-rules.pro")
                    if (consumerRules.exists()) {
                        consumerProguardFiles(consumerRules)
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }
            }
        }
    }
}
