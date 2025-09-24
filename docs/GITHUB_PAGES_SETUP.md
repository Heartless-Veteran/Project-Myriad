# GitHub Pages Setup Guide

This document explains the GitHub Pages configuration for Project Myriad's documentation site.

## 🌐 Overview

Project Myriad uses GitHub Pages to host comprehensive documentation including:
- **Markdown Documentation** - All files from the `docs/` folder
- **API Documentation** - Kotlin API reference generated with Dokka
- **Root Documentation** - README, ARCHITECTURE, CONTRIBUTING, etc.
- **Interactive Navigation** - Organized landing page and search functionality

**Live Site**: https://heartless-veteran.github.io/Project-Myriad

## 🏗️ Architecture

### Files Structure
```
Project-Myriad/
├── .github/workflows/pages.yml    # GitHub Pages deployment workflow
├── _config.yml                    # Jekyll configuration
├── index.html                     # Main landing page
├── docs/
│   ├── index.html                 # Documentation section index
│   └── *.md                       # All documentation files
└── [root documentation files]     # README.md, ARCHITECTURE.md, etc.
```

### Deployment Process
1. **Trigger**: Push to `main` branch or manual dispatch
2. **Build Phase**:
   - Generate Dokka API documentation
   - Update README statistics
   - Prepare site content in `_site/` directory
3. **Deploy Phase**:
   - Upload to GitHub Pages
   - Site becomes available at the GitHub Pages URL

## 🔧 Configuration Details

### Jekyll Configuration (`_config.yml`)
- **Theme**: Minima (GitHub Pages compatible)
- **Markdown**: Kramdown with GitHub Flavored Markdown
- **Plugins**: Feed, Sitemap, SEO tag
- **Collections**: Docs collection for enhanced navigation

### Workflow Configuration (`.github/workflows/pages.yml`)
- **JDK**: Java 17 (Temurin distribution)
- **Gradle**: Latest with caching enabled
- **Node.js**: v18 for README stats updates
- **Permissions**: `contents: read`, `pages: write`, `id-token: write`

## 🚀 Automatic Updates

The documentation site is automatically updated when:
- Code changes are pushed to the main branch
- New documentation files are added to `docs/`
- API changes are made (reflected in Dokka generation)
- Project statistics change (Kotlin files, dependencies, etc.)

## 🛠️ Manual Deployment

To trigger a manual deployment:
1. Go to the repository's Actions tab
2. Select the "Deploy to GitHub Pages" workflow
3. Click "Run workflow" and select the main branch

## 📋 Repository Settings

### Required Settings
1. **Pages Source**: GitHub Actions (configured in repository settings)
2. **Branch**: Main branch (deployment source)
3. **Environment**: `github-pages` environment (automatically created)

### Permissions
The workflow requires these permissions (automatically configured):
- `contents: read` - Access repository content
- `pages: write` - Deploy to GitHub Pages
- `id-token: write` - Authentication for deployment

## 🔍 Troubleshooting

### Common Issues

**Build Failures:**
- Check Java/Gradle setup in workflow logs
- Verify Dokka can generate documentation locally: `./gradlew app:dokkaGenerateModuleHtml`
- Ensure all markdown files have valid syntax

**Deployment Failures:**
- Verify GitHub Pages is enabled in repository settings
- Check repository permissions for GitHub Actions
- Ensure the `github-pages` environment exists

**Missing API Documentation:**
- Check if Dokka generation completed successfully
- Verify the correct path: `app/build/dokka-module/html/module/`
- Check for Kotlin compilation errors

### Testing Locally

To test the site preparation process:
```bash
# Generate Dokka documentation
./gradlew app:dokkaGenerateModuleHtml

# Test site preparation (creates test site in /tmp/test-site)
./scripts/test-pages-build.sh  # If created for testing
```

## 📈 Benefits

- **Centralized Documentation** - Single location for all project information
- **Automatic Updates** - Documentation stays current with code changes
- **Professional Presentation** - Clean, organized interface for users
- **Search Functionality** - Jekyll provides built-in search capabilities
- **Mobile Responsive** - Works well on all device sizes
- **SEO Optimized** - Proper meta tags and sitemap generation

## 🔮 Future Enhancements

Potential improvements for the documentation site:
- **Custom Theme** - Brand-specific styling
- **Advanced Search** - Full-text search with filters
- **Interactive Examples** - Code samples with live previews
- **Multi-language Support** - Documentation in multiple languages
- **Changelog Integration** - Automatic changelog generation from releases

---

*This setup ensures Project Myriad maintains professional, up-to-date documentation accessible to all users and contributors.*