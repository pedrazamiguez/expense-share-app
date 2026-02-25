plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.features"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    api(project(":features:activity-logging"))
    api(project(":features:authentication"))
    api(project(":features:balances"))
    api(project(":features:expenses"))
    api(project(":features:groups"))
    api(project(":features:main-entry"))

    api(project(":features:onboarding"))
    api(project(":features:profile"))
    api(project(":features:settings"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Immutable collections for Compose stability
    api(libs.kotlinx.collections.immutable)
}
