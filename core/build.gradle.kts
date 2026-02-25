plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.core"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    api(project(":core:common"))
    api(project(":core:design-system"))
}
