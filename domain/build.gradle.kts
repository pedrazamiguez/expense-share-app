plugins {
    id("expenseshare.jvm.library")
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)

    // Unit Testing (extras — common test deps provided by convention plugin)
    testImplementation(libs.koin.test)
}
