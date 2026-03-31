plugins {
    id("expenseshare.android.library.compose")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.features.profile"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:design-system"))
    implementation(project(":domain"))
    implementation(libs.coil.compose)

    // Unit Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}
