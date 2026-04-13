#!/bin/bash
# Rotates the Android release signing key and updates all related GitHub secrets.
#
# What this script does:
#   1. Generates a new PKCS12 keystore (you will be prompted for a password)
#   2. Extracts SHA-1 and SHA-256 fingerprints and prints them for Firebase
#   3. Encodes the keystore to base64 and updates RELEASE_KEYSTORE_BASE64
#   4. Prompts for SIGNING_KEY_ALIAS, SIGNING_KEY_PASSWORD, SIGNING_STORE_PASSWORD
#      and updates those secrets on GitHub
#   5. Shreds the keystore from disk immediately after encoding
#
# Prerequisites:
#   - GitHub CLI (gh) installed and authenticated  →  https://cli.github.com/
#   - keytool (bundled with JDK 21)
#
# After running:
#   - Go to Firebase Console → Project Settings → Your Android app
#   - Delete the old release SHA-1 and SHA-256 fingerprints
#   - Add the new ones printed by this script
#   - Download the new google-services.json
#   - Run update-google-services-secret.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYSTORE_TMP="$(mktemp /tmp/release-XXXXXX.keystore)"

# ─── Preflight checks ────────────────────────────────────────────────────────

if ! command -v gh &>/dev/null; then
  echo "❌ GitHub CLI (gh) is not installed. Install it from https://cli.github.com/"
  exit 1
fi

if ! command -v keytool &>/dev/null; then
  echo "❌ keytool not found. Make sure JDK 21 is installed and on your PATH."
  exit 1
fi

echo ""
echo "🔑 Android Release Signing Key Rotation"
echo "════════════════════════════════════════"
echo ""

# ─── Collect inputs ──────────────────────────────────────────────────────────

read -rp "Key alias (e.g. splittrip): " KEY_ALIAS
if [ -z "$KEY_ALIAS" ]; then
  echo "❌ Key alias cannot be empty."
  exit 1
fi

echo ""
echo "ℹ️  With PKCS12 keystores, the store password and key password are the same."
echo "   You will be prompted once by keytool — use this same password for both"
echo "   SIGNING_STORE_PASSWORD and SIGNING_KEY_PASSWORD secrets."
echo ""

# ─── Generate keystore ───────────────────────────────────────────────────────

echo "🏗️  Generating new keystore (keytool will ask for your password and org details)..."
echo "   Press Enter through the name/org fields — they don't affect functionality."
echo "   Type 'yes' when asked to confirm."
echo ""

keytool -genkeypair \
  -keystore "$KEYSTORE_TMP" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -storetype PKCS12

echo ""
echo "✅ Keystore generated."
echo ""

# ─── Extract and display fingerprints ────────────────────────────────────────

echo "🔍 Extracting certificate fingerprints..."
FINGERPRINTS=$(keytool -list -v \
  -keystore "$KEYSTORE_TMP" \
  -alias "$KEY_ALIAS" \
  -storetype PKCS12 2>/dev/null | grep -E "SHA1:|SHA256:")

SHA1=$(echo "$FINGERPRINTS" | grep "SHA1:" | awk '{print $2}')
SHA256=$(echo "$FINGERPRINTS" | grep "SHA256:" | awk '{print $2}')

echo ""
echo "┌─────────────────────────────────────────────────────────────┐"
echo "│  🔥 ADD THESE TO FIREBASE CONSOLE (Project Settings → App)  │"
echo "├─────────────────────────────────────────────────────────────┤"
echo "│  SHA-1   (release):                                          │"
printf "│  %-61s│\n" "$SHA1"
echo "│                                                              │"
echo "│  SHA-256 (release):                                          │"
printf "│  %-61s│\n" "${SHA256:0:61}"
if [ ${#SHA256} -gt 61 ]; then
  printf "│  %-61s│\n" "${SHA256:61}"
fi
echo "└─────────────────────────────────────────────────────────────┘"
echo ""
echo "  Steps in Firebase:"
echo "  1. Go to Firebase Console → Project Settings → Your Android app"
echo "  2. Delete the old release SHA-1 and SHA-256 fingerprints"
echo "  3. Add the SHA-1 and SHA-256 printed above"
echo "  4. Download the new google-services.json"
echo "  5. Run: scripts/update-google-services-secret.sh"
echo ""

# ─── Encode keystore and update GitHub secret ────────────────────────────────

echo "🔐 Encoding keystore and updating RELEASE_KEYSTORE_BASE64..."
base64 -i "$KEYSTORE_TMP" | gh secret set RELEASE_KEYSTORE_BASE64
echo "✅ RELEASE_KEYSTORE_BASE64 updated."

# ─── Shred the keystore immediately ──────────────────────────────────────────

echo "🧹 Securely removing keystore from disk..."
# Use shred if available (Linux), otherwise overwrite manually (macOS)
if command -v shred &>/dev/null; then
  shred -u "$KEYSTORE_TMP"
else
  dd if=/dev/urandom of="$KEYSTORE_TMP" bs=1024 count=64 2>/dev/null || true
  rm -f "$KEYSTORE_TMP"
fi
echo "✅ Keystore removed."
echo ""

# ─── Update signing metadata secrets ─────────────────────────────────────────

echo "📝 Now update the remaining signing secrets."
echo "   (These are stored encrypted — they will NOT be echoed to the terminal)"
echo ""

echo -n "SIGNING_KEY_ALIAS → press Enter to use '$KEY_ALIAS', or type a different value: "
read -r ALIAS_OVERRIDE
FINAL_ALIAS="${ALIAS_OVERRIDE:-$KEY_ALIAS}"
echo -n "$FINAL_ALIAS" | gh secret set SIGNING_KEY_ALIAS
echo "✅ SIGNING_KEY_ALIAS updated."

echo ""
printf "SIGNING_STORE_PASSWORD → "
read -rs STORE_PASSWORD
echo ""
echo -n "$STORE_PASSWORD" | gh secret set SIGNING_STORE_PASSWORD
echo "✅ SIGNING_STORE_PASSWORD updated."

echo ""
printf "SIGNING_KEY_PASSWORD   → (same as store password for PKCS12, or press Enter to reuse it): "
read -rs KEY_PASSWORD_OVERRIDE
echo ""
FINAL_KEY_PASSWORD="${KEY_PASSWORD_OVERRIDE:-$STORE_PASSWORD}"
echo -n "$FINAL_KEY_PASSWORD" | gh secret set SIGNING_KEY_PASSWORD
echo "✅ SIGNING_KEY_PASSWORD updated."

# ─── Done ────────────────────────────────────────────────────────────────────

echo ""
echo "════════════════════════════════════════"
echo "✅ Signing key rotation complete!"
echo ""
echo "📋 Checklist:"
echo "   ☐ Add the SHA-1 and SHA-256 above to Firebase Console"
echo "   ☐ Download the new google-services.json from Firebase"
echo "   ☐ Run: scripts/update-google-services-secret.sh"
echo "   ☐ Push to main to trigger the release workflow"
echo ""

