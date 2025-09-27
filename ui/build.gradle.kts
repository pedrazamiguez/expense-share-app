plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Internal UI dependencies for AppNavHost
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Module dependencies
    implementation(project(":core"))

    // Each feature module
    api(project(":ui:feature-activitylog"))
    api(project(":ui:feature-auth"))
    api(project(":ui:feature-balances"))
    api(project(":ui:feature-expenses"))
    api(project(":ui:feature-groups"))
    api(project(":ui:feature-main"))
    api(project(":ui:feature-onboarding"))
    api(project(":ui:feature-profile"))
    api(project(":ui:feature-settings"))

}
