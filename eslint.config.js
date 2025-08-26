// ESLint v9 configuration for Project Myriad
export default [
  {
    files: ["**/*.js", "**/*.mjs"],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
      globals: {
        console: "readonly",
        process: "readonly",
        __dirname: "readonly",
        __filename: "readonly",
        Buffer: "readonly",
        global: "readonly",
        module: "readonly",
        require: "readonly"
      }
    },
    rules: {
      // Basic rules for analysis scripts and build tools
      "no-console": "off", // We use console in analysis scripts
      "no-unused-vars": ["warn", { "argsIgnorePattern": "^_", "varsIgnorePattern": "^_" }],
      "no-undef": "error",
      "semi": ["error", "always"],
      "quotes": ["error", "double"],
      "indent": ["error", 2],
      "no-trailing-spaces": "error",
      "eol-last": "error"
    }
  },
  {
    // Node.js specific files
    files: ["scripts/**/*.js", "*.config.js"],
    languageOptions: {
      globals: {
        process: "readonly",
        __dirname: "readonly",
        __filename: "readonly",
        Buffer: "readonly",
        require: "readonly",
        module: "readonly",
        exports: "readonly"
      }
    }
  }
];
