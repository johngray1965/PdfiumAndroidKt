#!/bin/bash

# release.sh - Automate the release process
# Usage: ./release.sh 2.0.0-beta27

VERSION=$1

if [ -z "$VERSION" ]; then
  echo "Usage: ./release.sh <version>"
  echo "Example: ./release.sh 2.0.0-beta27"
  exit 1
fi

# Ensure GitHub CLI is installed
if ! command -v gh &> /dev/null; then
    echo "Error: GitHub CLI ('gh') is not installed."
    echo "Install it via 'brew install gh' or see https://cli.github.com/"
    exit 1
fi

echo "🚀 Starting Release Process for $VERSION"

# 1. Extract Release Notes from CHANGELOG.md
# This simple parser looks for "## [VERSION]" and reads until the next "## ["
NOTES_FILE="release_notes_temp.txt"
awk "/^## \[$VERSION\]/{flag=1; next} /^## \[/{flag=0} flag" CHANGELOG.md | sed '/^$/d' > "$NOTES_FILE"

if [ ! -s "$NOTES_FILE" ]; then
  echo "❌ Error: No release notes found for version $VERSION in CHANGELOG.md."
  echo "Please update CHANGELOG.md first."
  rm "$NOTES_FILE"
  exit 1
fi

echo "📋 Release Notes extracted:"
echo "---------------------------------------------------"
cat "$NOTES_FILE"
echo "---------------------------------------------------"
read -p "Press Enter to continue or Ctrl+C to cancel..."

# 2. Run Deployment to Maven Central
echo "📦 Deploying to Maven Central..."
./deploy.sh
if [ $? -ne 0 ]; then
    echo "❌ Deployment failed."
    rm "$NOTES_FILE"
    exit 1
fi

# 3. Git Tag and Push
echo "🏷️  Tagging release..."
git tag "$VERSION"
if [ $? -ne 0 ]; then
    echo "❌ Failed to create tag. Does it already exist?"
    rm "$NOTES_FILE"
    exit 1
fi

echo "⬆️  Pushing tag..."
git push origin "$VERSION"

# 4. Create GitHub Release
echo "🐙 Creating GitHub Release..."

# Auto-detect pre-release based on hyphen (semver)
PRERELEASE_FLAG=""
if [[ "$VERSION" == *"-"* ]]; then
  echo "⚠️  Detected pre-release version. Marking as pre-release."
  PRERELEASE_FLAG="--prerelease"
fi

gh release create "$VERSION" --title "Release $VERSION" --notes-file "$NOTES_FILE" $PRERELEASE_FLAG

# Cleanup
rm "$NOTES_FILE"

echo "✅ Release $VERSION completed successfully!"
echo "   - Maven Central: Uploaded (Staging)"
echo "   - GitHub Release: Created"
echo "   - CI: Triggered (will publish to GitHub Packages and upload AARs)"
