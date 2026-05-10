#!/bin/bash

# Script to bump version in version.properties based on GitHub tags
PROPS_FILE="version.properties"
BUMP_TYPE=${1:-"patch"} # Default to patch if no argument provided

# Ensure we are in the root of the project
cd "$(dirname "$0")/.."

# Fetch tags to ensure we have the latest
git fetch --tags

# Try to get the latest tag from GitHub (using git)
LATEST_TAG=$(git describe --tags --abbrev=0 2>/dev/null)

if [ -z "$LATEST_TAG" ]; then
    echo "No tags found, using local version.properties as fallback."
    VERSION_NAME=$(grep 'versionName=' "$PROPS_FILE" | cut -d'=' -f2)
    VERSION_CODE=$(grep 'versionCode=' "$PROPS_FILE" | cut -d'=' -f2)
else
    echo "Latest tag found: $LATEST_TAG"
    VERSION_NAME=${LATEST_TAG#v} # remove 'v' prefix
    VERSION_CODE=$(grep 'versionCode=' "$PROPS_FILE" | cut -d'=' -f2)
fi

echo "Current Version: $VERSION_NAME ($VERSION_CODE)"

# Increment version code
NEW_VERSION_CODE=$((VERSION_CODE + 1))

# Split version name
IFS='.' read -ra ADDR <<< "$VERSION_NAME"
MAJOR=${ADDR[0]:-1}
MINOR=${ADDR[1]:-0}
PATCH=${ADDR[2]:-0}

case $BUMP_TYPE in
    major)
        MAJOR=$((MAJOR + 1))
        MINOR=0
        PATCH=0
        ;;
    minor)
        MINOR=$((MINOR + 1))
        PATCH=0
        ;;
    patch)
        PATCH=$((PATCH + 1))
        ;;
    *)
        echo "Unknown bump type: $BUMP_TYPE. Using patch."
        PATCH=$((PATCH + 1))
        ;;
esac

NEW_VERSION_NAME="$MAJOR.$MINOR.$PATCH"

# Update file
echo "versionCode=$NEW_VERSION_CODE" > "$PROPS_FILE"
echo "versionName=$NEW_VERSION_NAME" >> "$PROPS_FILE"

echo "New Version: $NEW_VERSION_NAME ($NEW_VERSION_CODE)"
