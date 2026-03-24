plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events(
            "passed",
            "skipped",
            "failed"
        )
    }
    // Konsist tests scan the full project source — give them more heap
    jvmArgs("-Xmx2g")
}
