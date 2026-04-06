plugins {
    id("splittrip.android.feature")
}

android {
    namespace = "es.pedrazamiguez.splittrip.features.profile"
}

dependencies {
    implementation(libs.coil.compose)
}
