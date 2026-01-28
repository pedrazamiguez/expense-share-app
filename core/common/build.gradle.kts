plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.core.common"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.datastore.preferences)
    api(libs.timber)

    // Unit testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
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
