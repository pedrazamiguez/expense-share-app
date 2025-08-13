plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
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

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false

            val apiKey = project.findProperty("OPEN_EXCHANGE_RATES_API_KEY_USER")?.toString()
                ?: project.findProperty("OPEN_EXCHANGE_RATES_API_KEY")
                    ?.toString() // Fallback if you use a generic name
                ?: "YOUR_DEBUG_API_KEY_PLACEHOLDER_FOR_DATA" // Fallback if not found anywhere

            buildConfigField("String", "OPEN_EXCHANGE_RATES_API_KEY", "\"$apiKey\"")
            println("Data module debug API key: $apiKey")
        }
        getByName("release") {
            isMinifyEnabled = false

            val apiKeyFromEnv = System.getenv("OPEN_EXCHANGE_RATES_API_KEY_PROD")
            val apiKeyFromGradleProps =
                project.findProperty("OPEN_EXCHANGE_RATES_API_KEY_PROD")?.toString()
                    ?: project.findProperty("OPEN_EXCHANGE_RATES_API_KEY_USER")?.toString()

            val apiKey = apiKeyFromEnv ?: apiKeyFromGradleProps ?: run {
                if (System.getenv("CI").toBoolean()) {
                    throw GradleException("Production API key (OPEN_EXCHANGE_RATES_API_KEY_PROD) not found in environment variables for :data module release build.")
                } else {
                    "LOCAL_RELEASE_API_KEY_PLACEHOLDER_FOR_DATA"
                }
            }

            buildConfigField("String", "OPEN_EXCHANGE_RATES_API_KEY", "\"$apiKey\"")
            println("Data module release API key source: ${if (apiKeyFromEnv != null) "Env Var" else if (apiKeyFromGradleProps != null) "Gradle Prop" else "Placeholder/Error"}")
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit4)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(project(":domain"))
}
