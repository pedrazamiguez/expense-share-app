plugins {
    id("expenseshare.android.library")
}

android {
    namespace = "es.pedrazamiguez.expenseshareapp.core"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:design-system"))
}
