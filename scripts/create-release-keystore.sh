#!/usr/bin/env bash
# Creates the upload/release key and keystore.properties. Run once, then back up the .jks file and
# the passwords somewhere safe: without them you can never publish an update to the same listing.
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p keystore
read -r -s -p "Keystore password (min 6 chars): " STORE_PASS; echo
read -r -s -p "Key password (Enter = same): " KEY_PASS; echo
KEY_PASS=${KEY_PASS:-$STORE_PASS}
keytool -genkeypair -v \
  -keystore keystore/ravan-release.jks -storetype PKCS12 \
  -alias ravan -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass "$STORE_PASS" -keypass "$KEY_PASS" \
  -dname "CN=Ravan, O=Fama Studio, C=IR"
cat > keystore.properties <<PROPS
storeFile=keystore/ravan-release.jks
storePassword=$STORE_PASS
keyAlias=ravan
keyPassword=$KEY_PASS
PROPS
echo "Created keystore/ravan-release.jks and keystore.properties"
