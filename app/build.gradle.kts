import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

val versionProps = Properties()
val versionPropsFile = file("../version.properties")
if (versionPropsFile.exists()) {
    versionPropsFile.inputStream().use { versionProps.load(it) }
}

val vMajor = versionProps["versionMajor"]?.toString()?.toInt() ?: 0
val vMinor = versionProps["versionMinor"]?.toString()?.toInt() ?: 0
val vPatch = versionProps["versionPatch"]?.toString()?.toInt() ?: 0
val isSnapshot = versionProps["versionSnapshot"]?.toString()?.toBoolean() ?: true

val baseVersionName = "$vMajor.$vMinor.$vPatch"
val appVersionName = if (isSnapshot) "$baseVersionName-SNAPSHOT" else baseVersionName
// NOTE: The versionCode formula below supports:
// - Major: 0 - 21 (limited by Int.MAX_VALUE)
// - Minor: 0 - 999
// - Patch: 0 - 999
val appVersionCode = vMajor * 100000 + vMinor * 1000 + vPatch

android {
    namespace = "es.pedrazamiguez.expenseshareapp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "es.pedrazamiguez.expenseshareapp"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
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
            storePassword = storePasswordEnv
                ?: project.findProperty("EXSHAPP_RELEASE_STORE_PASSWORD") as String? ?: ""
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
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.coil.compose)
    implementation(libs.timber)
    testImplementation(libs.junit4)
    testImplementation(libs.mockk)

    debugImplementation(libs.leakcanary)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":features"))
}
