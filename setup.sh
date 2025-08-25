#!/bin/bash
# Setup script for Spring Boot Starter Template (Gradle + macOS compatible)
# Usage: ./setup.sh com.mycompany.myapp StarterApplication

if [ $# -lt 2 ]; then
  echo "Usage: $0 <base.package.name> <MainClassName>"
  echo "Example: $0 com.mycompany.myapp StarterApplication"
  exit 1
fi

NEW_PACKAGE=$1
MAIN_CLASS_NAME=$2

# Old defaults
OLD_PACKAGE="com.example.super_start_web_api"
OLD_MAIN_CLASS_NAME="SuperStartWebApiApplication"

# Convert package names to folder paths
PACKAGE_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')
OLD_PACKAGE_PATH=$(echo "$OLD_PACKAGE" | tr '.' '/')

# Convert main class name to lowercase for application name
APP_NAME_LOWERCASE=$(echo "$MAIN_CLASS_NAME" | tr '[:upper:]' '[:lower:]')

echo "⚡ Setting up project with:"
echo "   Package: $NEW_PACKAGE"
echo "   Main Class: $MAIN_CLASS_NAME"
echo "   App Name: $APP_NAME_LOWERCASE"
echo ""

# Replace package name in Java source files
echo "🔄 Updating package names in Java..."
find src -type f -name "*.java" -exec sed -i '' "s|$OLD_PACKAGE|$NEW_PACKAGE|g" {} +

# Replace package name in Kotlin files (if any)
find src -type f -name "*.kt" -exec sed -i '' "s|$OLD_PACKAGE|$NEW_PACKAGE|g" {} + 2>/dev/null

# Update main class name in Java files
echo "🔄 Updating main class name..."
find src -type f -name "*.java" -exec sed -i '' "s|$OLD_MAIN_CLASS_NAME|$MAIN_CLASS_NAME|g" {} +

# Update Gradle metadata
echo "🔄 Updating build.gradle and settings.gradle..."
sed -i '' "s|group = '.*'|group = '$NEW_PACKAGE'|g" build.gradle
sed -i '' "s|rootProject.name = '.*'|rootProject.name = '$MAIN_CLASS_NAME'|g" settings.gradle 2>/dev/null

# Update main class in build.gradle if it's specified there
sed -i '' "s|mainClass = '.*'|mainClass = '$NEW_PACKAGE.$MAIN_CLASS_NAME'|g" build.gradle 2>/dev/null

# Move source folders to new package path
echo "🔄 Restructuring package folders..."
mkdir -p src/main/java/$PACKAGE_PATH
mkdir -p src/test/java/$PACKAGE_PATH

rsync -a --remove-source-files src/main/java/$OLD_PACKAGE_PATH/ src/main/java/$PACKAGE_PATH/
rsync -a --remove-source-files src/test/java/$OLD_PACKAGE_PATH/ src/test/java/$PACKAGE_PATH/

rm -rf src/main/java/$OLD_PACKAGE_PATH
rm -rf src/test/java/$OLD_PACKAGE_PATH

# Rename main application class to the exact name you want
echo "🔄 Renaming main application class..."
find src/main/java/$PACKAGE_PATH -type f -name "${OLD_MAIN_CLASS_NAME}.java" -exec bash -c 'mv "$0" "${0%/*}/'$MAIN_CLASS_NAME'.java"' {} \;

# Update application properties with lowercase app name
echo "🔄 Updating application.properties..."
if [ -f "src/main/resources/application.properties" ]; then
    sed -i '' "s|spring.application.name=.*|spring.application.name=$APP_NAME_LOWERCASE|g" src/main/resources/application.properties
fi

echo "✅ Setup complete!"
echo "Next steps:"
echo "1. Open build.gradle and settings.gradle to adjust project info if needed."
echo "2. Run: ./gradlew clean build"
echo "3. Start your app: ./gradlew bootRun"