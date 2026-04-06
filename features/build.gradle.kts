plugins {
    id("splittrip.android.library")
}

android {
    namespace = "es.pedrazamiguez.splittrip.features"
}

dependencies {
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
}
