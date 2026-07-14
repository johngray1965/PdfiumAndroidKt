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

if ! gh auth status &>/dev/null; then
  echo "GH_TOKEN not set or gh cli not authenticated. Please run 'gh auth login' or set GH_TOKEN." >&2 exit 1
fi

echo "🚀 Starting Release Process for $VERSION"

# 1. Extract Release Notes from CHANGELOG.md
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

# 2. Tag (Locally) FIRST so Gradle sees the version
# We delete the tag if deployment fails
echo "🏷️  Tagging release locally..."
git tag "$VERSION"
if [ $? -ne 0 ]; then
    echo "❌ Failed to create tag. Does it already exist?"
    rm "$NOTES_FILE"
    exit 1
fi

# 3. Run Deployment to Maven Central
echo "📦 Deploying to Maven Central..."
./deploy.sh
if [ $? -ne 0 ]; then
    echo "❌ Deployment failed. Deleting local tag..."
    git tag -d "$VERSION"
    rm "$NOTES_FILE"
    exit 1
fi

# 4. Push Tag
echo "⬆️  Pushing tag..."
git push origin "$VERSION"

# 5. Create the GitHub Release WITH the built AAR assets.
# The AARs are attached at creation time (positional args below) because GitHub
# immutable releases forbid modifying a release after it exists. Owning this here
# -- rather than a CI step reacting to the release event -- is what keeps the CI
# "Build & Test" workflow from failing on an already-published release.
echo "🐙 Creating GitHub Release (with AAR assets)..."

# Auto-detect pre-release based on hyphen (semver)
PRERELEASE_FLAG=""
if [[ "$VERSION" == *"-"* ]]; then
  echo "⚠️  Detected pre-release version. Marking as pre-release."
  PRERELEASE_FLAG="--prerelease"
fi

# Collect the release AARs produced by ./deploy.sh above (bash 3.2-safe: no
# globstar, which macOS /bin/bash lacks).
AAR_FILES=()
while IFS= read -r aar; do
  AAR_FILES+=("$aar")
done < <(find . -path '*/build/outputs/aar/*-release.aar')

if [ ${#AAR_FILES[@]} -eq 0 ]; then
  echo "⚠️  No release AARs found under */build/outputs/aar/ -- creating the release without assets."
else
  echo "📎 Attaching ${#AAR_FILES[@]} AAR(s):"
  printf '   - %s\n' "${AAR_FILES[@]}"
fi

gh release create "$VERSION" "${AAR_FILES[@]}" \
  --title "Release $VERSION" --notes-file "$NOTES_FILE" $PRERELEASE_FLAG

# Cleanup
rm "$NOTES_FILE"

echo "✅ Release $VERSION completed successfully!"
echo "   - Maven Central: Uploaded (Staging)"
echo "   - GitHub Release: Created (with ${#AAR_FILES[@]} AAR asset(s))"
