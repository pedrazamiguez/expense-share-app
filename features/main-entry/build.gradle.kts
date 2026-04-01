plugins {
    id("expenseshare.android.feature")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.features.main"
}

dependencies {
    implementation(libs.haze)
}
