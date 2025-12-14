plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {

        getByName("debug") {
            // ** EXCHANGE_RATES_CACHE_DURATION_HOURS **
            buildConfigField(
                "long", "EXCHANGE_RATES_CACHE_DURATION_HOURS", "1L"
            )
        }

        getByName("release") {
            // ** EXCHANGE_RATES_CACHE_DURATION_HOURS **
            buildConfigField(
                "long", "EXCHANGE_RATES_CACHE_DURATION_HOURS", "24L"
            )
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(project(":domain"))
    api(project(":data:firebase"))
    api(project(":data:local"))
    api(project(":data:remote"))

    // Other dependencies
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.core)

    // Unit Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.koin.test)

}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events(
            "passed", "skipped", "failed"
        )
    }
}
