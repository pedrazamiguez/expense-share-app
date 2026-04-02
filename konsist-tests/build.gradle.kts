plugins {
    id("expenseshare.jvm.library")
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    // Konsist tests scan the full project source — give them more heap
    jvmArgs("-Xmx2g")
}
