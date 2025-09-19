plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    buildTypes {

        getByName("debug") {
            isMinifyEnabled = false

            // ** OER_API_BASE_URL **
            buildConfigField("String", "OER_API_BASE_URL", "\"https://openexchangerates.org/api/\"")

            // ** OER_APP_ID **
            val appId =
                project.findProperty("OER_APP_ID_DEBUG")?.toString() ?: "YOUR_DEBUG_OER_APP_ID"
            buildConfigField("String", "OER_APP_ID", "\"$appId\"")

            val displayedAppId = if (appId == "YOUR_DEBUG_OER_APP_ID") {
                appId // Don't shade the placeholder
            } else if (appId.length > 7) { // Ensure enough length to show first 3 and last 4
                "${appId.take(3)}...${appId.takeLast(4)}"
            } else if (appId.length > 4) { // If not long enough for first 3 + last 4, just show last 4
                "...${appId.takeLast(4)}"
            } else { // For very short IDs, shade most of it
                if (appId.isNotEmpty()) appId.first() + "...".take(appId.length -1) else "..."
            }
            println("Open Exchange Rates App ID for debug: $displayedAppId")

            // ** EXCHANGE_RATES_CACHE_DURATION_HOURS **
            buildConfigField("long", "EXCHANGE_RATES_CACHE_DURATION_HOURS", "1L")
        }

        getByName("release") {
            isMinifyEnabled = false

            // ** OER_API_BASE_URL **
            buildConfigField("String", "OER_API_BASE_URL", "\"https://openexchangerates.org/api/\"")

            // ** OER_APP_ID **
            val appIdFromEnv = System.getenv("OER_APP_ID_RELEASE")
            val appIdFromGradleProps = project.findProperty("OER_APP_ID_RELEASE")?.toString()
            val appId = appIdFromEnv ?: appIdFromGradleProps ?: run {
                if (System.getenv("CI")?.toBoolean() == true) {
                    throw GradleException("Open Exchange Rates App ID for release (OER_APP_ID_RELEASE) not found in environment variables or gradle properties.")
                } else {
                    "YOUR_RELEASE_OER_APP_ID"
                }
            }

            buildConfigField("String", "OER_APP_ID", "\"$appId\"")
            println("Open Exchange Rates App ID for release source: ${if (appIdFromEnv != null) "ENV VAR" else if (appIdFromGradleProps != null) "GRADLE PROP" else "PLACEHOLDER / ERROR"}")

            // ** EXCHANGE_RATES_CACHE_DURATION_HOURS **
            buildConfigField("long", "EXCHANGE_RATES_CACHE_DURATION_HOURS", "24L")
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
}

dependencies {
    // Core library desugaring (java.time, etc.)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)

    // Other dependencies
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.core)

    // Unit Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.vintage.engine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.koin.test)

    implementation(project(":domain"))
}

ksp {
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
