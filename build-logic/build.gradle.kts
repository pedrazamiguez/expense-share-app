plugins {
    `kotlin-dsl`
}

dependencies {
    // compileOnly is correct here — this is the standard pattern for precompiled script plugins
    // in an included build (see NowInAndroid, Gradle docs).
    //
    // These JARs are on the compile classpath so convention plugin code can reference DSL types
    // (e.g. DetektExtension, KtlintExtension, LibraryExtension) without import errors.
    // At runtime, when a convention plugin is applied to a subproject, Gradle's classloader
    // hierarchy makes these same JARs available via the parent build's plugin classpath
    // (declared with `apply false` in the root build.gradle.kts). No ClassNotFoundException
    // occurs — our full build + quality-tool runs confirm this.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

