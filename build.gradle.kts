import de.aaschmid.gradle.plugins.cpd.Cpd
import io.gitlab.arturbosch.detekt.Detekt
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
    alias(libs.plugins.cpd)
    alias(libs.plugins.sonarqube)
}

val jacocoToolVersion: String = libs.versions.jacoco.get()

subprojects {
    pluginManager.withPlugin("com.android.application") {
        extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
            lint {
                ignoreTestSources = true
                checkDependencies = false
                ignoreWarnings = true
                abortOnError = true
                sarifReport = true
            }
        }
    }
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<com.android.build.api.dsl.LibraryExtension> {
            lint {
                ignoreTestSources = true
                checkDependencies = false
                ignoreWarnings = true
                abortOnError = true
                sarifReport = true
            }
        }
    }

    // ── Ktlint ──────────────────────────────────────────────────────────────
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<KtlintExtension> {
        version.set("1.5.0")
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }

    // ── Detekt ──────────────────────────────────────────────────────────────
    extensions.configure<DetektExtension> {
        config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        ignoreFailures = true
    }


    tasks.withType<Detekt>().configureEach {
        reports {
            sarif.required.set(true)
            html.required.set(true)
            xml.required.set(false)
            txt.required.set(false)
        }
    }

    // ── JaCoCo ──────────────────────────────────────────────────────────────
    apply(plugin = "jacoco")

    extensions.configure<JacocoPluginExtension> {
        toolVersion = jacocoToolVersion
    }

    val jacocoExcludes = listOf(
        // Android generated
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        // Koin DI modules (hand-written, not business logic)
        "**/*Module.*",
        "**/*Module\$*.*",
        // Compose generated
        "**/*ComposableSingletons*.*",
        // Room generated
        "**/*_Impl.*",
        "**/*Dao_Impl.*",
        // Preview helpers (debug source set)
        "**/*PreviewHelper*.*",
        // Sealed/data class companion objects
        "**/*\$Companion.*",
        // ── Compose UI — only reachable via instrumentation tests, not JUnit ──────
        // Feature orchestrators (hold NavController / ViewModel, not unit-testable)
        "**/presentation/feature/**",
        // Stateless screen composables + ScreenUiProvider impls
        "**/presentation/screen/**",
        // Reusable composable components
        "**/presentation/component/**",
        // Preview files (debug source set; PreviewHelper already excluded above)
        "**/presentation/preview/**",
        // Design-system: shared composable components, theme, navigation primitives
        "**/designsystem/presentation/**",
        "**/designsystem/foundation/**",
        "**/designsystem/navigation/**",
        "**/designsystem/permission/**",
    )

    // Android modules (library + application)
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<com.android.build.api.dsl.LibraryExtension> {
            @Suppress("UnstableApiUsage")
            buildTypes {
                getByName("debug") {
                    enableUnitTestCoverage = true
                }
            }
        }
        configureAndroidJacoco(jacocoExcludes)
    }
    pluginManager.withPlugin("com.android.application") {
        extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
            @Suppress("UnstableApiUsage")
            buildTypes {
                getByName("debug") {
                    enableUnitTestCoverage = true
                }
            }
        }
        configureAndroidJacoco(jacocoExcludes)
    }

    // Pure JVM modules (:domain)
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        tasks.named<Test>("test") {
            finalizedBy(tasks.named("jacocoTestReport"))
        }
        tasks.withType<JacocoReport>().configureEach {
            dependsOn(tasks.named("test"))
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
        }
    }
}

// ── JaCoCo: Merged report across all subprojects ────────────────────────────
// Apply the jacoco plugin at root level so that `jacocoClasspath` is auto-configured
// for the root-level JacocoReport task (required by Gradle 9.x+).
apply(plugin = "jacoco")
configure<JacocoPluginExtension> {
    toolVersion = jacocoToolVersion
}

tasks.register<JacocoReport>("jacocoMergedReport") {
    group = "verification"
    description = "Generates a merged JaCoCo coverage report for all subprojects"

    val jacocoExcludes = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Module.*", "**/*Module\$*.*",
        "**/*ComposableSingletons*.*",
        "**/*_Impl.*", "**/*Dao_Impl.*",
        "**/*PreviewHelper*.*",
        "**/*\$Companion.*",
    )

    val reportTasks = subprojects.flatMap { sub ->
        sub.tasks.withType<JacocoReport>().matching { it.name == "jacocoTestReport" }
    }
    dependsOn(reportTasks)

    // Execution data from all subprojects
    executionData.setFrom(
        fileTree(rootProject.projectDir) {
            include(
                "**/build/jacoco/*.exec",
                "**/build/outputs/unit_test_code_coverage/**/*.exec",
            )
        }
    )

    // Source directories
    sourceDirectories.setFrom(
        subprojects.flatMap { sub ->
            listOfNotNull(
                sub.file("src/main/kotlin").takeIf { it.exists() },
                sub.file("src/main/java").takeIf { it.exists() },
            )
        }
    )

    // Class directories (with exclusions)
    classDirectories.setFrom(
        subprojects.flatMap { sub ->
            listOf(
                // JVM module classes
                fileTree(sub.layout.buildDirectory.dir("classes/kotlin/main")) {
                    exclude(jacocoExcludes)
                },
                // Android module classes
                fileTree(sub.layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                    exclude(jacocoExcludes)
                },
            )
        }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/merged/html"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/merged/jacocoMergedReport.xml"))
    }
}

// ── CPD (Copy-Paste Detector) ───────────────────────────────────────────────
cpd {
    language = "kotlin"
    minimumTokenCount = 100
    isIgnoreFailures = true
}

