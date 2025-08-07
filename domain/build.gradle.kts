plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.kotlin.coroutines)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}

kotlin {
    jvmToolchain(17)
}