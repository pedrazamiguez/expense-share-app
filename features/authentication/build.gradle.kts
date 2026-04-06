plugins {
    id("splittrip.android.feature")
}

android {
    namespace = "es.pedrazamiguez.splittrip.features.authentication"
}

dependencies {
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
}
