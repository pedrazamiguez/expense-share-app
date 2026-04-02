plugins {
    id("expenseshare.android.feature")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.features.authentication"
}

dependencies {
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
}
