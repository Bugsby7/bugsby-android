name: Build Bugsby APK

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Generate Keystore
        run: |
          keytool -genkeypair -v \
            -keystore app/release.keystore \
            -alias bugsby \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -storepass bugsby123 \
            -keypass bugsby123 \
            -dname "CN=Bugsby, OU=Companion, O=Bugsby, L=Android, S=Global, C=US"

      - name: Build APKs
        run: ./gradlew assembleRelease assembleDebug --stacktrace

      - name: Prepare APK Deliverables
        run: |
          mkdir -p dist
          cp app/build/outputs/apk/release/app-release.apk dist/Bugsby.apk
          cp app/build/outputs/apk/debug/app-debug.apk dist/Bugsby-debug.apk
          ls -lh dist/

      - name: Verify APK Signatures (no re-signing)
        run: |
          BUILD_TOOLS_DIR=$(find $ANDROID_HOME/build-tools -mindepth 1 -maxdepth 1 | sort -V | tail -n 1)
          echo "Using apksigner from $BUILD_TOOLS_DIR"
          echo "=== Verifying Bugsby.apk ==="
          $BUILD_TOOLS_DIR/apksigner verify --verbose dist/Bugsby.apk
          echo "=== Verifying Bugsby-debug.apk ==="
          $BUILD_TOOLS_DIR/apksigner verify --verbose dist/Bugsby-debug.apk || true

      - name: Upload APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: Bugsby-APKs
          path: dist/*.apk

      - name: Release APK
        uses: softprops/action-gh-release@v2
        if: startsWith(github.ref, 'refs/heads/main')
        with:
          tag_name: v1.0.3
          name: Bugsby v1.0.3 (Clean Signed APK)
          draft: false
          prerelease: false
          body: |
            ## 🥷 Bugsby AI Companion - v1.0.3

            Clean build. Gradle does the only signing (v1 + v2). No extra re-sign step.

            ### 📦 Download
            - **Bugsby.apk** (release)
            - **Bugsby-debug.apk** (debug)

            ### Install
            1. Download Bugsby.apk
            2. Open with Files app → Install
          files: dist/*.apk
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}