plugins {
    id("splittrip.android.library")
}

android {
    namespace = "es.pedrazamiguez.splittrip.core.performance"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:logging"))

    implementation(libs.koin.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.perf)
}
