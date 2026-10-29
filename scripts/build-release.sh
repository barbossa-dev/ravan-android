#!/usr/bin/env bash
# Builds the signed release APK and App Bundle and runs the unit tests first.
set -euo pipefail
cd "$(dirname "$0")/.."

# Gradle 8.13 / Kotlin 2.2 need JDK 17–24. Use Android Studio's bundled JDK (JBR) so a newer
# system JDK (e.g. 25+) doesn't break the build.
for jbr in "/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
           "$HOME/Applications/Android Studio.app/Contents/jbr/Contents/Home"; do
  if [ -x "$jbr/bin/java" ]; then export JAVA_HOME="$jbr"; break; fi
done
echo "Using JDK: ${JAVA_HOME:-system default}"
[ -f keystore.properties ] || echo "WARNING: keystore.properties missing — release is signed with the DEBUG key (do not publish)."
./gradlew clean test :app:assembleRelease :app:bundleRelease
echo
echo "APK: app/build/outputs/apk/release/app-release.apk"
echo "AAB: app/build/outputs/bundle/release/app-release.aab"
echo "R8 mapping (keep it for crash reports): app/build/outputs/mapping/release/mapping.txt"