tasks.withType<Cpd>().configureEach {
    reports {
        xml.required.set(true)
        text.required.set(true)
    }
    source = fileTree(rootProject.projectDir) {
        include(
            subprojects.map { sub ->
                sub.projectDir.relativeTo(rootProject.projectDir).path + "/src/main/kotlin/**/*.kt"
            }
        )
    }
}

// ── Helper: configure JaCoCo for Android modules ────────────────────────────
fun Project.configureAndroidJacoco(excludes: List<String>) {
    tasks.register<JacocoReport>("jacocoTestReport") {
        group = "verification"
        description = "Generates JaCoCo coverage report for debug unit tests"
        dependsOn("testDebugUnitTest")

        classDirectories.setFrom(
            fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                exclude(excludes)
            }
        )
        sourceDirectories.setFrom(files("src/main/kotlin", "src/main/java"))
        executionData.setFrom(
            fileTree(layout.buildDirectory) {
                include(
                    "jacoco/testDebugUnitTest.exec",
                    "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                )
            }
        )

        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

// ── SonarQube ───────────────────────────────────────────────────────────────
// Compatible with SonarQube Community Edition 9.9 CE (backward-compatible).
//
// AGP 9.x WORKAROUND: As of plugin 7.2.3, AndroidUtils still references the
// legacy AppExtension / LibraryExtension APIs removed in AGP 9.x, crashing
// during task configuration for ANY Android module (app or library).
// Workaround: skip ALL subprojects from the plugin's per-module auto-discovery
// and provide aggregated source, binary, and test paths at the ROOT project
// (which has no Android plugin applied, so AndroidUtils is never invoked).
// Track: https://sonarsource.atlassian.net/browse/SCANGRADLE-287

// Collect source & binary directories from subprojects, resolved at configuration time.
val sonarSources = mutableListOf<String>()
val sonarTests = mutableListOf<String>()
val sonarBinaries = mutableListOf<String>()

subprojects {
    // Skip every subproject so the plugin never calls AndroidUtils on them.
    sonarqube { isSkipProject = true }

    // Aggregate paths for the root-level scanner.
    afterEvaluate {
        val srcMain = file("src/main/kotlin")
        val srcTest = file("src/test/kotlin")
        if (srcMain.exists()) sonarSources.add(srcMain.absolutePath)
        if (srcTest.exists()) sonarTests.add(srcTest.absolutePath)

        // Android modules: classes go to tmp/kotlin-classes/debug
        // JVM modules: classes go to classes/kotlin/main
        val androidClasses = layout.buildDirectory.dir("tmp/kotlin-classes/debug").get().asFile
        val jvmClasses = layout.buildDirectory.dir("classes/kotlin/main").get().asFile
        when {
            plugins.hasPlugin("com.android.library") ||
                plugins.hasPlugin("com.android.application") ->
                sonarBinaries.add(androidClasses.absolutePath)

            plugins.hasPlugin("org.jetbrains.kotlin.jvm") ->
                sonarBinaries.add(jvmClasses.absolutePath)
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "expense-share-app")
        property("sonar.projectName", "ExpenseShareApp")

        // Skip implicit compilation — CI already compiles before running sonar.
        property("sonar.gradle.skipCompile", "true")

        // Aggregated source, test, and binary paths (populated by afterEvaluate above).
        property("sonar.sources", sonarSources.joinToString(","))
        property("sonar.tests", sonarTests.joinToString(","))
        property("sonar.java.binaries", sonarBinaries.joinToString(","))

        // Consume the merged JaCoCo XML produced by the jacocoMergedReport Gradle task.
        // The CI sonar job downloads this artifact before running ./gradlew sonar.
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.get()}/reports/jacoco/merged/jacocoMergedReport.xml",
        )

        // ── Coverage exclusions (mirrors jacocoExcludes above) ──────────────────
        // Keep both lists in sync: JaCoCo uses class-path globs, Sonar uses source-path globs.
        property(
            "sonar.coverage.exclusions",
            listOf(
                // Generated / boilerplate
                "**/*Module.kt",
                "**/*Module\$*.kt",
                "**/R.kt",
                "**/BuildConfig.kt",
                // Compose UI — only reachable via instrumentation tests, not JUnit
                "**/presentation/feature/**/*.kt",
                "**/presentation/screen/**/*.kt",
                "**/presentation/component/**/*.kt",
                "**/presentation/preview/**/*.kt",
                // Design-system: composable components, theme, navigation primitives
                "**/designsystem/presentation/**/*.kt",
                "**/designsystem/foundation/**/*.kt",
                "**/designsystem/navigation/**/*.kt",
                "**/designsystem/permission/**/*.kt",
            ).joinToString(","),
        )
        // ── Duplication exclusions ───────────────────────────────────────────────
        // Sonar's own CPD runs independently of the Gradle CPD plugin and has a lower
        // threshold. Compose's slot API / padding-parameter patterns produce structural
        // repetition that isn't meaningful duplication — exclude the UI layer.
        property(
            "sonar.cpd.exclusions",
            listOf(
                "**/*Module.kt",
                "**/presentation/component/**/*.kt",
                "**/presentation/screen/**/*.kt",
                "**/presentation/feature/**/*.kt",
                "**/designsystem/presentation/component/**/*.kt",
            ).joinToString(","),
        )
    }
}


// ── Git helper tasks ────────────────────────────────────────────────────────
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
    description = "Installs pre-commit hook for ktlint formatting checks"
    from("${rootProject.projectDir}/scripts/pre-commit")
    into("${rootProject.projectDir}/.git/hooks")
    filePermissions {
        unix("rwxr-xr-x")
    }
}

tasks.matching { it.name == "prepareKotlinBuildScriptModel" }.configureEach {
    dependsOn("installGitHooks")
}
