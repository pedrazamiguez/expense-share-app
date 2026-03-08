#!/bin/bash
# Updates the GOOGLE_SERVICES_JSON_BASE64 repo secret on GitHub
# from the local app/google-services.json file.
#
# Prerequisites: GitHub CLI (gh) installed and authenticated.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SOURCE_FILE="$PROJECT_ROOT/app/google-services.json"
TEMP_FILE="$PROJECT_ROOT/google-services-base64.txt"

if [ ! -f "$SOURCE_FILE" ]; then
  echo "❌ File not found: $SOURCE_FILE"
  exit 1
fi

if ! command -v gh &> /dev/null; then
  echo "❌ GitHub CLI (gh) is not installed. Install it from https://cli.github.com/"
  exit 1
fi

echo "🔐 Encoding google-services.json to base64..."
base64 -i "$SOURCE_FILE" -o "$TEMP_FILE"

echo "☁️  Updating GOOGLE_SERVICES_JSON_BASE64 repo secret..."
gh secret set GOOGLE_SERVICES_JSON_BASE64 < "$TEMP_FILE"

echo "🧹 Cleaning up temporary file..."
rm -f "$TEMP_FILE"

echo "✅ Secret updated successfully."

