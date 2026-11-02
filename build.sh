#!/bin/bash
# Build script for Android RAT

echo "🔨 Building Android RAT..."

# Check for Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install JDK 11+"
    exit 1
fi

# Check for Gradle
if ! command -v gradle &> /dev/null; then
    echo "⚠️  Gradle not found. Using Gradle wrapper..."
fi

# Build
cd "$(dirname "$0")"

if [ -f gradlew ]; then
    ./gradlew assembleRelease
else
    gradle assembleRelease
fi

# Find APK
APK_PATH=$(find . -name "*.apk" -path "*/release/*" 2>/dev/null | head -1)

if [ -n "$APK_PATH" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📦 APK location: $APK_PATH"
    
    # Copy to root
    cp "$APK_PATH" ./android-rat.apk
    echo "📦 Copied to: ./android-rat.apk"
else
    echo "❌ APK not found. Check build errors above."
fi
