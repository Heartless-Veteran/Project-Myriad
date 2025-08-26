module.exports = {
  env: {
    node: true,
    es2021: true,
  },
  extends: [
    'eslint:recommended',
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
  },
  rules: {
    // Basic rules for code analysis script
    'no-console': 'off', // We use console in analysis script
    'no-unused-vars': 'warn',
    'no-undef': 'error',
  },
};