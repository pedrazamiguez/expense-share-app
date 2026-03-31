plugins {
    id("expenseshare.android.library")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.data"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {

        getByName("debug") {
            // ** EXCHANGE_RATES_CACHE_DURATION_HOURS **
            buildConfigField(
                "long",
                "EXCHANGE_RATES_CACHE_DURATION_HOURS",
                "1L"
            )
        }

        getByName("release") {
            // ** EXCHANGE_RATES_CACHE_DURATION_HOURS **
            buildConfigField(
                "long",
                "EXCHANGE_RATES_CACHE_DURATION_HOURS",
                "24L"
            )
        }
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
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.timber)

    // Unit Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.koin.test)
}
