plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "es.pedrazamiguez.expenseshareapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val storeFileEnv = System.getenv("SIGNING_STORE_FILE")
            val keyAliasEnv = System.getenv("SIGNING_KEY_ALIAS")
            val keyPasswordEnv = System.getenv("SIGNING_KEY_PASSWORD")
            val storePasswordEnv = System.getenv("SIGNING_STORE_PASSWORD")

            storeFile = if (storeFileEnv != null) {
                file(storeFileEnv)
            } else {
                val storeFileProp = project.findProperty("EXSHAPP_RELEASE_STORE_FILE") as String?
                if (storeFileProp != null) {
                    file(storeFileProp)
                } else {
                    file("../keystore/release.keystore")
                }
            }

            keyAlias =
                keyAliasEnv ?: project.findProperty("EXSHAPP_RELEASE_KEY_ALIAS") as String? ?: ""
            keyPassword =
                keyPasswordEnv ?: project.findProperty("EXSHAPP_RELEASE_KEY_PASSWORD") as String?
                        ?: ""
            storePassword =
                storePasswordEnv ?: project.findProperty("EXSHAPP_RELEASE_STORE_PASSWORD") as String?
                        ?: ""
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.coil.compose)
    implementation(libs.timber)
    debugImplementation(libs.leakcanary)
    testImplementation(libs.junit4)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":ui"))
    implementation(project(":ui:auth"))
    implementation(project(":common"))
}
