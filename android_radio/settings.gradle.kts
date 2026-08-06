pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RadioCalliope"
include(":app")
include(":core:network")
include(":core:navigation")
include(":features:home:domain")
include(":features:home:presentation")
include(":core:system-ui")
include(":features:control-bar")
include(":core:datastore:api")
include(":core:datastore:impl")
include(":core:auth-sdk")
include(":core:auth-sdk:datastore")
include(":core:auth-sdk:sdk")
include(":features:filter")
include(":features:filter:presentation")
include(":features:filter:domain")
include(":core:player")
