plugins {
    id("expenseshare.android.feature")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.features.profile"
}

dependencies {
    implementation(libs.coil.compose)
}
