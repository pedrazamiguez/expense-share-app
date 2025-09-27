pluginManagement {
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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ExpenseShareApp"
include(":app")
include(":core")
include(":core:config")
include(":core:ui")
include(":data")
include(":data:firebase")
include(":data:local")
include(":data:remote")
include(":domain")
include(":ui")
include(":ui:feature-auth")
include(":ui:feature-groups")
include(":ui:feature-expenses")
include(":ui:feature-balances")
include(":ui:feature-settings")
include(":ui:feature-onboarding")
include(":ui:feature-main")
include(":ui:feature-activitylog")
include(":ui:feature-profile")
