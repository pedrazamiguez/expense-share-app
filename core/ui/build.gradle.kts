plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.core.ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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

    implementation(project(":domain"))
    implementation(project(":core:config"))

    // Compose BOM — exported so all consumers align
    api(platform(libs.androidx.compose.bom))

    // Compose essentials
    api(libs.androidx.ui)
    api(libs.androidx.material3)
    api(libs.material.icons.extended)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.runtime)

    // Debug tooling (only for dev/test)
    debugApi(libs.androidx.ui.tooling)
    debugApi(libs.androidx.ui.test.manifest)

    // Navigation
    api(libs.androidx.navigation.compose)

    // DI integration
    api(libs.koin.android)
    api(libs.koin.compose)

    // Unit Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.koin.test)
}

tasks
    .withType<Test>()
    .configureEach {
        useJUnitPlatform()
        testLogging {
            events(
                "passed",
                "skipped",
                "failed"
            )
        }
    }
