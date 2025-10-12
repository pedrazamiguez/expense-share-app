plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)

    // Unit Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.koin.test)
}

kotlin {
    jvmToolchain(17)
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
