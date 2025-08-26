# Branch Cleanup and Alpha Branch Migration Guide

## Overview

This document outlines the branch cleanup process and migration to the new Alpha branch strategy for Project Myriad.

## New Branching Strategy

### Previous Strategy
- `main` - Production-ready code
- `develop` - Integration branch for features  
- `feature/*` - Individual feature development
- `hotfix/*` - Critical bug fixes

### New Strategy  
- `main` - Production-ready code
- `alpha` - Integration branch for features and pre-production testing
- `feature/*` - Individual feature development
- `hotfix/*` - Critical bug fixes

## Branches Recommended for Deletion

The following branches have been identified as obsolete and should be deleted:

### 1. `Android-rebranding`
**Status**: Appears to be completed rebranding work
**Last Commit**: 418ef6c92c297ecff83479e0927d3e1ea7ec6e6f
**Recommendation**: Delete after confirming all changes are merged to main

### 2. `copilot/fix-109` 
**Status**: Completed copilot fix branch
**Last Commit**: efaae75a1c5bffcbfa50fa91cafcca336fe738fc
**Recommendation**: Delete if the fix has been merged

### 3. `copilot/fix-114`
**Status**: Completed copilot fix branch  
**Last Commit**: 407e8748b3b2006a34a0a1ed882c00a2a3201154
**Recommendation**: Delete if the fix has been merged

### 4. `copilot/fix-131`
**Status**: Completed copilot fix branch
**Last Commit**: 8a95addb4487d6a883f0ada093b8c7d4ede14ad3
**Recommendation**: Delete if the fix has been merged

### 5. `copilot/fix-e6bf1298-409e-49b1-844b-92617a8ba059`
**Status**: Temporary UUID-named copilot branch
**Last Commit**: 430383d9a18a15dc5ed13029a32262230034fe16  
**Recommendation**: Delete - appears to be a temporary working branch

## Migration Steps

### Step 1: Create Alpha Branch
```bash
# Create alpha branch from main
git checkout main
git pull origin main
git checkout -b alpha
git push origin alpha

# Set alpha as default branch for integration
```

### Step 2: Update Branch Protection Rules
1. Navigate to repository Settings > Branches
2. Create branch protection rule for `alpha` branch:
   - Require pull request reviews before merging
   - Require status checks to pass before merging
   - Require branches to be up to date before merging
   - Include administrators in these restrictions

### Step 3: Delete Obsolete Branches
```bash
# Delete remote branches (requires admin permissions)
git push origin --delete Android-rebranding
git push origin --delete copilot/fix-109
git push origin --delete copilot/fix-114  
git push origin --delete copilot/fix-131
git push origin --delete copilot/fix-e6bf1298-409e-49b1-844b-92617a8ba059
```

### Step 4: Update Documentation
- ✅ `CONTRIBUTING.md` - Updated branch strategy section
- ✅ GitHub Actions workflows - Updated to use `alpha` instead of `develop`
- ✅ Created this migration guide

### Step 5: Team Communication
1. Notify all developers of the new branching strategy
2. Ensure all open PRs targeting `develop` are retargeted to `alpha`
3. Update any automated tools or scripts that reference `develop` branch

## Developer Workflow Changes

### Before (with develop branch)
```bash
git checkout develop
git pull origin develop  
git checkout -b feature/new-feature
# ... make changes ...
git push origin feature/new-feature
# Create PR targeting develop
```

### After (with alpha branch)
```bash  
git checkout alpha
git pull origin alpha
git checkout -b feature/new-feature
# ... make changes ...
git push origin feature/new-feature  
# Create PR targeting alpha
```

## Files Updated

The following files have been updated to reflect the new branching strategy:

1. **CONTRIBUTING.md**
   - Updated Branch Strategy section to use `alpha` instead of `develop`

2. **GitHub Actions Workflows**  
   - `android-build.yml` - Updated trigger branches
   - `Superlinter.yml` - Updated trigger branches
   - `ci-cd.yml` - Updated trigger branches for both push and pull_request events

3. **Documentation**
   - Created `BRANCH_CLEANUP_GUIDE.md` (this file)

## Verification Checklist

- [ ] Alpha branch created from main
- [ ] Branch protection rules applied to alpha
- [ ] Obsolete branches deleted  
- [ ] All open PRs retargeted from develop to alpha
- [ ] Team notified of changes
- [ ] Automated tools updated
- [ ] Documentation verified and distributed

## Rollback Plan

If issues arise with the new strategy:

1. **Immediate**: Create `develop` branch from `alpha` 
2. **Update workflows**: Revert GitHub Actions to use `develop`
3. **Communication**: Notify team of rollback
4. **Investigation**: Analyze issues and plan re-migration

## Contact

For questions or issues with this migration, please:
- Open an issue in the repository
- Contact the development team leads
- Reference this guide in any related discussions

---

*Last Updated: [Current Date]*
*Migration Status: In Progress*