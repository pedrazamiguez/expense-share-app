plugins {
    id("splittrip.android.feature")
}

android {
    namespace = "es.pedrazamiguez.splittrip.features.main"
}

dependencies {
    implementation(libs.haze)
}
