plugins {
    id("splittrip.android.feature")
}

android {
    namespace = "es.pedrazamiguez.splittrip.features.group"
}

dependencies {
    implementation(libs.coil.compose)
}
