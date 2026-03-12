#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────
# generate-screenshot-summary.sh
#
# Generates an HTML screenshot gallery and writes it to
# $GITHUB_STEP_SUMMARY so screenshots appear directly in the
# GitHub Actions workflow summary page.
#
# Usage:
#   ./scripts/generate-screenshot-summary.sh <screenshots-dir>
#
# The script encodes each PNG as a base64 data-URI thumbnail.
# If the total payload would exceed GitHub's ~1 MB summary limit,
# it truncates and adds a note to download the full artifact.
# ─────────────────────────────────────────────────────────────────────
set -euo pipefail

SCREENSHOTS_DIR="${1:?Usage: $0 <screenshots-dir>}"
SUMMARY_FILE="${GITHUB_STEP_SUMMARY:-/dev/stdout}"
# Reserve space for closing </table>, truncation warning, and footer (~500 bytes)
FOOTER_RESERVE=500
MAX_SUMMARY_BYTES=$(( 950000 - FOOTER_RESERVE ))  # ~950 KB safety margin under 1 MB limit

if [ ! -d "$SCREENSHOTS_DIR" ]; then
  echo "⚠️ Screenshots directory not found: $SCREENSHOTS_DIR"
  echo "### 📸 Test Screenshots" >> "$SUMMARY_FILE"
  echo "_No screenshots were captured._" >> "$SUMMARY_FILE"
  exit 0
fi

# Collect PNGs sorted alphabetically
mapfile -t SCREENSHOTS < <(find "$SCREENSHOTS_DIR" -name '*.png' -type f | sort)

if [ ${#SCREENSHOTS[@]} -eq 0 ]; then
  echo "### 📸 Test Screenshots" >> "$SUMMARY_FILE"
  echo "_No screenshots were captured._" >> "$SUMMARY_FILE"
  exit 0
fi

echo "Found ${#SCREENSHOTS[@]} screenshot(s) in $SCREENSHOTS_DIR"

# ── Build summary content in a temp file ──────────────────────────────
TEMP_FILE=$(mktemp)
trap 'rm -f "$TEMP_FILE"' EXIT

{
  echo "### 📸 Test Screenshots (${#SCREENSHOTS[@]})"
  echo ""
  echo "<table>"
  echo "<tr><th>Test</th><th>Screenshot</th></tr>"
} >> "$TEMP_FILE"

TRUNCATED=false

for screenshot in "${SCREENSHOTS[@]}"; do
  FILENAME=$(basename "$screenshot" .png)
  # Split ClassName.methodName into readable form
  CLASS_NAME="${FILENAME%%.*}"
  METHOD_NAME="${FILENAME#*.}"

  # Encode as base64
  B64=$(base64 -i "$screenshot" | tr -d '\n')
  IMG_TAG="<img src=\"data:image/png;base64,${B64}\" width=\"320\" alt=\"${FILENAME}\" />"

  ROW="<tr><td><strong>${CLASS_NAME}</strong><br/><code>${METHOD_NAME}</code></td><td><details><summary>🖼️ View</summary>${IMG_TAG}</details></td></tr>"

  # Check byte size before appending (${#VAR} counts characters, not bytes;
  # multi-byte emoji like 📸/🖼️ would be undercounted)
  CURRENT_SIZE=$(wc -c < "$TEMP_FILE")
  ROW_BYTES=$(printf '%s\n' "$ROW" | wc -c)
  if (( CURRENT_SIZE + ROW_BYTES > MAX_SUMMARY_BYTES )); then
    TRUNCATED=true
    break
  fi

  echo "$ROW" >> "$TEMP_FILE"
done

echo "</table>" >> "$TEMP_FILE"

if [ "$TRUNCATED" = true ]; then
  echo "" >> "$TEMP_FILE"
  echo "> ⚠️ **Summary truncated** — too many screenshots for the GitHub summary limit. Download the **test-screenshots** artifact for the complete set." >> "$TEMP_FILE"
fi

echo "" >> "$TEMP_FILE"
echo "_Screenshots captured by \`ScreenshotRule\` — full-screen device captures via UiAutomator._" >> "$TEMP_FILE"

# ── Write to step summary ─────────────────────────────────────────────
cat "$TEMP_FILE" >> "$SUMMARY_FILE"

FINAL_SIZE=$(wc -c < "$TEMP_FILE")
echo "Screenshot summary written (${FINAL_SIZE} bytes)"

