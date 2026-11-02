#  Remote Access

## Features
- 📷 **Camera Access**: Take photos/videos remotely
- 📞 **Call Logs**: Access incoming/outgoing calls
- 📁 **File Access**: Browse and download files
- 📍 **Location**: GPS tracking
- 📱 **Device Info**: Get device details
- 🎤 **Microphone**: Record audio (optional)
- 📡 **SMS**: Read SMS messages (optional)

## Architecture
```
┌─────────────┐         ┌─────────────┐
│  Android    │         │  C2 Server  │
│  Client     │◄───────►│  (Python)   │
│  (APK)      │  HTTPS  │  (Flask)    │
└─────────────┘         └─────────────┘
```

## Files
- `app/src/main/java/` - Android client code
- `server.py` - C2 server
- `AndroidManifest.xml` - Permissions
- `build.gradle` - Build configuration

## Build Instructions
```bash
# Using Android Studio
1. Open project in Android Studio
2. Sync Gradle
3. Build APK (Build > Build Bundle(s) / APK(s) > Build APK(s))

# Using Command Line
./gradlew assembleRelease
```

## Usage
1. Start C2 server: `python3 server.py`
2. Install APK on target device
3. Grant all permissions
4. Access dashboard at http://localhost:5000

## Configuration
Edit `MainActivity.java`:
- `SERVER_URL`: C2 server address
- `CHECK_INTERVAL`: Heartbeat interval (ms)
