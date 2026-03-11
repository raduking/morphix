#!/bin/sh

# Exit immediately if a command exits with a non-zero status
set -e

mvn deploy -Drelease=true

echo "Deployment completed successfully."
