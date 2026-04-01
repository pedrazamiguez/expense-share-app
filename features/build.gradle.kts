plugins {
    id("expenseshare.android.library.compose")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.features"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    api(project(":features:activity-logging"))
    api(project(":features:authentication"))
    api(project(":features:balances"))
    api(project(":features:contributions"))
    api(project(":features:expenses"))
    api(project(":features:groups"))
    api(project(":features:subunits"))
    api(project(":features:main-entry"))

    api(project(":features:onboarding"))
    api(project(":features:profile"))
    api(project(":features:settings"))
    api(project(":features:withdrawals"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
}
