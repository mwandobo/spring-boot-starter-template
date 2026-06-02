#!/bin/bash
# ================================================
# Spring Boot Project Setup Script - Dynamic Version
# Usage: ./setup.sh <new.package.name> <MainClassName>
# Example: ./setup.sh com.mwalimubank.mbims MbimsApi
# ================================================

if [ $# -lt 2 ]; then
  echo "Usage: $0 <new.package.name> <MainClassName>"
  echo "Example: $0 com.mwalimubank.mbims MbimsApi"
  exit 1
fi

NEW_PACKAGE=$1
MAIN_CLASS=$2

# === DYNAMIC DETECTION OF OLD PACKAGE AND MAIN CLASS ===
echo "🔍 Detecting current package and main class..."

# Find the main class (prioritize @SpringBootApplication)
MAIN_CLASS_FILE=$(find src/main/java -name "*.java" -exec grep -lE '@SpringBootApplication|public static void main' {} + | head -n 1)

if [ -z "$MAIN_CLASS_FILE" ]; then
  echo "❌ Could not find main class. Make sure you have a class with @SpringBootApplication or main method."
  exit 1
fi

# Extract old main class name from filename
OLD_MAIN_CLASS=$(basename "$MAIN_CLASS_FILE" .java)

# Extract old package name from package declaration
OLD_PACKAGE=$(grep -m 1 '^package ' "$MAIN_CLASS_FILE" | sed 's/^package //' | sed 's/;$//' | tr -d ' ')

if [ -z "$OLD_PACKAGE" ]; then
  echo "❌ Could not detect package declaration."
  exit 1
fi

APP_NAME=$(echo "$MAIN_CLASS" | tr '[:upper:]' '[:lower:]')

echo "✅ Detected:"
echo "   Old Package : $OLD_PACKAGE"
echo "   Old Main Class : $OLD_MAIN_CLASS"
echo "   New Package : $NEW_PACKAGE"
echo "   New Main Class : $MAIN_CLASS"
echo "   App Name : $APP_NAME"
echo ""

# Continue with the rest of the script
OLD_PACKAGE_PATH=$(echo "$OLD_PACKAGE" | tr '.' '/')
PACKAGE_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')

echo "⚡ Setting up project..."

# 1. Update package name in all Java files
echo "🔄 Updating package declarations..."
find src -type f -name "*.java" -exec sed -i "s|$OLD_PACKAGE|$NEW_PACKAGE|g" {} +

# 2. Update main class name
echo "🔄 Updating main class references..."
find src -type f -name "*.java" -exec sed -i "s|\b$OLD_MAIN_CLASS\b|$MAIN_CLASS|g" {} +

# 3. Update Gradle files
echo "🔄 Updating Gradle files..."
sed -i "s|group = '.*'|group = '$NEW_PACKAGE'|g" build.gradle
sed -i "s|rootProject.name = '.*'|rootProject.name = '$MAIN_CLASS'|g" settings.gradle
sed -i "s|mainClass = '.*'|mainClass = '$NEW_PACKAGE.$MAIN_CLASS'|g" build.gradle 2>/dev/null

# 4. Restructure package folders
echo "🔄 Moving source to new package structure..."
mkdir -p "src/main/java/$PACKAGE_PATH"
mkdir -p "src/test/java/$PACKAGE_PATH"

cp -r "src/main/java/$OLD_PACKAGE_PATH/." "src/main/java/$PACKAGE_PATH/" 2>/dev/null
cp -r "src/test/java/$OLD_PACKAGE_PATH/." "src/test/java/$PACKAGE_PATH/" 2>/dev/null

# Remove old package
echo "🔄 Removing old package structure..."
rm -rf "src/main/java/$OLD_PACKAGE_PATH"
rm -rf "src/test/java/$OLD_PACKAGE_PATH"

# Clean empty directories
echo "🔄 Cleaning empty directories..."
find src/main/java -type d -empty -delete 2>/dev/null
find src/test/java -type d -empty -delete 2>/dev/null

# 5. Rename main class file
echo "🔄 Renaming main application class..."
find "src/main/java/$PACKAGE_PATH" -name "$OLD_MAIN_CLASS.java" -exec bash -c '
  mv "$0" "${0%/*}/'"$MAIN_CLASS"'.java"
' {} \;

# 6. Update application.properties
echo "🔄 Updating application name..."
if [ -f "src/main/resources/application.properties" ]; then
  sed -i "s|spring.application.name=.*|spring.application.name=$APP_NAME|g" src/main/resources/application.properties
fi

echo ""
echo "✅ Setup completed successfully! Project renamed."
echo ""
echo "Next steps:"
echo "  1. ./gradlew clean build"
echo "  2. ./gradlew bootRun"