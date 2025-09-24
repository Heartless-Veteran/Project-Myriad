# Security Configuration Guide

## ⚠️ CRITICAL: Keystore Security

**NEVER commit keystore files to version control!** This repository has been cleaned of a previously committed keystore file.

### Setting up Secure Signing

1. **Generate a keystore file**:
   ```bash
   keytool -genkey -v -keystore app/myriad-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias myriad-key-alias
   ```

2. **Create keystore.properties**:
   ```bash
   cp app/keystore.properties.example app/keystore.properties
   # Edit the file and fill in your actual passwords
   ```

3. **VERIFY .gitignore excludes**:
   ```
   *.jks
   *.keystore  
   *.p12
   keystore.properties
   ```

### CI/CD Environment Variables

For GitHub Actions, set these secrets:
- `KEYSTORE_FILE` (base64 encoded keystore)
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

## Security Features Enabled

### 🛡️ Code Protection
- **ProGuard/R8 Obfuscation**: Advanced string and resource obfuscation
- **Optimization Passes**: 5 passes for maximum protection
- **Anti-Reflection**: Protected against reflection attacks
- **Debug Protection**: Debugging disabled in release builds

### 🌐 Network Security
- **HTTPS Only**: All traffic must use HTTPS
- **Certificate Pinning**: Structure ready for API-specific pinning
- **Cleartext Traffic Disabled**: No unencrypted connections

### 📁 File Security
- **FileProvider**: Secure file sharing mechanism
- **Backup Disabled**: App data not included in backups
- **Secure Paths**: Controlled file access permissions

## Running Security Analysis

```bash
# Run comprehensive security analysis
node scripts/security-analyzer.js

# Run QA testing with security checks
bash scripts/qa-final-testing.sh
```

## Security Checklist

- [ ] Keystore files are excluded from version control
- [ ] keystore.properties is configured (not committed)
- [ ] ProGuard rules are enabled for release builds
- [ ] Network security config is properly configured
- [ ] No hardcoded secrets in source code
- [ ] FileProvider is configured for file sharing
- [ ] Debug flags are disabled in release builds

## Emergency Response

If sensitive data is accidentally committed:
1. **Immediately revoke** any exposed credentials
2. **Force push** to remove from history (if possible)
3. **Generate new keystore** and certificates
4. **Update all credentials** that may have been exposed
5. **Review and update** .gitignore patterns

---

**Last Updated**: December 2024
**Security Review**: Latest security analysis shows no critical issues