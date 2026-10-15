# 🥷 Bugsby - Floating AI Ninja Companion for Android

[![Download Latest APK](https://img.shields.io/badge/Download-Bugsby.apk_(v1.0.2)-4CAF50?style=for-the-badge&logo=android)](https://github.com/Bugsby7/bugsby-android/releases/download/v1.0.2/Bugsby.apk)
[![Download Debug APK](https://img.shields.io/badge/Download-Bugsby--debug.apk-blue?style=for-the-badge&logo=android)](https://github.com/Bugsby7/bugsby-android/releases/download/v1.0.2/Bugsby-debug.apk)

---

### 📥 Direct Download Links (v1.0.2)
- **[👉 Download Bugsby.apk (Recommended Signed Release)](https://github.com/Bugsby7/bugsby-android/releases/download/v1.0.2/Bugsby.apk)** (~10 MB)
- **[👉 Download Bugsby-debug.apk (Debug Build)](https://github.com/Bugsby7/bugsby-android/releases/download/v1.0.2/Bugsby-debug.apk)** (~15 MB)

---

# 🥷 Bugsby - Floating AI Ninja Companion for Android

Bugsby is an intelligent, floating 2D ninja AI companion designed for Android. He floats across your screen as an interactive overlay, reacts with animated expressions, responds to voice commands, and can transform into **Super Saiyan 3D mode** with blazing energy auras!

![Bugsby Reference](https://raw.githubusercontent.com/Bugsby7/bugsby-android/main/art/reference.jpg)

---

## ✨ Features (Phase 1)

1. **Floating Draggable Ninja Overlay (`SYSTEM_ALERT_WINDOW`)**
   - Lives as a persistent floating overlay on top of any active Android app.
   - Smooth drag tracking with magnetic edge-snapping to screen borders.
2. **Idle Hide-in-Shadows**
   - After 10 seconds of no interaction, Bugsby tucks into the nearest screen edge and fades to 35% opacity (ninja stealth mode).
   - Wakes up immediately upon touch.
3. **⚡ Super Saiyan Evolve Mode**
   - **Voice Command:** Say *"Evolve"*, *"Super Saiyan"*, or *"Power Up"* to trigger a dramatic transformation!
   - **Quick Toggle:** Tap Bugsby to reveal the radial menu and tap the lightning bolt icon.
   - **Visual Effects:** Golden radial aura, crackling cyan electric arcs, glowing energy eyes, and 3D illuminated rim lighting.
   - Say *"Normal"* to return to the calm, fluffy 2D ninja form.
4. **Animated Expressions**
   - Idle breathing bob and natural periodic blinking.
   - Mood states: Happy (`^ ^`), Thinking, Focused, Excited, and Sleepy.
5. **Hybrid Brain & Phone Control**
   - **Offline / Local Intents:**
     - `"Open [app name]"` (launches any installed app)
     - `"Search [query]"` (Google search)
     - `"Play [song]"` (YouTube / Spotify deep link)
     - `"Turn on flashlight"`, `"Alarm"`
   - **Free Cloud AI Brain:**
     - Supports free Google Gemini 1.5 Flash API key in Settings for warm, witty ninja conversations.
6. **Adaptive Voice**
   - Speech-to-text voice recognition.
   - Text-to-speech with adaptive pitch (energetic and higher pitch during Super Saiyan mode, stealth tone in normal mode).

---

## 📲 How to Download & Install the APK on Your Phone

You don't need a laptop or computer! You can do everything right from your Android phone:

1. Go to the [Bugsby GitHub Releases page](https://github.com/Bugsby7/bugsby-android/releases).
2. Tap on the latest release (`v1.0.0`) and download **`app-debug.apk`**.
3. In your phone's notification panel or Files app, tap the downloaded `.apk` file.
4. If Android displays *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings**.
   - Turn **ON** *"Allow from this source"*.
   - Tap **Back** and tap **Install**.
5. Launch **Bugsby** from your app drawer.
6. On the onboarding screen:
   - Tap **Grant** for **Display over other apps** (enables the floating ninja).
   - Tap **Grant** for **Microphone** (enables voice commands).
7. Tap **Start Bugsby** and enjoy your floating ninja companion!

---

## 🛠️ Automated Cloud Builds (GitHub Actions)

Every time code is updated, GitHub Actions automatically compiles the latest code into an installable Android APK and publishes it to the Releases tab.
