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

    // Internal UI dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI stack
    api(platform(libs.androidx.compose.bom)) // Export BOM so all features use same versions
    api(libs.androidx.ui)                    // Export if features directly use @Composable from UI
    api(libs.androidx.material3)             // Export Material3 if it’s your design system base
    implementation(libs.androidx.ui.graphics) // Internal, features usually don’t call this directly
    implementation(libs.androidx.runtime)     // Internal, part of Compose
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation — only export if features use NavHost/Compose navigation directly
    api(libs.androidx.navigation.compose)

    // Koin for DI — Export core libs, but no KSP here (handled in features)
    api(libs.koin.android)       // Export so features can inject ViewModels without adding Koin
    api(libs.koin.compose)
    api(libs.koin.annotations)

    // Image loading
    api(libs.coil.compose) // Export if UI components expose Coil images

    // Testing — Keep if you run tests at this level, or move to features if not used here
    testImplementation(libs.junit4)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.runtime)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Module dependencies
    api(project(":domain"))
    implementation(project(":core"))
    api(project(":common"))
}
