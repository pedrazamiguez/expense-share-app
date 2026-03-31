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

