## 🚀 Release Pull Request

### Release Information

**Version:** <!-- e.g., v0.7.1 -->
**Release Type:**

- [ ] 🐛 Patch (bug fixes)
- [ ] ✨ Minor (new features)
- [ ] 💥 Major (breaking changes)
- [ ] 🔥 Hotfix (critical fixes)

### Release Summary

<!-- Brief description of what's included in this release -->

### Changes Included

<!-- This section will be automatically populated by the changelog generation workflow -->
<!-- You can also manually list key changes if needed -->

#### 🆕 New Features
- 

#### 🐛 Bug Fixes
- 

#### 🔧 Improvements
- 

#### 🔥 Hotfixes
- 

### Pre-Release Checklist

- [ ] All features have been tested on develop branch
- [ ] Version number has been updated in `app/build.gradle.kts`
- [ ] All CI/CD checks are passing on develop
- [ ] Release notes have been prepared
- [ ] No pending critical issues

### Post-Merge Actions

- [ ] GitHub release will be automatically created
- [ ] APK will be automatically built and attached
- [ ] Git tag will be automatically created
- [ ] main branch will be synced back to develop (if needed)

### Testing Notes

- [ ] All automated tests are passing
- [ ] Manual testing completed on develop branch
- [ ] No regression issues identified

---
**Note:** This PR merges the `develop` branch into `main` to create a new release. Once merged, the
automated release workflow will trigger to build and publish the release.
