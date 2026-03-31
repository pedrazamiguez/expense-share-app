import java.io.StringReader
import java.util.Properties

plugins {
    id("expenseshare.android.application")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
}

val versionProps = Properties()
val versionFileText: Provider<String> = providers.fileContents(
    layout.projectDirectory.file("../version.properties")
).asText
if (versionFileText.isPresent) {
    versionProps.load(StringReader(versionFileText.get()))
}

val vMajor = versionProps.getProperty("versionMajor")?.toInt() ?: 0
val vMinor = versionProps.getProperty("versionMinor")?.toInt() ?: 0
val vPatch = versionProps.getProperty("versionPatch")?.toInt() ?: 0
val isSnapshot = versionProps.getProperty("versionSnapshot")?.toBoolean() ?: true

val baseVersionName = "$vMajor.$vMinor.$vPatch"
val appVersionName = if (isSnapshot) "$baseVersionName-SNAPSHOT" else baseVersionName
val appVersionCode = vMajor * 1000000 + vMinor * 1000 + vPatch

android {
    namespace = "es.pedrazamiguez.expenseshareapp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "es.pedrazamiguez.expenseshareapp"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "es.pedrazamiguez.expenseshareapp.TestRunner"
    }

    signingConfigs {
        create("release") {
            val storeFileEnv = providers.environmentVariable("SIGNING_STORE_FILE").orNull
            val keyAliasEnv = providers.environmentVariable("SIGNING_KEY_ALIAS").orNull
            val keyPasswordEnv = providers.environmentVariable("SIGNING_KEY_PASSWORD").orNull
            val storePasswordEnv = providers.environmentVariable("SIGNING_STORE_PASSWORD").orNull

            storeFile = if (storeFileEnv != null) {
                file(storeFileEnv)
            } else {
                val storeFileProp = providers.gradleProperty("EXSHAPP_RELEASE_STORE_FILE").orNull
                if (storeFileProp != null) {
                    file(storeFileProp)
                } else {
                    file("../keystore/release.keystore")
                }
            }

            keyAlias = keyAliasEnv ?: providers.gradleProperty("EXSHAPP_RELEASE_KEY_ALIAS").orNull ?: ""
            keyPassword = keyPasswordEnv ?: providers.gradleProperty("EXSHAPP_RELEASE_KEY_PASSWORD").orNull ?: ""
            storePassword = storePasswordEnv ?: providers.gradleProperty("EXSHAPP_RELEASE_STORE_PASSWORD").orNull ?: ""
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
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
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.coil.compose)
    implementation(libs.timber)
    testImplementation(libs.junit4)
    testImplementation(libs.mockk)

    debugImplementation(libs.leakcanary)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":features"))
}
