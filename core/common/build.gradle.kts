plugins {
    id("expenseshare.android.library")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.core.common"
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.coroutines.core)
    api(libs.timber)

    // Unit testing (extras — common test deps provided by convention plugin)
    testImplementation(libs.koin.test)
}
