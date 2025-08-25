#!/bin/bash
set -e

# Get the directory of the script
SCRIPT_DIR=$(dirname "$0")

echo "🚀 Starting Project Myriad setup..."

# Run the environment check script
"$SCRIPT_DIR/check_env.sh"

# If the check passes, proceed with installation
echo "✅ Environment check passed. Installing dependencies..."
echo "This might take a few minutes. Please do not cancel."

# Install npm dependencies using the required flag
npm install --legacy-peer-deps

echo "✅ Dependencies installed successfully!"
echo ""
echo "🎉 Setup complete!"
echo ""
echo "To run the application for development, you can use the following script:"
echo "  ./scripts/run_dev.sh"
echo ""
echo "Alternatively, you can run the commands manually:"
echo "1. Start the Metro bundler in a terminal:"
echo "   npm start"
echo "2. In a second terminal, run the Android app:"
echo "   npm run android"
