# Security Policy

## Supported Versions

The following versions of Project Myriad are currently being supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

**🔒 We take security seriously.** If you discover a security vulnerability in Project Myriad, please follow responsible disclosure practices.

### How to Report

1. **DO NOT** create a public GitHub issue for security vulnerabilities
2. **Email us directly** at: security@heartlessveteran.com
3. **Include the following information:**
   - Description of the vulnerability
   - Steps to reproduce the issue
   - Potential impact assessment
   - Suggested fix (if available)
   - Your contact information

### What to Expect

- **Acknowledgment**: We will acknowledge receipt within 48 hours
- **Initial Assessment**: We will provide an initial assessment within 5 business days
- **Regular Updates**: We will provide updates every 5-7 days until resolution
- **Resolution Timeline**: Critical vulnerabilities will be addressed within 30 days

### Security Scope

This security policy covers:
- **Mobile Application**: Android APK security vulnerabilities
- **Data Storage**: Local database and file storage security
- **Network Communications**: HTTPS and certificate pinning issues
- **Build Process**: Supply chain and dependency vulnerabilities

### Out of Scope

The following are generally considered out of scope:
- Social engineering attacks
- Physical access attacks
- DoS attacks on third-party services
- Issues in third-party dependencies (report to upstream)
- Self-XSS or issues requiring user to paste code into DevTools

### Recognition

We believe in recognizing security researchers who help improve our security:
- **Hall of Fame**: Public recognition (with permission)
- **Coordinated Disclosure**: We will work with you on disclosure timing
- **Credit**: You will be credited in release notes (unless you prefer anonymity)

## Security Features

Project Myriad implements several security measures:

### Network Security
- **HTTPS Enforcement**: All network traffic must use HTTPS
- **Certificate Pinning**: Pinned certificates for critical APIs (configurable)
- **Network Security Config**: Android Network Security Configuration enforced
- **Cleartext Traffic Prevention**: No unencrypted HTTP connections allowed

### Data Protection
- **No Backup**: Application data is not included in device backups
- **Secure Storage**: Sensitive data is encrypted at rest
- **API Key Protection**: API keys are validated and sanitized
- **File Provider**: Secure file sharing using Android FileProvider

### Application Security
- **Code Obfuscation**: Release builds use ProGuard/R8 obfuscation with advanced features
- **String Encryption**: String and resource obfuscation enabled
- **Debug Protection**: Security features adjusted based on build type, debugging disabled in release
- **Input Validation**: All user inputs are sanitized
- **Anti-Tampering**: Multiple optimization passes and advanced obfuscation

### Build Security
- **Dependency Scanning**: Automated vulnerability scanning of dependencies
- **Code Analysis**: Static analysis with security-focused rules
- **Signed Releases**: All releases are cryptographically signed
- **Keystore Protection**: Keystore files are never committed to version control
- **Secure Configuration**: Uses keystore.properties for secure credential management

## Security Best Practices for Contributors

When contributing to Project Myriad:

1. **Never commit secrets** - Use keystore.properties for signing credentials, local.properties for API keys
2. **Keystore Security** - NEVER commit .jks, .keystore, .p12 files to version control
3. **Use secure configuration** - Follow keystore.properties.example for signing setup
4. **Validate all inputs** - Sanitize user inputs and API responses
5. **Use secure APIs** - Prefer Android Keystore and secure random generators
6. **Follow least privilege** - Request minimal permissions necessary
7. **Handle errors securely** - Don't expose sensitive information in error messages

## Security Testing

We encourage security testing of Project Myriad:
- **Static Analysis**: Use detekt security rules
- **Dependency Checks**: Run OWASP dependency check
- **Network Testing**: Verify certificate pinning and HTTPS enforcement

---

**Contact**: For non-security issues, please use GitHub issues. For security concerns, email security@heartlessveteran.com
