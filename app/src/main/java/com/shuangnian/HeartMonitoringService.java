package com.shuangnian;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.provider.Settings;

import java.util.Collections;
import java.util.UUID;

/**
 * Persistent visible heart-monitor service. The widget can use this service to reconnect
 * to the previously selected BLE device even when MainActivity is not alive. When monitoring
 * or the floating overlay is enabled, this service remains the single GATT owner across Activity
 * foreground/background transitions; MainActivity consumes status broadcasts instead of opening
 * a second connection on every return.
 */
public class HeartMonitoringService extends Service {
    public static final String ACTION_START = "com.shuangnian.monitor.START";
    public static final String ACTION_UPDATE = "com.shuangnian.monitor.UPDATE";
    public static final String ACTION_STOP = "com.shuangnian.monitor.STOP";
    public static final String ACTION_ACTIVITY_VISIBLE = "com.shuangnian.monitor.ACTIVITY_VISIBLE";
    public static final String ACTION_ACTIVITY_HIDDEN = "com.shuangnian.monitor.ACTIVITY_HIDDEN";
    public static final String ACTION_SET_HEART_RATE_MODE = "com.shuangnian.monitor.SET_HEART_RATE_MODE";
    public static final String EXTRA_ENABLE_HEART_RATE_MODE = "enable_heart_rate_mode";
    public static final String EXTRA_HEART_RATE = "heart_rate";
    public static final String EXTRA_DEVICE_NAME = "device_name";
    public static final String EXTRA_CONNECTED = "connected";

    private static final String CHANNEL_MONITOR = "heart_monitor_status";
    private static final String CHANNEL_ALERTS = "heart_rate_alerts";
    private static final int NOTIFICATION_MONITOR = 8510;
    private static final int NOTIFICATION_ALERT = 8511;
    private static final String PREFS = "app_preferences";
    private static final String KEY_MONITOR = "monitor";
    private static final String KEY_OVERLAY = "overlay";
    private static final String KEY_DEVICE_ADDRESS = "device_address";
    private static final String KEY_DEVICE_NAME = "device_name";
    private static final String KEY_LAST_HEART = "monitor_last_hr";
    private static final String KEY_LAST_HEART_AT = "monitor_last_hr_at";
    private static final String KEY_ACTIVITY_VISIBLE = "main_activity_visible";
    private static final String KEY_ACTIVITY_LAST_SEEN = "main_activity_last_seen";
    // Published by MainActivity while its existing GATT connection remains alive.
    private static final String KEY_ACTIVITY_GATT_LAST_SEEN = "activity_gatt_owner_last_seen";
    private static final String KEY_ALERTS_ENABLED = "heart_rate_alerts_enabled";
    private static final String KEY_ALERT_LAST_AT = "heart_rate_alert_last_at";
    private static final UUID HEART_RATE_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    private static final UUID HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_FEE0_SERVICE = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_FEE1_SERVICE = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_HEART_RATE_CONTROL = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences preferences;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic heartRateCharacteristic;
    private boolean foregroundStarted;
    private boolean connected;
    private boolean scanning;
    private boolean intentionalClose;
    private int heartRate;
    private int reconnectAttempt;
    private String deviceName = "心率设备";

