#!/bin/sh

# Exit immediately if a command exits with a non-zero status
set -e

# Set the Maven binary folder, defaulting to the directory of the 'mvn' command if not already set
MAVEN_BIN_FOLDER="${MAVEN_BIN_FOLDER:-$(dirname "$(which mvn)")}"

"$MAVEN_BIN_FOLDER/mvn" deploy -Drelease=true

echo "Deployment completed successfully."
