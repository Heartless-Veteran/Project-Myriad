import js from '@eslint/js';
import nodePlugin from 'eslint-plugin-n';

export default [
  js.configs.recommended,
  {
    files: ['**/*.js', '**/*.mjs'],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: 'module',
      globals: {
        console: 'readonly',
        process: 'readonly',
        Buffer: 'readonly',
        __dirname: 'readonly',
        __filename: 'readonly',
        exports: 'writable',
        global: 'readonly',
        module: 'readonly',
        require: 'readonly'
      }
    },
    plugins: {
      n: nodePlugin
    },
    rules: {
      // Copy rules from .eslintrc.js
      'indent': ['error', 2],
      'linebreak-style': ['error', 'unix'],
      'quotes': ['error', 'single'],
      'semi': ['error', 'always'],
      'no-console': 'warn',
      'no-unused-vars': 'warn',
      'eqeqeq': 'error',
      'curly': 'error',
      'no-trailing-spaces': 'error',
      'comma-dangle': ['error', 'never'],
      'object-curly-spacing': ['error', 'always'],
      'array-bracket-spacing': ['error', 'never'],
      'n/no-missing-import': 'error',
      'n/no-unpublished-import': 'error'
    }
  },
  {
    // Ignore patterns
    ignores: [
      'node_modules/**',
      'build/**',
      'dist/**',
      '.gradle/**',
      'app/build/**',
      '.idea/**',
      '*.min.js'
    ]
  }
];