    private final Runnable connectionTimeout = () -> {
        if (bluetoothGatt != null && !connected) {
            BluetoothGatt stale = bluetoothGatt;
            bluetoothGatt = null;
            closeQuietly(stale);
            updateConnectionState(false);
            scheduleReconnect();
        }
    };
    private final Runnable scanTimeout = () -> {
        if (scanning) stopScan();
        if (shouldRunInBackground()) scheduleReconnect();
    };
    private final Runnable reconnectRunnable = this::connectSavedDeviceOrScan;
    private final Runnable staleForegroundFallback = () -> {
        if (shouldRunInBackground()) connectSavedDeviceOrScan();
    };

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (ACTION_UPDATE.equals(action)) {
                if (intent.hasExtra(EXTRA_HEART_RATE)) heartRate = intent.getIntExtra(EXTRA_HEART_RATE, heartRate);
                if (intent.hasExtra(EXTRA_DEVICE_NAME)) {
                    String value = intent.getStringExtra(EXTRA_DEVICE_NAME);
                    if (value != null && !value.trim().isEmpty()) deviceName = value;
                }
                if (intent.hasExtra(EXTRA_CONNECTED)) connected = intent.getBooleanExtra(EXTRA_CONNECTED, connected);
                refreshNotification();
                HeartControlWidgetProvider.refreshWidgets(HeartMonitoringService.this);
            } else if (ACTION_ACTIVITY_VISIBLE.equals(action)) {
                preferences.edit().putBoolean(KEY_ACTIVITY_VISIBLE, true)
                        .putLong(KEY_ACTIVITY_LAST_SEEN, System.currentTimeMillis()).apply();
                // Do not disconnect here. This service owns GATT while monitor/overlay is active.
                handler.removeCallbacks(staleForegroundFallback);
                if (shouldRunInBackground()) connectSavedDeviceOrScan();
                // If MainActivity still owns a healthy GATT, its connection state is authoritative.
                // Broadcasting this service's (possibly unused) disconnected state would make the UI
                // appear disconnected and encourage an unnecessary reconnect when returning to foreground.
                if (!isActivityGattOwnerAlive()) broadcastDeviceStatus(false);
            } else if (ACTION_ACTIVITY_HIDDEN.equals(action)) {
                preferences.edit().putBoolean(KEY_ACTIVITY_VISIBLE, false).putLong(KEY_ACTIVITY_LAST_SEEN, 0L).apply();
                if (shouldRunInBackground()) connectSavedDeviceOrScan();
            } else if (ACTION_SET_HEART_RATE_MODE.equals(action)) {
                setHeartRateMode(intent.getBooleanExtra(EXTRA_ENABLE_HEART_RATE_MODE, false));
            }
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (gatt != bluetoothGatt) { closeQuietly(gatt); return; }
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                handler.removeCallbacks(connectionTimeout);
                connected = true;
                reconnectAttempt = 0;
                try {
                    if (hasConnectPermission()) {
                        BluetoothDevice device = gatt.getDevice();
                        if (device != null) {
                            String name = device.getName();
                            if (name != null && !name.trim().isEmpty()) deviceName = name;
                            preferences.edit().putString(KEY_DEVICE_ADDRESS, device.getAddress())
                                    .putString(KEY_DEVICE_NAME, deviceName)
                                    .putBoolean(HeartControlWidgetProvider.KEY_WIDGET_CONNECTED, true).apply();
                        }
                        gatt.discoverServices();
                    }
                } catch (RuntimeException ignored) { }
                updateConnectionState(true);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED || status != BluetoothGatt.GATT_SUCCESS) {
                handler.removeCallbacks(connectionTimeout);
                BluetoothGatt old = bluetoothGatt;
                bluetoothGatt = null;
                heartRateCharacteristic = null;
                connected = false;
                closeQuietly(old);
                updateConnectionState(false);
                if (!intentionalClose && shouldRunInBackground()) scheduleReconnect();
            }
        }

        @Override public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (gatt != bluetoothGatt || status != BluetoothGatt.GATT_SUCCESS) return;
            try {
                BluetoothGattService service = gatt.getService(HEART_RATE_SERVICE);
                if (service == null) {
                    // Some Xiaomi firmware exposes the control point in the standard service only.
                    // Do not invent heart-rate values if the model provides no standard measurement channel.
                    heartRateCharacteristic = null;
                    refreshNotification();
                    return;
                }
                heartRateCharacteristic = service.getCharacteristic(HEART_RATE_MEASUREMENT);
                if (heartRateCharacteristic == null || !hasConnectPermission()) {
                    refreshNotification();
                    return;
                }
                gatt.setCharacteristicNotification(heartRateCharacteristic, true);
                BluetoothGattDescriptor descriptor = heartRateCharacteristic.getDescriptor(CLIENT_CONFIG);
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
                boolean xiaomi = gatt.getService(MI_FEE0_SERVICE) != null || gatt.getService(MI_FEE1_SERVICE) != null;
                if (xiaomi && preferences.getBoolean(KEY_MONITOR, false)) {
                    BluetoothGattCharacteristic control = service.getCharacteristic(MI_HEART_RATE_CONTROL);
                    if (control != null) {
                        handler.postDelayed(() -> {
                            if (gatt != bluetoothGatt || !hasConnectPermission()) return;
                            try {
                                control.setValue(new byte[]{0x15, 0x01});
                                gatt.writeCharacteristic(control);
                            } catch (Exception ignored) { }
                        }, 450L);
                    }
                }
                refreshNotification();
            } catch (RuntimeException ignored) {
                refreshNotification();
            }
        }

        @Override public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic != null) handleHeartPacket(gatt, characteristic, characteristic.getValue());
        }

        @Override public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
            handleHeartPacket(gatt, characteristic, value);
        }

        @Override public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) handleHeartPacket(gatt, characteristic, characteristic.getValue());
        }

        @Override public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) handleHeartPacket(gatt, characteristic, value);
        }
    };

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
            if (result == null || result.getDevice() == null || !scanning) return;
            BluetoothDevice device = result.getDevice();
            try {
                if (!hasConnectPermission()) return;
                String address = device.getAddress();
                String name = device.getName();
                preferences.edit().putString(KEY_DEVICE_ADDRESS, address)
                        .putString(KEY_DEVICE_NAME, name == null || name.trim().isEmpty() ? "BLE 心率设备" : name).apply();
                stopScan();
                connectToDevice(device);
            } catch (SecurityException ignored) { stopScan(); }
        }
        @Override public void onScanFailed(int errorCode) {
            scanning = false;
            scanner = null;
            scheduleReconnect();
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        heartRate = preferences.getInt(KEY_LAST_HEART, 0);
        deviceName = preferences.getString(KEY_DEVICE_NAME, "心率设备");
        createChannels();
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (manager != null) bluetoothAdapter = manager.getAdapter();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE);
        filter.addAction(ACTION_ACTIVITY_VISIBLE);
        filter.addAction(ACTION_ACTIVITY_HIDDEN);
        filter.addAction(ACTION_SET_HEART_RATE_MODE);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(updateReceiver, filter);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_START : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            preferences.edit().putBoolean(HeartControlWidgetProvider.KEY_WIDGET_CONNECTED, false).apply();
            stopBleAndReconnect();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent == null) {
            boolean active = shouldRunInBackground();
            if (!active) { stopForeground(true); stopSelf(); return START_NOT_STICKY; }
        }
        if (intent != null) {
            if (intent.hasExtra(EXTRA_HEART_RATE)) heartRate = intent.getIntExtra(EXTRA_HEART_RATE, heartRate);
            if (intent.hasExtra(EXTRA_DEVICE_NAME)) {
                String value = intent.getStringExtra(EXTRA_DEVICE_NAME);
                if (value != null && !value.trim().isEmpty()) deviceName = value;
            }
            if (intent.hasExtra(EXTRA_CONNECTED)) connected = intent.getBooleanExtra(EXTRA_CONNECTED, connected);
        }
        if (!shouldRunInBackground()) {
            stopBleAndReconnect();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (!foregroundStarted) {
            startForeground(NOTIFICATION_MONITOR, buildMonitoringNotification());
            foregroundStarted = true;
        } else refreshNotification();
        handler.removeCallbacks(staleForegroundFallback);
        // Keep one stable GATT owner regardless of Activity visibility. connectSavedDeviceOrScan()
        // is a no-op while an existing GATT object is alive, so Activity recreation won't reconnect.
        connectSavedDeviceOrScan();
        return START_STICKY;
    }

    private boolean shouldRunInBackground() {
        if (preferences == null) return false;
        return preferences.getBoolean(KEY_MONITOR, false) || preferences.getBoolean(KEY_OVERLAY, false);
    }

    private boolean isActivityVisible() {
        if (preferences == null) return false;
        boolean visible = preferences.getBoolean(KEY_ACTIVITY_VISIBLE, false);
        long seen = preferences.getLong(KEY_ACTIVITY_LAST_SEEN, 0L);
        // A stale persisted foreground flag must not prevent reconnection if Android killed the app process.
        return visible && seen > 0L && System.currentTimeMillis() - seen < 12000L;
    }

    private boolean hasConnectPermission() {
        return Build.VERSION.SDK_INT < 31 || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasScanPermission() {
        if (Build.VERSION.SDK_INT >= 31) return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isActivityGattOwnerAlive() {
        if (preferences == null) return false;
        long seen = preferences.getLong(KEY_ACTIVITY_GATT_LAST_SEEN, 0L);
        return seen > 0L && System.currentTimeMillis() - seen >= 0L
                && System.currentTimeMillis() - seen < 12000L;
    }

    private void connectSavedDeviceOrScan() {
        if (!shouldRunInBackground()) return;
        if (bluetoothGatt != null || scanning) return;
        // MainActivity keeps its established GATT while it is merely in the background.
        // Do not start a second BLE session; retry later so this service can take over only
        // after Activity/process death or a genuinely lost Activity-owned connection.
        if (isActivityGattOwnerAlive()) {
            handler.removeCallbacks(reconnectRunnable);
            handler.postDelayed(reconnectRunnable, 10000L);
            return;
        }
        if (bluetoothAdapter == null) {
            BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            if (manager != null) bluetoothAdapter = manager.getAdapter();
        }
        if (!hasConnectPermission() || bluetoothAdapter == null) {
            connected = false;
            updateConnectionState(false);
            refreshNotification();
            return;
        }
        try {
            if (!bluetoothAdapter.isEnabled()) {
                connected = false;
                updateConnectionState(false);
                scheduleReconnect();
                return;
            }
        } catch (SecurityException permissionError) {
            updateConnectionState(false);
            return;
        }
        String address = preferences.getString(KEY_DEVICE_ADDRESS, "");
        if (address != null && !address.trim().isEmpty()) {
            try {
                connectToDevice(bluetoothAdapter.getRemoteDevice(address));
                return;
            } catch (Exception ignored) { }
        }
        startHeartRateScan();
    }

    private void connectToDevice(BluetoothDevice device) {
        if (device == null || !shouldRunInBackground() || !hasConnectPermission()) return;
        stopScan();
        handler.removeCallbacks(reconnectRunnable);
        handler.removeCallbacks(connectionTimeout);
        BluetoothGatt previousGatt = bluetoothGatt;
        bluetoothGatt = null;
        intentionalClose = true;
        closeQuietly(previousGatt);
        intentionalClose = false;
        heartRateCharacteristic = null;
        try {
            String address = device.getAddress();
            String name = device.getName();
            if (name != null && !name.trim().isEmpty()) deviceName = name;
            preferences.edit().putString(KEY_DEVICE_ADDRESS, address).putString(KEY_DEVICE_NAME, deviceName).apply();
            bluetoothGatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
            connected = false;
            updateConnectionState(false);
            if (bluetoothGatt == null) {
                scheduleReconnect();
            } else {
                handler.postDelayed(connectionTimeout, 18000L);
            }
        } catch (RuntimeException error) {
            bluetoothGatt = null;
            updateConnectionState(false);
            scheduleReconnect();
        }
    }

    private void startHeartRateScan() {
        if (!hasScanPermission() || !hasConnectPermission() || bluetoothAdapter == null) {
            updateConnectionState(false);
            refreshNotification();
            return;
        }
        try {
            if (!bluetoothAdapter.isEnabled()) {
                updateConnectionState(false);
                scheduleReconnect();
                return;
            }
        } catch (SecurityException permissionError) {
            updateConnectionState(false);
            return;
        }
        try {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner == null) { scheduleReconnect(); return; }
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(HEART_RATE_SERVICE)).build();
            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            scanner.startScan(Collections.singletonList(filter), settings, scanCallback);
            scanning = true;
            handler.removeCallbacks(scanTimeout);
            handler.postDelayed(scanTimeout, 15000L);
            refreshNotification();
        } catch (RuntimeException error) {
            scanning = false;
            scanner = null;
            scheduleReconnect();
        }
    }

    private void stopScan() {
        handler.removeCallbacks(scanTimeout);
        if (scanner != null && scanning && hasScanPermission()) {
            try { scanner.stopScan(scanCallback); } catch (Exception ignored) { }
        }
        scanning = false;
        scanner = null;
    }

    private void scheduleReconnect() {
        if (!shouldRunInBackground()) return;
        handler.removeCallbacks(reconnectRunnable);
        long delay = Math.min(30000L, 2500L << Math.min(reconnectAttempt++, 4));
        handler.postDelayed(reconnectRunnable, delay);
    }

    private void releaseBleOwnership() {
        handler.removeCallbacks(reconnectRunnable);
        stopBleAndReconnect();
        connected = false;
        updateConnectionState(false);
        handler.removeCallbacks(staleForegroundFallback);
        handler.postDelayed(staleForegroundFallback, 13000L);
    }

    private void stopBleAndReconnect() {
        handler.removeCallbacks(reconnectRunnable);
        handler.removeCallbacks(connectionTimeout);
        stopScan();
        intentionalClose = true;
        BluetoothGatt old = bluetoothGatt;
        bluetoothGatt = null;
        heartRateCharacteristic = null;
        connected = false;
        closeQuietly(old);
        intentionalClose = false;
    }

    private void closeQuietly(BluetoothGatt gatt) {
        if (gatt == null) return;
        try { gatt.disconnect(); } catch (Exception ignored) { }
        try { gatt.close(); } catch (Exception ignored) { }
    }

    private void updateConnectionState(boolean isConnected) {
        connected = isConnected;
        if (preferences != null) preferences.edit().putBoolean(HeartControlWidgetProvider.KEY_WIDGET_CONNECTED, isConnected).apply();
        HeartControlWidgetProvider.refreshWidgets(this);
        refreshNotification();
        broadcastDeviceStatus(false);
    }

    private void broadcastDeviceStatus() {
        broadcastDeviceStatus(false);
    }

    private void broadcastDeviceStatus(boolean includeHeartSample) {
        Intent status = new Intent("com.shuangnian.DEVICE_STATUS").setPackage(getPackageName());
        status.putExtra(EXTRA_CONNECTED, connected).putExtra(EXTRA_DEVICE_NAME, deviceName);
        if (includeHeartSample && connected && heartRate > 0) status.putExtra(EXTRA_HEART_RATE, heartRate);
        sendBroadcast(status);
    }

    private void setHeartRateMode(boolean enabled) {
        BluetoothGatt gatt = bluetoothGatt;
        if (gatt == null || !connected || !hasConnectPermission()) return;
        try {
            BluetoothGattService hrService = gatt.getService(HEART_RATE_SERVICE);
            if (hrService == null) return;
            BluetoothGattCharacteristic control = hrService.getCharacteristic(MI_HEART_RATE_CONTROL);
            if (control == null) control = hrService.getCharacteristic(UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb"));
            if (control == null) return;
            control.setValue(new byte[]{0x15, (byte) (enabled ? 0x01 : 0x00)});
            gatt.writeCharacteristic(control);
        } catch (Exception ignored) { }
    }

    private void handleHeartPacket(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] data) {
        if (gatt != bluetoothGatt || characteristic == null || data == null || data.length < 2
                || !HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) return;
        int flags = data[0] & 0xFF;
        int bpm = (flags & 0x01) != 0 && data.length >= 3
                ? ((data[1] & 0xFF) | ((data[2] & 0xFF) << 8))
                : (data[1] & 0xFF);
        if (bpm <= 0 || bpm > 250) return;
        heartRate = bpm;
        long now = System.currentTimeMillis();
        preferences.edit().putInt(KEY_LAST_HEART, bpm).putLong(KEY_LAST_HEART_AT, now).putBoolean(HeartControlWidgetProvider.KEY_WIDGET_CONNECTED, true).apply();
        try {
            HeartDatabase db = new HeartDatabase(this);
            db.insertHeartRate(bpm, now);
            db.close();
        } catch (Exception ignored) { }
        HeartControlWidgetProvider.recordHeartRate(this, bpm);
        Intent overlay = new Intent(HeartOverlayService.ACTION_UPDATE).setPackage(getPackageName()).putExtra(EXTRA_HEART_RATE, bpm);
        sendBroadcast(overlay);
        broadcastDeviceStatus(true);
        updateNotificationSample(bpm);
        if (preferences.getBoolean(KEY_ALERTS_ENABLED, false)) {
            int low = preferences.getInt("low_threshold", 50);
            int high = preferences.getInt("high_threshold", 120);
            long last = preferences.getLong(KEY_ALERT_LAST_AT, 0L);
            if ((bpm < low || bpm > high) && now - last >= 60000L) {
                preferences.edit().putLong(KEY_ALERT_LAST_AT, now).apply();
                postThresholdAlert(this, bpm, low, high, deviceName);
            }
        }
    }

    private void updateNotificationSample(int bpm) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null && foregroundStarted) manager.notify(NOTIFICATION_MONITOR, buildMonitoringNotification());
    }

    @Override public void onTaskRemoved(Intent rootIntent) {
        // START_STICKY and foreground operation keep enabled widget features running after task removal.
        super.onTaskRemoved(rootIntent);
    }

    @Override public void onDestroy() {
        stopBleAndReconnect();
        try { unregisterReceiver(updateReceiver); } catch (Exception ignored) { }
        foregroundStarted = false;
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    private Notification buildMonitoringNotification() {
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingOpen = PendingIntent.getActivity(this, 8510, open,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        String title = connected ? "心率监测正在运行" : "心率监测等待连接";
        String content;
        if (connected && heartRate > 0) content = deviceName + " · " + heartRate + " BPM · 实时接收中";
        else if (connected) content = deviceName + " · 蓝牙已连接，等待心率数据";
        else if (isActivityVisible()) content = "应用前台管理蓝牙连接";
        else if (!hasConnectPermission()) content = "缺少附近设备权限，无法自动连接";
        else if (preferences.getString(KEY_DEVICE_ADDRESS, "").trim().isEmpty() && !hasScanPermission()) content = "尚未保存设备；请先在应用内授权并选择心率设备";
        else content = deviceName + " · 正在尝试后台连接/重连";
        if (Build.VERSION.SDK_INT >= 26) {
            return new Notification.Builder(this, CHANNEL_MONITOR)
                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(new Notification.BigTextStyle().bigText(content))
                    .setContentIntent(pendingOpen)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
        }
        return new Notification.Builder(this).setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle(title).setContentText(content).setContentIntent(pendingOpen).setOngoing(true).build();
    }

    private void refreshNotification() {
        if (!foregroundStarted) return;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(NOTIFICATION_MONITOR, buildMonitoringNotification());
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;
        NotificationChannel running = new NotificationChannel(CHANNEL_MONITOR, "后台心率监测", NotificationManager.IMPORTANCE_LOW);
        running.setDescription("监测开启时显示连接状态与最新心率");
        running.setShowBadge(false);
        manager.createNotificationChannel(running);
        NotificationChannel alerts = new NotificationChannel(CHANNEL_ALERTS, "心率范围提醒", NotificationManager.IMPORTANCE_DEFAULT);
        alerts.setDescription("当实时心率超出你设置的提醒范围时通知你");
        manager.createNotificationChannel(alerts);
    }

    /** Posts a user-enabled threshold alert, independently from the ongoing status notification. */
    public static void postThresholdAlert(Context context, int heartRate, int low, int high, String device) {
        if (context == null || heartRate <= 0) return;
        try {
            Intent launch = new Intent(context, MainActivity.class);
            launch.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pending = PendingIntent.getActivity(context, 8511, launch,
                    PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
            NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (manager == null) return;
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel alerts = new NotificationChannel(CHANNEL_ALERTS, "心率范围提醒", NotificationManager.IMPORTANCE_DEFAULT);
                alerts.setDescription("当实时心率超出你设置的提醒范围时通知你");
                manager.createNotificationChannel(alerts);
                Notification notification = new Notification.Builder(context, CHANNEL_ALERTS)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle(heartRate < low ? "心率低于提醒范围" : "心率高于提醒范围")
                        .setContentText((device == null || device.trim().isEmpty() ? "心率设备" : device) + " · 当前 " + heartRate + " BPM，设置范围 " + low + "–" + high + " BPM")
                        .setContentIntent(pending).setAutoCancel(true).build();
                manager.notify(NOTIFICATION_ALERT, notification);
            } else {
                Notification notification = new Notification.Builder(context).setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle(heartRate < low ? "心率低于提醒范围" : "心率高于提醒范围")
                        .setContentText("当前 " + heartRate + " BPM，设置范围 " + low + "–" + high + " BPM")
                        .setContentIntent(pending).setAutoCancel(true).build();
                manager.notify(NOTIFICATION_ALERT, notification);
            }
        } catch (Exception ignored) { }
    }
}
