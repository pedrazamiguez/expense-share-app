plugins {
    id("expenseshare.android.library.compose")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.core.designsystem"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))

    // Compose BOM — exported so all consumers align
    api(platform(libs.androidx.compose.bom))

    // Kotlinx immutable collections (used by shared UI components & UiState models)
    api(libs.kotlinx.collections.immutable)

    // Compose essentials
    api(libs.androidx.ui)
    api(libs.androidx.material3)
    api(libs.material.icons.extended)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.graphics.shapes)

    // Debug tooling (only for dev/test - not included in release APK)
    debugApi(libs.androidx.ui.tooling)
    debugApi(libs.androidx.ui.tooling.preview)
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
