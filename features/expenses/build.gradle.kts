plugins {
    id("expenseshare.android.feature")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.features.expense"
}

dependencies {
    // Image loading for receipt photos
    implementation(libs.coil.compose)
}
