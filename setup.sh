#!/bin/bash
# Setup script for Spring Boot Starter Template
# Usage: ./setup.sh com.mycompany.myapp MyApp

if [ $# -lt 2 ]; then
  echo "Usage: $0 <base.package.name> <AppName>"
  echo "Example: $0 com.mycompany.myapp MyApp"
  exit 1
fi

NEW_PACKAGE=$1
APP_NAME=$2

# Old defaults (update if you use something different in your starter)
OLD_PACKAGE="com.example.super_start_web_api"
OLD_APP_NAME="SuperStartWebApiApplication"

# Convert package name (e.g. com.mycompany.myapp → com/mycompany/myapp)
PACKAGE_PATH=$(echo "$NEW_PACKAGE" | tr '.' '/')
OLD_PACKAGE_PATH=$(echo "$OLD_PACKAGE" | tr '.' '/')

echo "⚡ Setting up project with:"
echo "   Package: $NEW_PACKAGE"
echo "   App Name: $APP_NAME"
echo ""

# Replace package name in Java source files
find src -type f -name "*.java" -exec sed -i '' "s|$OLD_PACKAGE|$NEW_PACKAGE|g" {} +

# Replace package name in Kotlin files (if any)
find src -type f -name "*.kt" -exec sed -i '' "s|$OLD_PACKAGE|$NEW_PACKAGE|g" {} + 2>/dev/null

## Replace groupId & artifactId in pom.xml
#echo "🔄 Updating pom.xml..."
#sed -i "s|<groupId>.*</groupId>|<groupId>$NEW_PACKAGE</groupId>|g" pom.xml
#sed -i "s|<artifactId>.*</artifactId>|<artifactId>$APP_NAME</artifactId>|g" pom.xml

# Update group in build.gradle
sed -i '' "s|group = '.*'|group = '$NEW_PACKAGE'|g" build.gradle

# Update rootProject.name in settings.gradle (or build.gradle if defined there)
sed -i '' "s|rootProject.name = '.*'|rootProject.name = '$APP_NAME'|g" settings.gradle 2>/dev/null

# Rename main application class
echo "🔄 Renaming main application class..."
find src -type f -name "${OLD_APP_NAME}.java" -exec bash -c 'mv "$0" "${0%/*}/'$APP_NAME'Application.java"' {} \;

# Move source folders to new package path
echo "🔄 Restructuring package folders..."
mkdir -p src/main/java/$PACKAGE_PATH
mkdir -p src/test/java/$PACKAGE_PATH

rsync -a --remove-source-files src/main/java/$OLD_PACKAGE_PATH/ src/main/java/$PACKAGE_PATH/
rsync -a --remove-source-files src/test/java/$OLD_PACKAGE_PATH/ src/test/java/$PACKAGE_PATH/

rm -rf src/main/java/$OLD_PACKAGE_PATH
rm -rf src/test/java/$OLD_PACKAGE_PATH

echo "✅ Setup complete!"
echo "Next steps:"
echo "1. Open pom.xml and adjust <name>, <description>, and <version>."
echo "2. Run: mvn clean install"
echo "3. Start your app: mvn spring-boot:run"
