plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.data.remote"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {

        getByName("debug") {
            isMinifyEnabled = false

            // ** OER_API_BASE_URL **
            buildConfigField(
                "String",
                "OER_API_BASE_URL",
                "\"https://openexchangerates.org/api/\""
            )

            // ** OER_APP_ID **
            val appId =
                project
                    .findProperty("OER_APP_ID_DEBUG")
                    ?.toString() ?: "YOUR_DEBUG_OER_APP_ID"
            buildConfigField(
                "String",
                "OER_APP_ID",
                "\"$appId\""
            )

            val displayedAppId = if (appId == "YOUR_DEBUG_OER_APP_ID") {
                appId // Don't shade the placeholder
            } else if (appId.length > 7) { // Ensure enough length to show first 3 and last 4
                "${appId.take(3)}...${appId.takeLast(4)}"
            } else if (appId.length > 4) { // If not long enough for first 3 + last 4, just show last 4
                "...${appId.takeLast(4)}"
            } else { // For very short IDs, shade most of it
                if (appId.isNotEmpty()) "*".repeat(appId.length) else "..."
            }
            println("Open Exchange Rates App ID for debug: $displayedAppId")
        }

        getByName("release") {
            isMinifyEnabled = false

            // ** OER_API_BASE_URL **
            buildConfigField(
                "String",
                "OER_API_BASE_URL",
                "\"https://openexchangerates.org/api/\""
            )

            // ** OER_APP_ID **
            val appIdFromEnv = System.getenv("OER_APP_ID_RELEASE")
            val appIdFromGradleProps = project
                .findProperty("OER_APP_ID_RELEASE")
                ?.toString()
            val appId = appIdFromEnv ?: appIdFromGradleProps ?: run {
                if (System
                        .getenv("CI")
                        ?.toBoolean() == true
                ) {
                    throw GradleException("Open Exchange Rates App ID for release (OER_APP_ID_RELEASE) not found in environment variables or gradle properties.")
                } else {
                    "YOUR_RELEASE_OER_APP_ID"
                }
            }

            buildConfigField(
                "String",
                "OER_APP_ID",
                "\"$appId\""
            )
            println("Open Exchange Rates App ID for release source: ${if (appIdFromEnv != null) "ENV VAR" else if (appIdFromGradleProps != null) "GRADLE PROP" else "PLACEHOLDER / ERROR"}")
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
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(project(":domain"))

    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)

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
