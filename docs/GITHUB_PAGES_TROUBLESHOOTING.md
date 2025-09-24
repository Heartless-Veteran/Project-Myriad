# GitHub Pages Troubleshooting Guide

## 🚨 Common Issues and Fixes

### Issue 1: Jekyll Build Failures

**Symptoms:**
- GitHub Pages workflow fails during "Build with Jekyll" step
- Site shows "Page not found" or doesn't update

**Fixes:**
1. **Check Gemfile compatibility:**
   ```ruby
   source "https://rubygems.org"
   gem "github-pages", "~> 231", group: :jekyll_plugins
   gem "webrick", "~> 1.8" # Required for Ruby 3.0+
   ```

2. **Verify _config.yml syntax:**
   - Ensure proper YAML formatting
   - Check that all referenced files exist
   - Verify baseurl and url settings

3. **Test locally:**
   ```bash
   ./scripts/test-jekyll-build.sh
   ```

### Issue 2: Broken Links

**Symptoms:**
- Links return 404 errors
- Navigation doesn't work properly

**Fixes:**
1. **Use relative paths without .html extension:**
   ```html
   <!-- ❌ Wrong -->
   <a href="docs/ARCHITECTURE.html">Architecture</a>
   
   <!-- ✅ Correct -->
   <a href="docs/ARCHITECTURE">Architecture</a>
   ```

2. **Ensure target files exist:**
   - Check that `docs/ARCHITECTURE.md` exists
   - Verify file names match exactly (case-sensitive)

### Issue 3: API Documentation Missing

**Symptoms:**
- `/api/` path returns 404
- No Kotlin documentation visible

**Fixes:**
1. **Check Dokka generation:**
   ```bash
   ./gradlew app:dokkaGenerateModuleHtml
   ```

2. **Verify workflow step:**
   - Ensure Dokka step completes without errors
   - Check that files are copied to `api/` directory

### Issue 4: Workflow Permission Errors

**Symptoms:**
- "Deploy to GitHub Pages" step fails
- Permission denied errors

**Fixes:**
1. **Check repository settings:**
   - Go to Settings → Pages
   - Set Source to "GitHub Actions"
   - Ensure workflow has proper permissions

2. **Verify workflow permissions:**
   ```yaml
   permissions:
     contents: read
     pages: write
     id-token: write
   ```

## 🛠️ Manual Fixes Applied

The following fixes have been implemented:

### 1. Fixed Broken Links ✅
- Removed `.html` extensions from internal links
- Updated both `index.html` and `docs/index.html`
- Fixed navigation paths throughout the site

### 2. Improved Jekyll Configuration ✅
- Updated `_config.yml` with better exclusions
- Added vendor directory to exclude list
- Enhanced Kramdown settings for better markdown processing

### 3. Enhanced Workflow ✅
- Added verbose logging for better debugging
- Improved error handling in build steps
- Added site structure validation

### 4. Updated Dependencies ✅
- Pinned GitHub Pages gem to compatible version
- Added webrick for Ruby 3.0+ compatibility
- Specified plugin versions for stability

### 5. Added Testing Tools ✅
- Created `scripts/test-jekyll-build.sh` for local testing
- Added site validation steps
- Improved error reporting

## 🔍 Verification Steps

To verify the fixes are working:

1. **Check workflow logs:**
   - Go to Actions tab in GitHub repository
   - Look for "Deploy to GitHub Pages" workflow
   - Verify all steps complete successfully

2. **Test the live site:**
   - Visit: https://heartless-veteran.github.io/Project-Myriad
   - Click through navigation links
   - Verify API documentation loads (if available)

3. **Local testing:**
   ```bash
   cd Project-Myriad
   ./scripts/test-jekyll-build.sh
   bundle exec jekyll serve
   ```
   Then visit http://localhost:4000

## 🚀 Expected Results

After these fixes:
- ✅ GitHub Pages should build successfully
- ✅ All internal links should work
- ✅ Site should be accessible at the GitHub Pages URL
- ✅ Navigation should work smoothly
- ✅ API documentation should be generated (when available)

## 📞 If Issues Persist

If GitHub Pages still doesn't work after these fixes:

1. **Check GitHub status:**
   - Visit https://www.githubstatus.com/
   - Look for Pages service issues

2. **Repository permissions:**
   - Ensure repository has Pages enabled
   - Check that Actions have necessary permissions
   - Verify no branch protection rules block deployment

3. **Contact support:**
   - Repository owner can contact GitHub Support
   - Provide workflow run logs for diagnosis

---

*This troubleshooting guide covers the most common GitHub Pages issues for Jekyll sites.*