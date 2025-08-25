#!/bin/bash
set -e

echo "🚀 Starting development environment for Project Myriad..."

# Start Metro bundler in the background
echo "Starting Metro bundler in the background... Logs will be saved to metro.log"
npm start > metro.log 2>&1 &
METRO_PID=$!

# Add a small delay to allow Metro to start up
sleep 8

echo "✅ Metro bundler started with PID: $METRO_PID"
echo "You can view its logs by running: tail -f metro.log"
echo ""

# Run the Android app
echo "📱 Starting the Android application..."
echo "This can take a while, especially on the first run."
npm run android

echo ""
echo "✅ Android app process finished."
echo "To stop the Metro bundler, run the following command:"
echo "  kill $METRO_PID"
echo ""
echo "👋 Happy coding!"
