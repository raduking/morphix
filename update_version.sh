#!/bin/sh

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <new-version>"
    exit 1
fi

NEW_VERSION=$1
OLD_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

echo "New version: $NEW_VERSION"
echo "Old version: $OLD_VERSION"

if [ "$NEW_VERSION" = "$OLD_VERSION" ]; then
    echo "Nothing to update."
    exit 1
fi

echo "Updating project version to $NEW_VERSION in pom.xml files..."

mvn versions:set -DnewVersion=$NEW_VERSION
mvn versions:update-child-modules
mvn versions:commit

FILES=(
    "README.md"
)

# escape slashes and ampersands for sed
ESCAPED_OLD=$(printf '%s\n' "$OLD_VERSION" | sed -e 's/[\/&]/\\&/g')
ESCAPED_NEW=$(printf '%s\n' "$NEW_VERSION" | sed -e 's/[\/&]/\\&/g')

echo "Updating version references in other files..."
echo "Replacing $ESCAPED_OLD with $ESCAPED_NEW"

for FILE in "${FILES[@]}"; do
    if [ -f "$FILE" ]; then
        echo "Processing $FILE..."

        # check if we're on macOS or Linux
        if [[ "$(uname)" == "Darwin" ]]; then
            # we are on macOS/BSD 
            # sed requires an empty string for in-place without backup
            sed -i '' "s/$ESCAPED_OLD/$ESCAPED_NEW/g" "$FILE"
        else
            # we are on Linux/GNU
            # sed in-place without backup
            sed -i "s/$ESCAPED_OLD/$ESCAPED_NEW/g" "$FILE"
        fi

        echo "  Updated $FILE"
    else
        echo "  Warning: $FILE not found"
    fi
done

echo "Version update complete."

exit 0
