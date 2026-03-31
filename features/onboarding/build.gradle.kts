plugins {
    id("expenseshare.android.library.compose")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.features.onboarding"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:design-system"))
    implementation(project(":domain"))
}
