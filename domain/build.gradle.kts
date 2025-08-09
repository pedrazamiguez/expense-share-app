plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.kotlin.coroutines)
    testImplementation(libs.junit4)
    testImplementation(libs.mockk)
}

kotlin {
    jvmToolchain(17)
}
