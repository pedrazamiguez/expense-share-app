import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

// Detekt 1.23.8 emits a Gradle 10 deprecation warning for ReportingExtension.file().
// This is a known upstream issue (https://github.com/detekt/detekt/issues/8452) fixed
// on their main branch. Upgrade to Detekt 2.0.0 stable once it is released.
val detektVersion: String = libs.versions.detekt.get()
val detektFormattingDep = libs.detekt.formatting.get().toString()

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<DetektExtension> {
        toolVersion = detektVersion
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
        baseline = file("detekt-baseline.xml")
    }

    extensions.configure<KtlintExtension> {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }

    dependencies {
        "detektPlugins"(detektFormattingDep)
        "detektPlugins"(project(":detekt-rules"))
    }

    pluginManager.withPlugin("com.android.application") {
        extensions.configure<ApplicationExtension> {
            lint {
                ignoreTestSources = true
                checkDependencies = false
                ignoreWarnings = true
                abortOnError = true
            }
        }
    }
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<LibraryExtension> {
            lint {
                ignoreTestSources = true
                checkDependencies = false
                ignoreWarnings = true
                abortOnError = true
            }
        }
    }
}

tasks.register<Exec>("pruneBranches") {
    group = "git"
    description = "Prunes local branches that no longer exist on the remote"

    inputs.file("$rootDir/scripts/prune-branches.sh")
    outputs.upToDateWhen { false }

    commandLine("sh", "$rootDir/scripts/prune-branches.sh")

    isIgnoreExitValue = true
}

tasks.register<Copy>("installGitHooks") {
    group = "git"
    description = "Installs the pre-commit hook that runs ktlintCheck and detekt"

    from("$rootDir/scripts/pre-commit")
    into("$rootDir/.git/hooks")
    filePermissions {
        user {
            read = true
            write = true
            execute = true
        }
        group {
            read = true
            execute = true
        }
        other {
            read = true
            execute = true
        }
    }
}

