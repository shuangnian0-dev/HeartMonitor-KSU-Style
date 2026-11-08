package com.shuangnian;

import android.Manifest;
import android.app.Activity;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.RadialGradient;
import android.graphics.SurfaceTexture;
import android.graphics.Shader;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.DecelerateInterpolator;

import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class MainActivity extends Activity {

    // =========================================================
    // BLE UUID
    // =========================================================

    private static final UUID HEART_RATE_SERVICE =
            UUID.fromString(
                    "0000180d-0000-1000-8000-00805f9b34fb"
            );

    private static final UUID HEART_RATE_MEASUREMENT =
            UUID.fromString(
                    "00002a37-0000-1000-8000-00805f9b34fb"
            );

    private static final UUID CLIENT_CONFIG =
            UUID.fromString(
                    "00002902-0000-1000-8000-00805f9b34fb"
            );

    private static final UUID BATTERY_SERVICE =
            UUID.fromString(
                    "0000180f-0000-1000-8000-00805f9b34fb"
            );

    private static final UUID BATTERY_LEVEL =
            UUID.fromString(
                    "00002a19-0000-1000-8000-00805f9b34fb"
            );

    // 小米手环 BLE 服务：兼容旧款 FFxx 协议以及 Mi Band 2/3 等 3512 私有 UUID。
    private static final UUID MI_FEE0_SERVICE = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_FEE1_SERVICE = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_BATTERY_CHARACTERISTIC = UUID.fromString("0000ff0c-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_LEGACY_DEVICE_INFO_CHARACTERISTIC = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_LEGACY_STEP_CHARACTERISTIC = UUID.fromString("0000ff06-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_LEGACY_NOTIFY_CHARACTERISTIC = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_LEGACY_LE_PARAMS_CHARACTERISTIC = UUID.fromString("0000ff09-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_LEGACY_DATE_TIME_CHARACTERISTIC = UUID.fromString("0000ff0a-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_LEGACY_ACTIVITY_CHARACTERISTIC = UUID.fromString("0000ff07-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_LEGACY_SENSOR_CHARACTERISTIC = UUID.fromString("0000ff0e-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_LEGACY_CONTROL_CHARACTERISTIC = UUID.fromString("0000ff05-0000-1000-8000-00805f9b34fb");
    private static final UUID MI_BAND2_BATTERY_CHARACTERISTIC = UUID.fromString("00000006-0000-3512-2118-0009af100700");
    private static final UUID MI_BAND2_STEP_CHARACTERISTIC = UUID.fromString("00000007-0000-3512-2118-0009af100700");
    private static final UUID HEART_RATE_CONTROL_POINT = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");

    private static final UUID DEVICE_INFORMATION_SERVICE =
            UUID.fromString(
                    "0000180a-0000-1000-8000-00805f9b34fb"
            );

    private static final UUID MANUFACTURER =
            UUID.fromString(
                    "00002a29-0000-1000-8000-00805f9b34fb"
            );

    private static final UUID MODEL_NUMBER =
            UUID.fromString(
                    "00002a24-0000-1000-8000-00805f9b34fb"
            );

    private static final UUID FIRMWARE =
            UUID.fromString(
                    "00002a26-0000-1000-8000-00805f9b34fb"
            );

    // =========================================================
    // 页面
    // =========================================================

    private static final int PAGE_HOME = 0;
    private static final int PAGE_DATA = 1;
    private static final int PAGE_DEVICE = 2;
    private static final int PAGE_MONITOR = 3;
    private static final int PAGE_SETTINGS = 4;
    private static final int PAGE_THEME = 5; // 主题设置（从设置页进入，不在底栏）

    private int currentPage = PAGE_HOME;

    // =========================================================
    // 颜色
    // =========================================================

    private boolean darkMode = false;

    private int backgroundColor;
    private int cardColor;
    private int textColor;
    private int secondaryTextColor;
    private int accentColor;
    private int dividerColor;

    // =========================================================
    // UI
    // =========================================================

    private FrameLayout root;
    private ImageView customBackgroundView;
    private TextureView desktopWallpaperVideoView;
    private MediaPlayer desktopWallpaperVideoPlayer;
    private Surface desktopWallpaperVideoSurface;
    private Uri desktopWallpaperVideoUri;
    private Uri desktopWallpaperVideoPendingUri;
    private boolean activityResumed = false;
    private View backgroundScrim;
    private Bitmap desktopWallpaperBitmap;
    private volatile boolean wallpaperReadInProgress = false;
    private ImageView swipePreviewImage;
    private Bitmap swipePreviewBitmap;
    private final Bitmap[] swipePagePreviewCache = new Bitmap[PAGE_THEME + 1];
    private final int[] pageScrollPositions = new int[PAGE_THEME + 1];
    private boolean renderingSwipePreview = false;
    private boolean suppressNextPageTransitionAnimation = false;
    private boolean firstInterfaceBuilt = false;
    private String lastToastMessage = "";
    private long lastToastUptime = 0L;
    private boolean swipeInProgress = false;
    private boolean swipePreviewPreparing = false;
    private float swipeDownX = 0f;
    private float swipeDownY = 0f;
    private float swipeDeltaX = 0f;
    private int swipeTargetPage = -1;
    private int swipeOriginalPage = -1;
    private LinearLayout contentContainer;
    private ScrollView contentScrollView;
    private LinearLayout bottomNavigation;
    private FrameLayout navContentFrame;
    private View selectedNavPill;
    private ObjectAnimator navPillPositionAnimator;
    private FrameLayout navigationShell;
    private GlassBarShell glassBarShell;
    // 记录应用内使用全局液态玻璃的表面，滑块调整时原位更新，不重建页面避免闪屏。
    private final Map<View, GlassSurfaceSpec> glassSurfaceSpecs = new WeakHashMap<>();

    private static final class GlassSurfaceSpec {
        final int baseColor;
        final int radiusDp;
        final int disabledStrokeColor;
        GlassSurfaceSpec(int baseColor, int radiusDp, int disabledStrokeColor) {
            this.baseColor = baseColor;
            this.radiusDp = radiusDp;
            this.disabledStrokeColor = disabledStrokeColor;
        }
    }
    private final Runnable adjacentPreviewPreloadRunnable = () -> preloadAdjacentSwipePreviews();
    private int navDraggedIndex = -1;
    private float navTouchDownRawX;
    private float navTouchDownRawY;
    private boolean navDragActive = false;
    private boolean navPointerMoved = false;
    private float navDragOriginPillX;
    private int navDragStartIndex = -1;
    private float navDragLastRawX;
    private long navDragLastTime;
    private final LinearLayout[] navItems = new LinearLayout[5];
    private final TextView[] navIcons = new TextView[5];
    private final TextView[] navTitles = new TextView[5];

    private TextView homeHeart;
    private TextView homeStatus;
    private TextView homeDevice;
    private TextView homeBattery;
    private TextView homeRssi;
    private TextView homeMin;
    private TextView homeAvg;
    private TextView homeMax;
    private TextView homeCount;
    private TextView homeLast;

    private LinearLayout historyContainer;

    private TextView deviceStatus;
    private TextView deviceName;
    private TextView deviceAddress;
    private TextView deviceRssi;
    private TextView deviceBattery;
    private TextView deviceSteps;
    private TextView devicePacketCount;
    private TextView deviceManufacturer;
    private TextView deviceModel;
    private TextView deviceFirmware;
    private TextView deviceCapabilities;
    private TextView deviceBatteryCapability;

    private TextView monitorStatus;
    private TextView monitorHeart;

    private HeartRateChart chart;
    private HeartRateChart dataChart;
    private TextView dataMinValue;
    private TextView dataAvgValue;
    private TextView dataMaxValue;
    private TextView dataCountText;
    private int selectedDataRange = 0; // 0=今天，1=近7天，2=近30天
    private final ArrayList<EcgChart> ecgCharts = new ArrayList<>();

    // =========================================================
    // BLE
    // =========================================================

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;
    private BluetoothGatt bluetoothGatt;

    private boolean scanning = false;
    private boolean monitoring = false;
    private boolean connected = false;
    private String lastBluetoothDiagnostic = "尚未开始连接";
    private final Runnable scanTimeoutRunnable = this::stopScan;
    // 发现首批设备后短暂收集邻近广播，再提前展示列表；无设备时仍保留兜底超时。
    // 通过方法引用延迟读取 discoveredDevices，避免字段初始化阶段引用后定义的成员。
    private final Runnable scanEarlyFinishRunnable = this::finishEarlyScanIfReady;

    private void finishEarlyScanIfReady() {
        if (scanning && !discoveredDevices.isEmpty()) {
            stopScan();
        }
    }

    private String connectedName = "未连接";
    private String connectedAddress = "";

    private int currentHeartRate = 0;
    private int currentRssi = 0;
    private int currentBattery = -1;
    private int currentSteps = -1;
    private boolean userRequestedDisconnect = false;
    private int reconnectAttempts = 0;
    private boolean scanAfterPermission = false;
    private boolean servicesDiscovered = false;
    private final Runnable connectTimeoutRunnable = () -> {
        if (!connected && bluetoothGatt != null) {
            BluetoothGatt timedOut = bluetoothGatt;
            bluetoothGatt = null;
            lastBluetoothDiagnostic = "连接超时：设备没有在规定时间内响应。请唤醒手环并断开其他手机的连接";
            try { timedOut.disconnect(); } catch (Exception ignored) { }
            try { timedOut.close(); } catch (Exception ignored) { }
            updateStatusViews();
            toast("蓝牙连接超时，请唤醒手环并确认没有被其他 App 占用");
            if (!userRequestedDisconnect) scheduleReconnect();
        }
    };
    private final Runnable serviceDiscoveryTimeoutRunnable = () -> {
        if (connected && bluetoothGatt != null && !servicesDiscovered) {
            lastBluetoothDiagnostic = "连接已建立，但设备服务发现超时；设备可能被其他 App 占用或需要认证";
            toast("已连接，但读取设备服务超时；请先关闭小米运动健康等其他设备管理 App，再重新连接");
            try { bluetoothGatt.disconnect(); } catch (Exception ignored) { }
        }
    };

    private String manufacturer = "未知";
    private String model = "未知";
    private String firmware = "未知";

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    // GATT 通道通常一次只可靠处理一个异步操作。把读取、写入和通知订阅排队，
    // 避免连续 read/write/descriptor 操作互相覆盖，导致部分手环永远收不到数据。
    private interface GattOperation {
        boolean start(BluetoothGatt gatt);
    }
    private final ArrayDeque<GattOperation> gattOperationQueue = new ArrayDeque<>();
    private boolean gattOperationRunning = false;
    private BluetoothGatt activeOperationGatt = null;
    private int gattOperationGeneration = 0;

    private final Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (!connected && !userRequestedDisconnect && connectedAddress != null && !connectedAddress.isEmpty()) {
                autoConnect();
            }
        }
    };

    private final Runnable batteryPollRunnable = new Runnable() {
        @Override
        public void run() {
            if (connected && bluetoothGatt != null) {
                requestBatteryLevel(bluetoothGatt);
                try { bluetoothGatt.readRemoteRssi(); } catch (Exception ignored) { }
                long nextDelay = preferences != null && preferences.getBoolean(KEY_LOW_POWER_MONITOR, true) ? 60000L : 10000L;
                handler.postDelayed(this, nextDelay);
            }
        }
    };

    // 开发者模式：随机模拟心率，同一个随机数同时喂给实时心率、心率趋势和心电图
    private int devHeartRate = 75;
    private final java.util.Random devRandom = new java.util.Random();
    private final Runnable devSimRunnable = new Runnable() {
        @Override
        public void run() {
            if (preferences == null || !preferences.getBoolean(KEY_DEVELOPER, false)) return;
            if (!connected) {
                devHeartRate = Math.max(50, Math.min(130, devHeartRate + devRandom.nextInt(17) - 8));
                updateHeartRate(devHeartRate);
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void startDevSim() {
        handler.removeCallbacks(devSimRunnable);
        handler.post(devSimRunnable);
    }

    private void stopDevSim() {
        handler.removeCallbacks(devSimRunnable);
    }

    private final Runnable batteryFallbackRunnable = new Runnable() {
        @Override
        public void run() {
            if (connected && currentBattery < 0) {
                runOnUiThread(() -> updateStatusViews());
            }
        }
    };

    // 最近记录每 10 秒自动刷新一次
    private final Runnable recentHistoryRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentPage == PAGE_DATA && historyContainer != null) {
                renderHistoryRecords();
                refreshDataRange();
            }
            handler.postDelayed(this, 10000);
        }
    };

    private final List<BluetoothDevice> discoveredDevices =
            new ArrayList<>();

    private final Map<String, Integer> discoveredRssi =
            new HashMap<>();
    private final Map<String, String> discoveredNames =
            new HashMap<>();

    private String serviceDumpText =
            "";
    private final LinkedHashMap<String, String> latestBlePackets = new LinkedHashMap<>();
    private volatile int receivedBlePacketCount = 0;

    // =========================================================
    // Preferences
    // =========================================================

    private SharedPreferences preferences;

    private static final String PREFS =
            "app_preferences";

    private static final String KEY_DEVICE_ADDRESS =
            "device_address";

    private static final String KEY_DEVICE_NAME =
            "device_name";
    private static final String KEY_SAVED_DEVICE_ADDRESSES = "saved_device_addresses";
    private static final int REQUEST_IMPORT_CSV = 6501;
    private static final int REQUEST_EXPORT_CSV = 6502;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1003;

    private static final String KEY_THEME =
            "theme";

    private static final String KEY_ACCENT_COLOR = "accent_color";
    private static final String KEY_DYNAMIC_COLOR = "dynamic_color";
    private static final String KEY_DEVELOPER = "developer_mode";
    private static final String KEY_SHOW_DEVELOPER = "show_developer_features";
    private static final String KEY_STARTUP_ANIMATION = "startup_animation";
    private static final String KEY_TOUCH_FEEDBACK = "touch_feedback";
    private static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
    private static final String KEY_STARTUP_DIRECTION = "startup_animation_direction";
    private static final String KEY_GLASS_OPACITY = "glass_opacity";
    private static final String KEY_WALLPAPER_PANEL_OPACITY = "wallpaper_panel_opacity";
    private static final String KEY_GLASS_HIGHLIGHT = "glass_highlight";
    private static final String KEY_GLASS_RADIUS = "glass_radius_scale";
    private static final String KEY_GLASS_REFRACTION = "glass_refraction";
    private static final String KEY_GLASS_TINT = "glass_tint";
    private static final String KEY_THEME_CUSTOM = "theme_custom";
    private static final String KEY_FLOATING_BAR = "floating_bottom_bar";
    private static final String KEY_NAV_BAR_OFFSET_X = "floating_bottom_bar_offset_x_dp";
    private static final String KEY_NAV_BAR_OFFSET_Y = "floating_bottom_bar_offset_y_dp";
    private static final String KEY_LIQUID_GLASS = "liquid_glass";
    // Legacy key retained for a one-time migration from the old selector glass switch.
    private static final String KEY_LIQUID_GLASS_DIALOG = "liquid_glass_dialog";
    private static final String KEY_LIQUID_GLASS_NAV_BAR = "liquid_glass_navigation_bar";
    private static final String KEY_CUSTOM_BACKGROUND_URI = "custom_background_uri";
    private static final String KEY_USE_SYSTEM_WALLPAPER = "use_system_wallpaper";
    private static final String KEY_DESKTOP_WALLPAPER_URI = "desktop_wallpaper_image_uri";
    private static final String KEY_SYSTEM_WALLPAPER_ID = "system_wallpaper_id";
    private static final String KEY_WALLPAPER_ACCESS_PENDING = "wallpaper_all_files_access_pending";
    private static final String DESKTOP_WALLPAPER_CACHE_FILE = "desktop_wallpaper_snapshot.jpg";
    private static final int REQUEST_CUSTOM_BACKGROUND = 6204;
    private static final int REQUEST_DESKTOP_WALLPAPER_IMAGE = 6205;
    private static final int REQUEST_WALLPAPER_PERMISSION = 6206;
    private static final int REQUEST_WALLPAPER_ALL_FILES_ACCESS = 6207;
    private static final String KEY_PAGE_SCALE = "page_scale";
    private static final String KEY_REOPEN_PAGE = "reopen_page";
    private static final int DEFAULT_ACCENT = 0xFF3B82F6;
    private static final int[] KEY_COLOR_VALUES = {
            0, 0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5,
            0xFF2196F3, 0xFF00BCD4, 0xFF009688, 0xFF4FAF50, 0xFFFFEB3B,
            0xFFFFC107, 0xFFFF9800, 0xFF795548, 0xFF607D8F, 0xFFFF9CA8
    };
    private static final String[] KEY_COLOR_NAMES = {
            "默认", "红色", "粉色", "紫色", "深紫", "靛蓝", "蓝色", "青色",
            "蓝绿", "绿色", "黄色", "琥珀", "橙色", "棕色", "蓝灰", "樱花"
    };
    private static final String[] STYLE_KEYS = {"TonalSpot", "Neutral", "Vibrant", "Expressive", "Rainbow", "FruitSalad", "Monochrome", "Fidelity", "Content"};
    private float pageScale = 1f;
    private static final String KEY_PALETTE_STYLE = "palette_style";
    private static final String KEY_COLOR_SPEC = "color_spec";

    private static final String KEY_MONITOR =
            "monitor";
    private static final String KEY_HEART_ALERTS = "heart_rate_alerts_enabled";
    private static final String KEY_LOW_POWER_MONITOR = "low_power_monitor";
    private static final String KEY_LAST_HEART_ALERT_AT = "heart_rate_alert_last_at";
    private static final String KEY_LAST_HEART = "monitor_last_hr";

    private static final String KEY_OVERLAY =
            "overlay";

    private static final String KEY_OVERLAY_SIZE =
            "overlay_size";
    private static final String KEY_OVERLAY_SIZE_MIGRATION =
            "overlay_size_smaller_v2_migrated";

    private static final String KEY_OVERLAY_ALPHA =
            "overlay_alpha";

    private static final String KEY_OVERLAY_LOCKED =
            "overlay_locked";

    private static final String ACTION_OVERLAY_SETTINGS =
            "com.shuangnian.UPDATE_OVERLAY_SETTINGS";

    // =========================================================
    // 广播
    // =========================================================

    private final BroadcastReceiver heartReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(
                        Context context,
                        Intent intent
                ) {

                    if (intent == null) {
                        return;
                    }

                    if (HeartControlWidgetProvider.ACTION_CONTROL.equals(intent.getAction())) {
                        boolean enabled = intent.getBooleanExtra("enabled", false);
                        if (enabled) {
                            preferences.edit().putBoolean(HeartControlWidgetProvider.KEY_WIDGET_PENDING_START, true).apply();
                            boolean activityActuallyVisible = preferences.getBoolean(KEY_MAIN_ACTIVITY_VISIBLE, false)
                                    && System.currentTimeMillis() - preferences.getLong(KEY_MAIN_ACTIVITY_LAST_SEEN, 0L) < 12000L;
                            // A stopped Activity also keeps its receiver registered. When it is not visible,
                            // let HeartMonitoringService own BLE so the widget does not create a duplicate GATT.
                            if (activityActuallyVisible) {
                                if (connected) {
                                    preferences.edit().putBoolean(HeartControlWidgetProvider.KEY_WIDGET_PENDING_START, false).apply();
                                    startMonitoring();
                                } else if (hasBluetoothConnectPermission()) {
                                    if (shouldUseBackgroundBleService()) ensureBackgroundBleService();
                                    else autoConnect();
                                }
                            }
                        } else {
                            preferences.edit().putBoolean(HeartControlWidgetProvider.KEY_WIDGET_PENDING_START, false).apply();
                            if (monitoring || preferences.getBoolean(KEY_MONITOR, false)) stopMonitoring();
                            stopOverlayService();
                        }
                        HeartControlWidgetProvider.refreshWidgets(context);
                        return;
                    }

                    if ("com.shuangnian.DEVICE_STATUS".equals(intent.getAction())) {
                        connected = intent.getBooleanExtra(HeartMonitoringService.EXTRA_CONNECTED, connected);
                        String serviceName = intent.getStringExtra(HeartMonitoringService.EXTRA_DEVICE_NAME);
                        if (serviceName != null && !serviceName.trim().isEmpty() && connected) {
                            connectedName = serviceName;
                            connectedAddress = preferences.getString(KEY_DEVICE_ADDRESS, connectedAddress);
                        }
                        if (!connected) {
                            servicesDiscovered = false;
                        }
                        int serviceHeart = intent.getIntExtra(HeartMonitoringService.EXTRA_HEART_RATE, 0);
                        if (serviceHeart > 0) updateHeartRateFromService(serviceHeart);
                        refreshDevicePage();
                        updateStatusViews();
                        HeartControlWidgetProvider.refreshWidgets(context);
                    }
                }
            };

    // Activity 可见状态用于在前后台之间交接 BLE GATT 所有权，避免同一设备被两个连接同时占用。
    private static final String KEY_MAIN_ACTIVITY_VISIBLE = "main_activity_visible";
    private static final String KEY_MAIN_ACTIVITY_LAST_SEEN = "main_activity_last_seen";
    private static final String KEY_ACTIVITY_GATT_LAST_SEEN = "activity_gatt_owner_last_seen";
    private boolean hasStoppedOnce = false;

    private boolean shouldUseBackgroundBleService() {
        return preferences != null && (preferences.getBoolean(KEY_MONITOR, false)
                || preferences.getBoolean(KEY_OVERLAY, false));
    }

    private void ensureBackgroundBleService() {
        if (!shouldUseBackgroundBleService()) return;
        Intent service = new Intent(this, HeartMonitoringService.class)
                .setAction(HeartMonitoringService.ACTION_START)
                .putExtra(HeartMonitoringService.EXTRA_HEART_RATE, currentHeartRate)
                .putExtra(HeartMonitoringService.EXTRA_DEVICE_NAME, connected ? connectedName
                        : preferences.getString(KEY_DEVICE_NAME, connectedName))
                .putExtra(HeartMonitoringService.EXTRA_CONNECTED, connected);
        try {
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(service); else startService(service);
        } catch (Exception ignored) { }
    }

    private final Runnable activityVisibleHeartbeat = new Runnable() {
        @Override public void run() {
            if (preferences == null || isFinishing() || isDestroyed()) return;
            preferences.edit().putBoolean(KEY_MAIN_ACTIVITY_VISIBLE, true)
                    .putLong(KEY_MAIN_ACTIVITY_LAST_SEEN, System.currentTimeMillis()).apply();
            handler.postDelayed(this, 4000L);
        }
    };
    // Lets the foreground service avoid opening a duplicate GATT while the Activity-owned link
    // is still alive in the background. If the process/Activity dies, this lease naturally expires.
    private final Runnable activityGattOwnerHeartbeat = new Runnable() {
        @Override public void run() {
            if (preferences == null || isFinishing() || isDestroyed()) return;
            long ownerTimestamp = bluetoothGatt != null ? System.currentTimeMillis() : 0L;
            preferences.edit().putLong(KEY_ACTIVITY_GATT_LAST_SEEN, ownerTimestamp).apply();
            handler.postDelayed(this, 3500L);
        }
    };

    // =========================================================
    // Activity
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        preferences =
                getSharedPreferences(
                        PREFS,
                        MODE_PRIVATE
                );
        if (!preferences.contains(KEY_LIQUID_GLASS_NAV_BAR)) {
            boolean oldIndependentValue = preferences.getBoolean(KEY_LIQUID_GLASS_DIALOG, true);
            preferences.edit().putBoolean(KEY_LIQUID_GLASS_NAV_BAR, oldIndependentValue).apply();
        }
        preferences.edit().putBoolean(KEY_MAIN_ACTIVITY_VISIBLE, true)
                .putLong(KEY_MAIN_ACTIVITY_LAST_SEEN, System.currentTimeMillis()).apply();
        preferences.edit().putLong(KEY_ACTIVITY_GATT_LAST_SEEN, 0L).apply();
        handler.removeCallbacks(activityGattOwnerHeartbeat);
        handler.postDelayed(activityGattOwnerHeartbeat, 1000L);

        pageScale = Math.max(0.8f, Math.min(1.1f, preferences.getFloat(KEY_PAGE_SCALE, 1f)));
        migrateOverlaySizePreference();
        // 清理旧版本留下的未使用认证字段；当前版本不再读取或展示该字段。
        preferences.edit().remove("device_authkey").apply();
        monitoring = preferences.getBoolean(KEY_MONITOR, false);
        // Preserve the foreground service's real GATT state when it owns BLE.
        if (!shouldUseBackgroundBleService()) {
            preferences.edit().putBoolean(HeartControlWidgetProvider.KEY_WIDGET_CONNECTED, false).apply();
        }
        HeartControlWidgetProvider.refreshWidgets(this);

        loadTheme();

        BluetoothManager bluetoothManager =
                (BluetoothManager)
                        getSystemService(
                                BLUETOOTH_SERVICE
                        );

        if (bluetoothManager != null) {
            bluetoothAdapter =
                    bluetoothManager.getAdapter();
        }

        buildInterface();

        IntentFilter filter =
                new IntentFilter();

        // 实时心率只接受本应用的 BLE GATT 直连数据，不再通过 HEART_RATE 广播转发。
        // 保留设备状态广播，避免破坏现有设备页刷新功能。
        filter.addAction("com.shuangnian.DEVICE_STATUS");
        filter.addAction(HeartControlWidgetProvider.ACTION_CONTROL);

        if (Build.VERSION.SDK_INT >= 33) {

            registerReceiver(
                    heartReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
            );

        } else {

            registerReceiver(
                    heartReceiver,
                    filter
            );
        }

        sendBroadcast(new Intent(HeartMonitoringService.ACTION_ACTIVITY_VISIBLE).setPackage(getPackageName()));
        requestRequiredPermissions();

        // 如果用户此前开启了后台监测，启动时恢复可见的前台监测状态；设备在线状态仍以真实 GATT 回调为准。
        monitoring = preferences.getBoolean(KEY_MONITOR, false);

        // The foreground service remains the single BLE owner whenever monitoring/overlay is active.
        if (shouldUseBackgroundBleService()) {
            ensureBackgroundBleService();
        } else if (hasBluetoothConnectPermission()) {
            autoConnect();
        }
        HeartControlWidgetProvider.refreshWidgets(this);

        // 更新桌面组件的配色，并在本次功能新增公告尚未展示时显示一次。
        HeartControlWidgetProvider.refreshWidgets(this);
        handler.postDelayed(this::showAddedFeaturesAnnouncementOnce, 2800L);

        // 悬浮窗仅由用户主动开启；页面创建/重建不自动弹出悬浮窗。
    }

    private static final String KEY_ADDED_FEATURES_ANNOUNCEMENT_SHOWN =
            "announcement_added_features_20261024_shown";

    /** 只介绍新增能力，不把样式调整、缺陷修复等小改动塞进公告；使用不透明原生弹窗。 */
    private void showAddedFeaturesAnnouncementOnce() {
        if (isFinishing() || isDestroyed() || preferences == null
                || preferences.getBoolean(KEY_ADDED_FEATURES_ANNOUNCEMENT_SHOWN, false)) return;

        // Set the flag before opening the dialog so Activity recreation cannot show it twice.
        preferences.edit().putBoolean(KEY_ADDED_FEATURES_ANNOUNCEMENT_SHOWN, true).apply();
        String message =
                "• 桌面快捷组件：一键控制监测功能，查看当前心率和近期趋势\n\n"
              + "• 自定义背景图片：导入图片作为应用页面背景\n\n"
              + "• 启动动画设置：可在主题设置中自主开启或关闭\n\n"
              + "• 独立液态玻璃控制：主题页面与底部切换栏可分别设置\n\n"
              + "• 隐藏开发者功能：提供数据管理工具，默认隐藏";
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("近期新增功能")
                .setMessage(message)
                .setPositiveButton("知道了", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            Window window = dialog.getWindow();
            if (window != null) {
                GradientDrawable surface = new GradientDrawable();
                surface.setColor(applyWallpaperPanelOpacity(darkMode ? 0xFF20262E : 0xFFF7F9FC));
                surface.setCornerRadius(dp(22));
                window.setBackgroundDrawable(surface);
            }
            int body = darkMode ? 0xFFE8EEF5 : 0xFF243647;
            int heading = darkMode ? 0xFFF5F8FC : 0xFF172B3D;
            try {
                int messageId = getResources().getIdentifier("message", "id", "android");
                View bodyView = dialog.findViewById(messageId);
                if (bodyView instanceof TextView) ((TextView) bodyView).setTextColor(body);
                int titleId = getResources().getIdentifier("alertTitle", "id", "android");
                View titleView = dialog.findViewById(titleId);
                if (titleView instanceof TextView) ((TextView) titleView).setTextColor(heading);
            } catch (Exception ignoredColor) { }
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                toast("系统通知权限未开启，心率记录不受影响，但范围提醒无法弹出");
            }
            return;
        }
        if (requestCode == REQUEST_WALLPAPER_PERMISSION) {
            if (Build.VERSION.SDK_INT < 33 && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readDesktopWallpaperSnapshot();
            } else {
                if (!hasSavedDesktopWallpaperImage() && !isSystemWallpaperEnabled()) {
                    preferences.edit().putBoolean(KEY_USE_SYSTEM_WALLPAPER, false).apply();
                }
                recreateTo(PAGE_THEME);
                toast("未获得读取壁纸所需的权限，已保留原背景");
            }
            return;
        }
        if (requestCode == 1001) {
            if (scanAfterPermission) {
                if (hasBluetoothPermission()) {
                    handler.postDelayed(() -> {
                        scanAfterPermission = false;
                        startDeviceScan();
                    }, 250);
                } else {
                    scanAfterPermission = false;
                    lastBluetoothDiagnostic = "扫描权限未完整授予：需要附近设备和位置权限来发现 BLE 手环";
                    toast("扫描失败：请在应用权限中允许‘附近设备’和‘位置信息’后重试");
                }
            } else if (hasBluetoothConnectPermission()) {
                // 已知地址的 BLE GATT 直连只需要蓝牙连接权限；不要因为用户没授权定位就阻断直连。
                handler.postDelayed(() -> {
                    if (shouldUseBackgroundBleService()) ensureBackgroundBleService();
                    else autoConnect();
                }, 250);
            } else {
                lastBluetoothDiagnostic = "缺少蓝牙连接权限，无法建立 GATT 直连";
                toast("蓝牙直连失败：请允许‘附近设备’权限");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (preferences != null) {
            preferences.edit().putBoolean(KEY_MAIN_ACTIVITY_VISIBLE, true)
                    .putLong(KEY_MAIN_ACTIVITY_LAST_SEEN, System.currentTimeMillis()).apply();
            handler.removeCallbacks(activityVisibleHeartbeat);
            handler.postDelayed(activityVisibleHeartbeat, 4000L);
            sendBroadcast(new Intent(HeartMonitoringService.ACTION_ACTIVITY_VISIBLE).setPackage(getPackageName()));
        }
        if (shouldUseBackgroundBleService()) {
            ensureBackgroundBleService();
        } else if (hasStoppedOnce) {
            handler.postDelayed(() -> {
                if (!isFinishing() && bluetoothGatt == null && hasBluetoothConnectPermission()) autoConnect();
            }, 450L);
        }
        hasStoppedOnce = false;
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(activityVisibleHeartbeat);
        boolean keepFeaturesRunning = preferences != null
                && (preferences.getBoolean(KEY_MONITOR, false) || preferences.getBoolean(KEY_OVERLAY, false));
        if (preferences != null) {
            preferences.edit().putBoolean(KEY_MAIN_ACTIVITY_VISIBLE, false)
                    .putLong(KEY_MAIN_ACTIVITY_LAST_SEEN, 0L).apply();
        }
        // Keep the same Activity-owned GATT on ordinary background transitions. The service keeps
        // its foreground notification alive, but will only take over if this GATT-owner heartbeat expires.
        stopScan();
        if (keepFeaturesRunning) {
            Intent monitor = new Intent(this, HeartMonitoringService.class).setAction(HeartMonitoringService.ACTION_START)
                    .putExtra(HeartMonitoringService.EXTRA_HEART_RATE, currentHeartRate)
                    .putExtra(HeartMonitoringService.EXTRA_DEVICE_NAME, connectedName)
                    .putExtra(HeartMonitoringService.EXTRA_CONNECTED, connected);
            try {
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(monitor); else startService(monitor);
            } catch (Exception ignored) { }
        } else {
            // No foreground monitoring feature is requested; retain the live GATT only while this
            // Activity/process remains alive. Normal Activity destruction closes it and a fresh launch reconnects.
            try { stopService(new Intent(this, HeartMonitoringService.class)); } catch (Exception ignored) { }
        }
        hasStoppedOnce = true;
        super.onStop();
    }

    @Override
    protected void onPause() {
        activityResumed = false;
        pauseDesktopWallpaperVideo();
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        activityResumed = true;
        resumeDesktopWallpaperVideo();

        int previousBackgroundColor = backgroundColor;
        int previousCardColor = cardColor;
        int previousAccentColor = accentColor;
        boolean previousDarkMode = darkMode;
        loadTheme();
        if (previousDarkMode != darkMode || previousBackgroundColor != backgroundColor
                || previousCardColor != cardColor || previousAccentColor != accentColor) {
            recreateTo(currentPage);
        } else {
            updateBottomNavigation();
        }

        handler.removeCallbacks(recentHistoryRefreshRunnable);
        handler.post(recentHistoryRefreshRunnable);
        HeartControlWidgetProvider.refreshWidgets(this);
        if (preferences != null && preferences.getBoolean(KEY_WALLPAPER_ACCESS_PENDING, false)) {
            // 从系统“所有文件访问”设置页返回后继续读取，不再弹出图片选择器。
            handler.post(this::completeWallpaperAccessRequest);
        }
        if (isSystemWallpaperEnabled()) {
            applySystemBars();
            applyCustomBackground();
            handler.postDelayed(this::refreshSystemWallpaperIfChanged, 220L);
        }

        if (preferences != null && preferences.getBoolean(KEY_DEVELOPER, false)) startDevSim();

        // 不在 onResume 自动启动悬浮窗，避免打开设置/选择项时反复弹出。
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(activityGattOwnerHeartbeat);
        if (preferences != null) preferences.edit().putLong(KEY_ACTIVITY_GATT_LAST_SEEN, 0L).apply();
        handler.removeCallbacks(recentHistoryRefreshRunnable);
        handler.removeCallbacks(adjacentPreviewPreloadRunnable);
        clearSwipePagePreviewCache();
        stopDevSim();
        stopDesktopWallpaperVideo();

        super.onDestroy();

        try {
            unregisterReceiver(
                    heartReceiver
            );
        } catch (Exception ignored) {
        }

        stopScan();
        boolean keepBackgroundMonitoring = monitoring && preferences != null
                && preferences.getBoolean(KEY_MONITOR, false);
        if (!keepBackgroundMonitoring) {
            handler.removeCallbacks(reconnectRunnable);
            handler.removeCallbacks(batteryPollRunnable);
            handler.removeCallbacks(connectTimeoutRunnable);
            handler.removeCallbacks(serviceDiscoveryTimeoutRunnable);
            resetGattOperationQueue();
            if (bluetoothGatt != null) {
                try { bluetoothGatt.close(); } catch (Exception ignored) { }
                bluetoothGatt = null;
            }
        }
    }

    // =========================================================
    // 主题
    // =========================================================

    private void loadTheme() {

        String theme =
                preferences.getString(
                        KEY_THEME,
                        "system"
                );

        if ("dark".equals(theme)) {

            darkMode = true;

        } else if ("light".equals(theme)) {

            darkMode = false;

        } else {

            int mode =
                    getResources()
                            .getConfiguration()
                            .uiMode
                            &
                            android.content.res.Configuration
                                    .UI_MODE_NIGHT_MASK;

            darkMode =
                    mode ==
                            android.content.res.Configuration
                                    .UI_MODE_NIGHT_YES;
        }

        applyColors();
        applySystemBars();
    }

    private void applyColors() {
        String theme = preferences == null
                ? "system"
                : preferences.getString(KEY_THEME, "system");
        int mode = getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        darkMode = "dark".equals(theme)
                || "amoled".equals(theme)
                || "midnight".equals(theme)
                || ("system".equals(theme)
                    && mode == android.content.res.Configuration.UI_MODE_NIGHT_YES);

        boolean custom = isThemeCustom();
        boolean useDynamicColor = preferences != null
                && preferences.getBoolean(KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= 31)
                && Build.VERSION.SDK_INT >= 31;
        int systemAccent = Color.rgb(59, 130, 246);
        if (useDynamicColor && Build.VERSION.SDK_INT >= 31) {
            try {
                int resourceId = getResources().getIdentifier("system_accent1_500", "color", "android");
                if (resourceId != 0) systemAccent = getResources().getColor(resourceId, getTheme());
            } catch (Exception ignored) { }
        }
        int baseAccent = useDynamicColor ? systemAccent
                : (custom && preferences != null
                    ? preferences.getInt(KEY_ACCENT_COLOR, DEFAULT_ACCENT)
                    : DEFAULT_ACCENT);
        String paletteStyle = preferences == null
                ? "TonalSpot"
                : preferences.getString(KEY_PALETTE_STYLE, "TonalSpot");
        float[] hsv = new float[3];
        Color.colorToHSV(baseAccent, hsv);
        switch (paletteStyle) {
            case "Neutral": hsv[1] *= 0.45f; break;
            case "Vibrant": hsv[1] = Math.min(1f, Math.max(0.75f, hsv[1])); break;
            case "Expressive": hsv[0] = (hsv[0] + 35f) % 360f; hsv[1] = Math.max(0.55f, hsv[1]); break;
            case "Rainbow": hsv[0] = (hsv[0] + 120f) % 360f; hsv[1] = Math.max(0.65f, hsv[1]); break;
            case "FruitSalad": hsv[0] = (hsv[0] + 300f) % 360f; hsv[1] = Math.max(0.6f, hsv[1]); break;
            case "Monochrome": hsv[1] = 0f; break;
            case "Fidelity": hsv[1] = Math.min(1f, hsv[1] * 1.08f); break;
            case "Content": hsv[1] *= 0.85f; break;
            default: break; // Tonal Spot
        }
        // 与 KernelSU 一致：新版色彩规范只对 TonalSpot / Neutral / Vibrant / Expressive 生效，其余风格自动用旧版
        String colorSpec = (!custom || preferences == null) ? "SPEC_2025" : preferences.getString(KEY_COLOR_SPEC, "SPEC_2025");
        if ("SPEC_2025".equals(colorSpec) && !supportsSpec2025(paletteStyle)) colorSpec = "SPEC_2021";
        if ("SPEC_2021".equals(colorSpec) && (custom || useDynamicColor)) hsv[1] = Math.min(1f, hsv[1] * 1.04f);
        hsv[2] = darkMode ? 0.88f : 0.58f;
        accentColor = Color.HSVToColor(hsv);

        if ("amoled".equals(theme)) {
            backgroundColor = Color.BLACK;
            cardColor = Color.rgb(12, 12, 12);
            textColor = Color.rgb(245, 245, 245);
            secondaryTextColor = Color.rgb(175, 175, 175);
            dividerColor = Color.rgb(48, 48, 48);
        } else if (darkMode) {
            // KSU-like neutral dark surfaces. Accent color never tints the entire dark theme.
            backgroundColor = Color.rgb(18, 18, 20);
            cardColor = Color.rgb(29, 29, 32);
            textColor = Color.rgb(242, 242, 245);
            secondaryTextColor = Color.rgb(181, 181, 188);
            dividerColor = Color.rgb(54, 54, 59);
        } else {
            backgroundColor = Color.rgb(247, 247, 250);
            cardColor = Color.WHITE;
            textColor = Color.rgb(29, 29, 33);
            secondaryTextColor = Color.rgb(99, 99, 108);
            dividerColor = Color.rgb(228, 228, 234);
        }
    }

    // =========================================================
    // 主界面
    // =========================================================

    /** 根容器在横向手势超过阈值时拦截事件，纵向滑动仍完整交给 ScrollView。 */
    private final class SwipeRoot extends FrameLayout {
        private final int touchSlop = ViewConfiguration.get(MainActivity.this).getScaledTouchSlop();
        private boolean swipeEligible = false;

        SwipeRoot(Context context) {
            super(context);
            setClickable(true);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    handler.removeCallbacks(adjacentPreviewPreloadRunnable);
                    swipeDownX = event.getX();
                    swipeDownY = event.getY();
                    swipeDeltaX = 0f;
                    swipeEligible = currentPage != PAGE_THEME
                            && event.getY() < getHeight() - dp(104)
                            && !isHorizontalGestureReserved(event.getX(), event.getY());
                    return false;
                case MotionEvent.ACTION_MOVE:
                    if (swipeEligible && !swipeInProgress) {
                        float dx = event.getX() - swipeDownX;
                        float dy = event.getY() - swipeDownY;
                        float swipeThreshold = Math.max(touchSlop * 1.8f, dp(18));
                        if (Math.abs(dx) > swipeThreshold && Math.abs(dx) > Math.abs(dy) * 1.45f) {
                            int target = currentPage + (dx < 0 ? 1 : -1);
                            if (target >= PAGE_HOME && target <= PAGE_SETTINGS) {
                                swipeOriginalPage = currentPage;
                                swipeTargetPage = target;
                                swipeInProgress = true;
                                Bitmap rendered = getOrCreateSwipePagePreview(target);
                                if (rendered == null) {
                                    swipeInProgress = false;
                                    swipeTargetPage = -1;
                                    return false;
                                }
                                // 页面预览由缓存持有；滑动期间只移动 View，不在每一帧重绘整页。
                                swipePreviewBitmap = rendered;
                                if (swipePreviewImage != null) {
                                    swipePreviewImage.setImageBitmap(rendered);
                                    swipePreviewImage.setVisibility(View.VISIBLE);
                                    swipePreviewImage.setAlpha(1f);
                                    swipePreviewImage.setTranslationX(target > swipeOriginalPage ? getWidth() : -getWidth());
                                    swipePreviewImage.bringToFront();
                                    // 当前内容必须位于目标页面预览上方，滑动离开后才会逐渐露出预览。
                                    if (contentScrollView != null) contentScrollView.bringToFront();
                                    if (navigationShell != null) navigationShell.bringToFront();
                                }
                                swipeEligible = false;
                                return true;
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    swipeEligible = false;
                    if (!swipeInProgress && currentPage <= PAGE_SETTINGS) {
                        handler.removeCallbacks(adjacentPreviewPreloadRunnable);
                        handler.postDelayed(adjacentPreviewPreloadRunnable, 700L);
                    }
                    break;
            }
            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (!swipeInProgress) return super.onTouchEvent(event);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_MOVE:
                    onSwipeProgress(event.getX());
                    return true;
                case MotionEvent.ACTION_UP:
                    onSwipeProgress(event.getX());
                    finishSwipe(true);
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    finishSwipe(false);
                    return true;
                default:
                    return true;
            }
        }
    }

    /** 避免页面横向手势抢走 SeekBar、输入框、开关等控件本身的触摸操作。 */
    private boolean isHorizontalGestureReserved(float rootX, float rootY) {
        if (contentScrollView == null) return false;
        float localX = rootX - contentScrollView.getLeft();
        float localY = rootY - contentScrollView.getTop();
        return containsHorizontalInteractiveControl(contentScrollView, localX, localY);
    }

    private boolean containsHorizontalInteractiveControl(View view, float x, float y) {
        if (view == null || view.getVisibility() != View.VISIBLE
                || x < 0 || y < 0 || x >= view.getWidth() || y >= view.getHeight()) return false;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                float childX = x - child.getLeft() + group.getScrollX() - child.getTranslationX();
                float childY = y - child.getTop() + group.getScrollY() - child.getTranslationY();
                if (containsHorizontalInteractiveControl(child, childX, childY)) return true;
            }
        }
        return view instanceof SeekBar
                || view instanceof HorizontalScrollView
                || view instanceof CompoundButton
                || view instanceof EditText
                || view instanceof Button;
    }

    private void applySystemBars() {
        Window window = getWindow();
        if (window == null) return;
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 不再让系统把桌面壁纸作为 Activity 窗口背景。壁纸图片由应用自己的 ImageView 绘制，
        // 因此状态栏、页面滚动和底部导航不再依赖 FLAG_SHOW_WALLPAPER 的窗口合成行为。
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        boolean imageBackground = shouldDrawImageBehindStatusBar();
        window.setStatusBarColor(imageBackground ? Color.TRANSPARENT : backgroundColor);
        window.setNavigationBarColor(backgroundColor);
        if (Build.VERSION.SDK_INT >= 29) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (imageBackground) {
            // 状态栏仍然显示，只让图片延伸到它后面；页面内容会额外留出状态栏高度。
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        }
        if (!darkMode && Build.VERSION.SDK_INT >= 23) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        if (!darkMode && Build.VERSION.SDK_INT >= 26) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        window.getDecorView().setSystemUiVisibility(flags);
    }

    private boolean shouldDrawImageBehindStatusBar() {
        if (isSystemWallpaperEnabled()) return true;
        if (preferences == null) return false;
        String uri = preferences.getString(KEY_CUSTOM_BACKGROUND_URI, null);
        return uri != null && !uri.trim().isEmpty();
    }

    private int getStatusBarHeight() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : dp(24);
    }

    private void buildInterface() {

        applySystemBars();

        handler.removeCallbacks(adjacentPreviewPreloadRunnable);
        clearSwipePreview();
        clearSwipePagePreviewCache();
        glassBarShell = null;
        navigationShell = null;

        root = new SwipeRoot(this);
        root.setBackgroundColor(backgroundColor);
        // Child animations and button scale feedback must stay within the visible screen bounds.
        root.setClipChildren(true);
        root.setClipToPadding(true);
        setContentView(root);

        // 壁纸/自定义图片位于页面内容下方；不叠加整屏黑雾，卡片自身保持内容对比度。
        customBackgroundView = new ImageView(this);
        customBackgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        customBackgroundView.setFocusable(false);
        root.addView(customBackgroundView, new FrameLayout.LayoutParams(-1, -1));
        final TextureView videoBackgroundLayer = new TextureView(this);
        desktopWallpaperVideoView = videoBackgroundLayer;
        videoBackgroundLayer.setOpaque(false);
        videoBackgroundLayer.setVisibility(View.GONE);
        videoBackgroundLayer.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (desktopWallpaperVideoView == videoBackgroundLayer && desktopWallpaperVideoPendingUri != null) {
                    prepareDesktopWallpaperVideo(desktopWallpaperVideoPendingUri, surface);
                }
            }
            @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }
            @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (desktopWallpaperVideoView == videoBackgroundLayer) releaseDesktopWallpaperVideo();
                return true;
            }
            @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
        });
        root.addView(videoBackgroundLayer, new FrameLayout.LayoutParams(-1, -1));
        backgroundScrim = new View(this);
        backgroundScrim.setBackgroundColor(Color.TRANSPARENT);
        backgroundScrim.setVisibility(View.GONE);
        root.addView(backgroundScrim, new FrameLayout.LayoutParams(-1, -1));
        swipePreviewImage = new ImageView(this);
        swipePreviewImage.setScaleType(ImageView.ScaleType.FIT_XY);
        swipePreviewImage.setVisibility(View.GONE);
        swipePreviewImage.setClickable(false);
        root.addView(swipePreviewImage, new FrameLayout.LayoutParams(-1, -1));
        applyCustomBackground();

        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setClipChildren(true);
        contentContainer.setClipToPadding(true);
        // 底栏覆盖在内容之上，额外留出滚动空间，末尾项目不会被玻璃导航遮住。
        contentContainer.setPadding(dp(18), dp(18) + (shouldDrawImageBehindStatusBar() ? getStatusBarHeight() : 0), dp(18), dp(126));

        contentScrollView = new ScrollView(this);
        contentScrollView.setFillViewport(true);
        contentScrollView.setClipToPadding(false);
        contentScrollView.setBackgroundColor(Color.TRANSPARENT);
        contentScrollView.addView(contentContainer, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(contentScrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        contentScrollView.setOnScrollChangeListener((View v, int scrollX, int scrollY,
                int oldScrollX, int oldScrollY) -> {
            if (currentPage >= PAGE_HOME && currentPage <= PAGE_THEME) {
                pageScrollPositions[currentPage] = scrollY;
            }
        });

        buildBottomNavigation();

        int reopen = preferences.getInt(KEY_REOPEN_PAGE, -1);
        if (reopen >= 0) {
            preferences.edit().remove(KEY_REOPEN_PAGE).apply();
            showPage(reopen);
        } else {
            showPage(PAGE_HOME);
        }
        
        // 仅首次构建真实界面时播放卡片弹入；设置项刷新不重放动画，避免闪屏。
        boolean isFirstBuild = !firstInterfaceBuilt;
        firstInterfaceBuilt = true;
        if (isFirstBuild && preferences.getBoolean(KEY_STARTUP_ANIMATION, true)) {
            root.post(() -> animateContentEntrance(preferences.getString(KEY_STARTUP_DIRECTION, "up")));
        }
    }

    /** 让首页卡片从用户选择的方向依次入场；仅用于首次启动，不在设置刷新时重播。 */
    private void animateContentEntrance(String direction) {
        if (contentContainer == null || isFinishing() || !preferences.getBoolean(KEY_STARTUP_ANIMATION, true)) return;
        final int count = contentContainer.getChildCount();
        if (count == 0) return;
        final String dir = direction == null ? "up" : direction;
        final float distance = dp(22);
        for (int i = 0; i < count; i++) {
            final View child = contentContainer.getChildAt(i);
            if (child == null) continue;
            child.animate().cancel();
            child.setAlpha(0f);
            child.setTranslationX("left".equals(dir) ? -distance : "right".equals(dir) ? distance : 0f);
            child.setTranslationY("down".equals(dir) ? distance : "up".equals(dir) ? -distance : 0f);
            child.animate().alpha(1f).translationX(0f).translationY(0f)
                    .setStartDelay(Math.min(180L, i * 28L))
                    .setDuration(250L)
                    .setInterpolator(new DecelerateInterpolator(1.45f)).start();
        }
    }

    private String startupDirectionLabel(String direction) {
        if ("down".equals(direction)) return "从下方弹入";
        if ("left".equals(direction)) return "从左侧弹入";
        if ("right".equals(direction)) return "从右侧弹入";
        return "从上方弹入";
    }

    /**
     * 直接读取系统当前桌面壁纸，不让用户从相册中再次选择。
     * Android 13+ 的 WallpaperManager.getDrawable() 需要系统“所有文件访问”特殊权限。
     */
    private boolean isSystemWallpaperEnabled() {
        return preferences != null && preferences.getBoolean(KEY_USE_SYSTEM_WALLPAPER, false);
    }

    private boolean hasSavedDesktopWallpaperImage() {
        if (preferences == null) return false;
        String savedUri = preferences.getString(KEY_DESKTOP_WALLPAPER_URI, null);
        if (savedUri != null && !savedUri.trim().isEmpty()) return true;
        File cache = new File(getFilesDir(), DESKTOP_WALLPAPER_CACHE_FILE);
        return cache.exists() && cache.length() > 0L;
    }

    private boolean hasDirectWallpaperReadAccess() {
        if (Build.VERSION.SDK_INT >= 33) {
            return Environment.isExternalStorageManager();
        }
        return Build.VERSION.SDK_INT < 23
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLegacyWallpaperPermission() {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 33
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WALLPAPER_PERMISSION);
        } else {
            readDesktopWallpaperSnapshot();
        }
    }

    private void beginDesktopWallpaperRead() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (Environment.isExternalStorageManager()) {
                readDesktopWallpaperSnapshot();
            } else {
                showWallpaperAllFilesAccessExplanation();
            }
            return;
        }
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestLegacyWallpaperPermission();
            return;
        }
        readDesktopWallpaperSnapshot();
    }

    private void showWallpaperAllFilesAccessExplanation() {
        new AppDialogBuilder(false)
                .setTitle("需要系统壁纸读取权限")
                .setMessage("Android 13 及以上限制普通应用直接读取当前桌面壁纸。要实现自动读取、不再手动选择图片，需要你在系统设置中单独允许本应用使用“所有文件访问”。\n\n该权限范围较大，可访问共享存储中的文件，不只是壁纸。Google Play 对此权限也有严格限制。只有确认接受后才继续；拒绝后应用会保留原背景。")
                .setPositiveButton("前往系统授权", (dialog, which) -> openWallpaperAllFilesAccessSettings())
                .setNegativeButton("暂不开启", (dialog, which) -> {
                    if (!isSystemWallpaperEnabled()) preferences.edit().putBoolean(KEY_USE_SYSTEM_WALLPAPER, false).apply();
                    recreateTo(PAGE_THEME);
                })
                .setOnCancelListener(dialog -> {
                    if (!isSystemWallpaperEnabled()) preferences.edit().putBoolean(KEY_USE_SYSTEM_WALLPAPER, false).apply();
                    recreateTo(PAGE_THEME);
                })
                .show();
    }

    private void openWallpaperAllFilesAccessSettings() {
        if (Build.VERSION.SDK_INT < 30) {
            readDesktopWallpaperSnapshot();
            return;
        }
        preferences.edit().putBoolean(KEY_WALLPAPER_ACCESS_PENDING, true).apply();
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_WALLPAPER_ALL_FILES_ACCESS);
        } catch (Exception appSpecificUnavailable) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_WALLPAPER_ALL_FILES_ACCESS);
            } catch (Exception unavailable) {
                preferences.edit().putBoolean(KEY_WALLPAPER_ACCESS_PENDING, false).apply();
                toast("无法打开系统存储权限设置");
            }
        }
    }

    private void completeWallpaperAccessRequest() {
        if (preferences == null || !preferences.getBoolean(KEY_WALLPAPER_ACCESS_PENDING, false)) return;
        preferences.edit().putBoolean(KEY_WALLPAPER_ACCESS_PENDING, false).apply();
        if (Build.VERSION.SDK_INT >= 33 && Environment.isExternalStorageManager()) {
            readDesktopWallpaperSnapshot();
        } else {
            if (!isSystemWallpaperEnabled()) preferences.edit().putBoolean(KEY_USE_SYSTEM_WALLPAPER, false).apply();
            recreateTo(PAGE_THEME);
            toast("未授予所有文件访问，未读取桌面壁纸");
        }
    }

    private void refreshSystemWallpaperIfChanged() {
        if (!isSystemWallpaperEnabled() || wallpaperReadInProgress || !hasDirectWallpaperReadAccess()) return;
        try {
            WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
            int currentId = manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM);
            long savedId = preferences.getLong(KEY_SYSTEM_WALLPAPER_ID, Long.MIN_VALUE);
            if (currentId != savedId) {
                readDesktopWallpaperSnapshot();
            }
        } catch (SecurityException ignored) {
            // 权限被用户在系统设置中撤销时保留最后一次成功读取的背景。
        } catch (Exception ignored) { }
    }

    private void readDesktopWallpaperSnapshot() {
        if (wallpaperReadInProgress) return;
        if (!hasDirectWallpaperReadAccess()) {
            if (Build.VERSION.SDK_INT >= 33) showWallpaperAllFilesAccessExplanation();
            else requestLegacyWallpaperPermission();
            return;
        }
        wallpaperReadInProgress = true;
        final boolean wallpaperWasEnabled = isSystemWallpaperEnabled();
        final int fallbackWidth = Math.max(1, getResources().getDisplayMetrics().widthPixels);
        final int fallbackHeight = Math.max(1, getResources().getDisplayMetrics().heightPixels);
        new Thread(() -> {
            Bitmap bitmap = null;
            long wallpaperId = -1L;
            try {
                WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
                wallpaperId = manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM);
                Drawable wallpaper = manager.getDrawable();
                if (wallpaper != null) {
                    int sourceWidth = wallpaper.getIntrinsicWidth() > 0 ? wallpaper.getIntrinsicWidth() : fallbackWidth;
                    int sourceHeight = wallpaper.getIntrinsicHeight() > 0 ? wallpaper.getIntrinsicHeight() : fallbackHeight;
                    float scale = Math.min(1f, 2400f / Math.max(sourceWidth, sourceHeight));
                    int width = Math.max(1, Math.round(sourceWidth * scale));
                    int height = Math.max(1, Math.round(sourceHeight * scale));
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    wallpaper.setBounds(0, 0, width, height);
                    wallpaper.draw(canvas);
                }
            } catch (SecurityException ignored) {
                bitmap = null;
            } catch (Exception ignored) {
                bitmap = null;
            }

            boolean saved = false;
            if (bitmap != null) {
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(new File(getFilesDir(), DESKTOP_WALLPAPER_CACHE_FILE));
                    saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output);
                    output.flush();
                } catch (Exception ignored) {
                    saved = false;
                } finally {
                    try { if (output != null) output.close(); } catch (Exception ignored) { }
                    if (!bitmap.isRecycled()) bitmap.recycle();
                }
            }
            final boolean success = saved;
            final long savedWallpaperId = wallpaperId;
            runOnUiThread(() -> {
                wallpaperReadInProgress = false;
                if (isFinishing() || isDestroyed()) return;
                if (success) {
                    desktopWallpaperBitmap = null;
                    preferences.edit().putBoolean(KEY_USE_SYSTEM_WALLPAPER, true)
                            .putLong(KEY_SYSTEM_WALLPAPER_ID, savedWallpaperId)
                            .remove(KEY_DESKTOP_WALLPAPER_URI).apply();
                    recreateTo(PAGE_THEME);
                    toast("已自动读取当前桌面壁纸");
                } else {
                    if (!wallpaperWasEnabled && !hasSavedDesktopWallpaperImage()) {
                        preferences.edit().putBoolean(KEY_USE_SYSTEM_WALLPAPER, false).apply();
                    }
                    recreateTo(PAGE_THEME);
                    toast("系统未能提供当前壁纸图像；已保留原背景");
                }
            });
        }, "desktop-wallpaper-reader").start();
    }

    private void launchDesktopWallpaperPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_DESKTOP_WALLPAPER_IMAGE);
        } catch (Exception error) {
            recreateTo(PAGE_THEME);
            toast("无法打开照片和视频选择器");
        }
    }



    private void launchBackgroundPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQUEST_CUSTOM_BACKGROUND);
        } catch (Exception error) {
            toast("无法打开图片选择器");
        }
    }

    private void startDesktopWallpaperVideo(Uri uri) {
        if (uri == null || desktopWallpaperVideoView == null) return;
        if (uri.equals(desktopWallpaperVideoUri) && desktopWallpaperVideoPlayer != null) {
            desktopWallpaperVideoView.setVisibility(View.VISIBLE);
            if (activityResumed) {
                try { if (!desktopWallpaperVideoPlayer.isPlaying()) desktopWallpaperVideoPlayer.start(); } catch (Exception ignored) { }
            }
            return;
        }
        desktopWallpaperVideoPendingUri = uri;
        desktopWallpaperVideoView.setVisibility(View.VISIBLE);
        if (desktopWallpaperVideoView.isAvailable()) {
            prepareDesktopWallpaperVideo(uri, desktopWallpaperVideoView.getSurfaceTexture());
        }
    }

    private void prepareDesktopWallpaperVideo(Uri uri, SurfaceTexture texture) {
        if (uri == null || texture == null || desktopWallpaperVideoView == null) return;
        releaseDesktopWallpaperVideo();
        desktopWallpaperVideoPendingUri = uri;
        Surface surface = new Surface(texture);
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(this, uri);
            player.setSurface(surface);
            player.setLooping(true);
            player.setVolume(0f, 0f);
            player.setOnPreparedListener(mp -> {
                if (desktopWallpaperVideoPlayer != mp) return;
                if (activityResumed) {
                    try { mp.start(); } catch (Exception ignored) { }
                }
            });
            player.setOnErrorListener((mp, what, extra) -> {
                if (desktopWallpaperVideoPlayer == mp) {
                    releaseDesktopWallpaperVideo();
                    if (desktopWallpaperVideoView != null) desktopWallpaperVideoView.setVisibility(View.GONE);
                    toast("视频壁纸无法播放，请更换视频文件");
                }
                return true;
            });
            desktopWallpaperVideoSurface = surface;
            desktopWallpaperVideoPlayer = player;
            desktopWallpaperVideoUri = uri;
            player.prepareAsync();
        } catch (Exception error) {
            try { player.release(); } catch (Exception ignored) { }
            try { surface.release(); } catch (Exception ignored) { }
            desktopWallpaperVideoPlayer = null;
            desktopWallpaperVideoSurface = null;
            desktopWallpaperVideoUri = null;
            if (desktopWallpaperVideoView != null) desktopWallpaperVideoView.setVisibility(View.GONE);
            toast("视频壁纸初始化失败");
        }
    }

    private void pauseDesktopWallpaperVideo() {
        if (desktopWallpaperVideoPlayer == null) return;
        try {
            if (desktopWallpaperVideoPlayer.isPlaying()) desktopWallpaperVideoPlayer.pause();
        } catch (Exception ignored) { }
    }

    private void resumeDesktopWallpaperVideo() {
        if (desktopWallpaperVideoPlayer == null || !activityResumed) return;
        try {
            if (!desktopWallpaperVideoPlayer.isPlaying()) desktopWallpaperVideoPlayer.start();
        } catch (Exception ignored) { }
    }

    private void stopDesktopWallpaperVideo() {
        releaseDesktopWallpaperVideo();
        desktopWallpaperVideoPendingUri = null;
        if (desktopWallpaperVideoView != null) desktopWallpaperVideoView.setVisibility(View.GONE);
    }

    private void releaseDesktopWallpaperVideo() {
        MediaPlayer player = desktopWallpaperVideoPlayer;
        desktopWallpaperVideoPlayer = null;
        if (player != null) {
            try { player.setOnPreparedListener(null); } catch (Exception ignored) { }
            try { player.setOnErrorListener(null); } catch (Exception ignored) { }
            try { player.release(); } catch (Exception ignored) { }
        }
        Surface surface = desktopWallpaperVideoSurface;
        desktopWallpaperVideoSurface = null;
        if (surface != null) {
            try { surface.release(); } catch (Exception ignored) { }
        }
        desktopWallpaperVideoUri = null;
    }

    private Bitmap decodeDesktopWallpaperCache() {
        File file = new File(getFilesDir(), DESKTOP_WALLPAPER_CACHE_FILE);
        if (!file.exists() || file.length() <= 0L) return null;
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
        int sample = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / sample > 2400) sample *= 2;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void applyCustomBackground() {
        if (customBackgroundView == null || backgroundScrim == null || preferences == null) return;
        if (isSystemWallpaperEnabled()) {
            String wallpaperUriText = preferences.getString(KEY_DESKTOP_WALLPAPER_URI, null);
            if (wallpaperUriText != null && !wallpaperUriText.trim().isEmpty()) {
                Uri wallpaperUri = Uri.parse(wallpaperUriText);
                if (isVideoMediaUri(wallpaperUri)) {
                    customBackgroundView.setImageDrawable(null);
                    customBackgroundView.setVisibility(View.GONE);
                    backgroundScrim.setVisibility(View.GONE);
                    startDesktopWallpaperVideo(wallpaperUri);
                    return;
                }
                stopDesktopWallpaperVideo();
                Bitmap wallpaperBitmap = desktopWallpaperBitmap;
                if (wallpaperBitmap == null || wallpaperBitmap.isRecycled()) {
                    wallpaperBitmap = decodeWallpaperMediaBitmap(wallpaperUri);
                    desktopWallpaperBitmap = wallpaperBitmap;
                }
                if (wallpaperBitmap != null && !wallpaperBitmap.isRecycled()) {
                    customBackgroundView.setImageBitmap(wallpaperBitmap);
                    customBackgroundView.setVisibility(View.VISIBLE);
                } else {
                    customBackgroundView.setImageDrawable(null);
                    customBackgroundView.setVisibility(View.GONE);
                }
            } else {
                stopDesktopWallpaperVideo();
                Bitmap wallpaperBitmap = desktopWallpaperBitmap;
                if (wallpaperBitmap == null || wallpaperBitmap.isRecycled()) {
                    wallpaperBitmap = decodeDesktopWallpaperCache();
                    desktopWallpaperBitmap = wallpaperBitmap;
                }
                if (wallpaperBitmap != null && !wallpaperBitmap.isRecycled()) {
                    customBackgroundView.setImageBitmap(wallpaperBitmap);
                    customBackgroundView.setVisibility(View.VISIBLE);
                } else {
                    customBackgroundView.setImageDrawable(null);
                    customBackgroundView.setVisibility(View.GONE);
                }
            }
            // 壁纸保持原始亮度，不叠加深色或浅色遮罩。
            backgroundScrim.setVisibility(View.GONE);
            return;
        }
        stopDesktopWallpaperVideo();
        String uriText = preferences.getString(KEY_CUSTOM_BACKGROUND_URI, null);
        if (uriText == null || uriText.trim().isEmpty()) {
            customBackgroundView.setImageDrawable(null);
            customBackgroundView.setVisibility(View.GONE);
            backgroundScrim.setVisibility(View.GONE);
            return;
        }
        Bitmap bitmap = decodeBackgroundBitmap(Uri.parse(uriText));
        if (bitmap == null) {
            customBackgroundView.setImageDrawable(null);
            customBackgroundView.setVisibility(View.GONE);
            backgroundScrim.setVisibility(View.GONE);
            preferences.edit().remove(KEY_CUSTOM_BACKGROUND_URI).apply();
            return;
        }
        customBackgroundView.setImageBitmap(bitmap);
        customBackgroundView.setVisibility(View.VISIBLE);
        // 自定义背景也不再强制压暗；主题文字和卡片自身负责保持可读性。
        backgroundScrim.setVisibility(View.GONE);
    }

    private Bitmap decodeBackgroundBitmap(Uri uri) {
        if (uri == null) return null;
        InputStream boundsStream = null;
        InputStream bitmapStream = null;
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            boundsStream = getContentResolver().openInputStream(uri);
            if (boundsStream == null) return null;
            BitmapFactory.decodeStream(boundsStream, null, bounds);
            boundsStream.close();
            boundsStream = null;
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
            int sample = 1;
            int maxDimension = Math.max(bounds.outWidth, bounds.outHeight);
            while (maxDimension / sample > 2400) sample *= 2;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmapStream = getContentResolver().openInputStream(uri);
            if (bitmapStream == null) return null;
            return BitmapFactory.decodeStream(bitmapStream, null, options);
        } catch (Exception ignored) {
            return null;
        } finally {
            try { if (boundsStream != null) boundsStream.close(); } catch (Exception ignored) { }
            try { if (bitmapStream != null) bitmapStream.close(); } catch (Exception ignored) { }
        }
    }

    private boolean isVideoMediaUri(Uri uri) {
        if (uri == null) return false;
        try {
            String mime = getContentResolver().getType(uri);
            if (mime != null && mime.toLowerCase(Locale.ROOT).startsWith("video/")) return true;
            String text = uri.toString().toLowerCase(Locale.ROOT);
            return text.endsWith(".mp4") || text.endsWith(".m4v") || text.endsWith(".webm")
                    || text.endsWith(".3gp") || text.endsWith(".mkv") || text.endsWith(".mov");
        } catch (Exception ignored) {
            return false;
        }
    }

    /** 视频媒体先抽取首帧做有效性检查；实际背景由 TextureView 静音循环播放。 */
    private Bitmap decodeWallpaperMediaBitmap(Uri uri) {
        if (uri == null) return null;
        if (!isVideoMediaUri(uri)) return decodeBackgroundBitmap(uri);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            Bitmap frame = retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (frame == null) return null;
            int max = Math.max(frame.getWidth(), frame.getHeight());
            if (max <= 2400) return frame;
            float scale = 2400f / max;
            Bitmap scaled = Bitmap.createScaledBitmap(frame,
                    Math.max(1, Math.round(frame.getWidth() * scale)),
                    Math.max(1, Math.round(frame.getHeight() * scale)), true);
            if (scaled != frame && !frame.isRecycled()) frame.recycle();
            return scaled;
        } catch (Exception ignored) {
            return null;
        } finally {
            try { retriever.release(); } catch (Exception ignored) { }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1002) {
            if (resultCode == RESULT_OK) {
                handler.postDelayed(this::startDeviceScan, 350L);
            } else {
                lastBluetoothDiagnostic = "手机蓝牙仍处于关闭状态";
                toast("需要先开启手机蓝牙才能直连手环");
            }
            return;
        }
        if (requestCode == REQUEST_IMPORT_CSV) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) importCsv(data.getData());
            return;
        }
        if (requestCode == REQUEST_EXPORT_CSV) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) saveCsvToUri(data.getData());
            return;
        }
        if (requestCode == REQUEST_WALLPAPER_ALL_FILES_ACCESS) {
            completeWallpaperAccessRequest();
            return;
        }
        if (requestCode == REQUEST_DESKTOP_WALLPAPER_IMAGE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri wallpaperUri = data.getData();
                try {
                    int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(wallpaperUri, flags & Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) { }
                Bitmap testWallpaper = decodeWallpaperMediaBitmap(wallpaperUri);
                if (testWallpaper == null) {
                    recreateTo(PAGE_THEME);
                    toast("这张照片或视频无法读取，请重新选择");
                    return;
                }
                if (!testWallpaper.isRecycled()) testWallpaper.recycle();
                desktopWallpaperBitmap = null;
                preferences.edit().putString(KEY_DESKTOP_WALLPAPER_URI, wallpaperUri.toString())
                        .putBoolean(KEY_USE_SYSTEM_WALLPAPER, true).apply();
                recreateTo(PAGE_THEME);
                toast(isVideoMediaUri(wallpaperUri) ? "视频壁纸已应用，播放时静音循环" : "桌面壁纸图片已应用");
            } else {
                recreateTo(PAGE_THEME);
            }
            return;
        }
        if (requestCode != REQUEST_CUSTOM_BACKGROUND || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, flags & Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
            // 某些图片选择器不提供持久授权；立即应用仍可使用当前授权。
        }
        Bitmap test = decodeBackgroundBitmap(uri);
        if (test == null) {
            toast("这张图片无法读取，请换一张图片");
            return;
        }
        if (!test.isRecycled()) test.recycle();
        preferences.edit().putString(KEY_CUSTOM_BACKGROUND_URI, uri.toString()).apply();
        recreateTo(PAGE_THEME);
        toast("背景图片已应用");
    }

    private Bitmap getOrCreateSwipePagePreview(int page) {
        if (page < PAGE_HOME || page > PAGE_SETTINGS) return null;
        Bitmap cached = swipePagePreviewCache[page];
        if (cached != null && !cached.isRecycled()) return cached;
        Bitmap rendered = renderPagePreview(page);
        if (rendered != null && !rendered.isRecycled()) swipePagePreviewCache[page] = rendered;
        return rendered;
    }

    private Bitmap renderPagePreview(int page) {
        if (root == null || contentScrollView == null || contentContainer == null) return null;
        final int width = contentScrollView.getWidth();
        final int height = contentScrollView.getHeight();
        if (width <= 0 || height <= 0) return null;
        final int previousPage = currentPage;
        final int oldScrollY = contentScrollView.getScrollY();
        final LinearLayout originalContainer = contentContainer;
        final ScrollView originalScroll = contentScrollView;
        final boolean wasRenderingPreview = renderingSwipePreview;
        Bitmap preview = null;
        ScrollView scratchScroll = new ScrollView(this);
        scratchScroll.setFillViewport(true);
        scratchScroll.setClipToPadding(false);
        scratchScroll.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout scratchContainer = new LinearLayout(this);
        scratchContainer.setOrientation(LinearLayout.VERTICAL);
        scratchContainer.setPadding(dp(20), dp(20), dp(20), dp(132));
        scratchScroll.addView(scratchContainer, new ScrollView.LayoutParams(-1, -1));
        renderingSwipePreview = true;
        try {
            contentContainer = scratchContainer;
            contentScrollView = scratchScroll;
            populatePageContent(page);
            scratchScroll.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            scratchScroll.layout(0, 0, width, height);
            // 预览只作为拖动动画纹理，使用 60% 分辨率与透明背景，减少大图分配且保留自定义壁纸。
            int previewWidth = Math.max(1, width * 3 / 5);
            int previewHeight = Math.max(1, height * 3 / 5);
            preview = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(preview);
            canvas.scale(previewWidth / (float) width, previewHeight / (float) height);
            scratchScroll.draw(canvas);
        } catch (Throwable failure) {
            if (preview != null && !preview.isRecycled()) preview.recycle();
            preview = null;
        } finally {
            contentContainer = originalContainer;
            contentScrollView = originalScroll;
            renderingSwipePreview = true;
            // restore visible page and live widget references, then restore its previous scroll offset.
            showPage(previousPage);
            originalScroll.post(() -> originalScroll.scrollTo(0, oldScrollY));
            renderingSwipePreview = wasRenderingPreview;
        }
        return preview;
    }

    private void clearSwipePagePreviewCache() {
        for (int i = 0; i < swipePagePreviewCache.length; i++) {
            Bitmap bitmap = swipePagePreviewCache[i];
            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            swipePagePreviewCache[i] = null;
        }
    }

    private void preloadAdjacentSwipePreviews() {
        if (isFinishing() || root == null || swipeInProgress
                || currentPage < PAGE_HOME || currentPage > PAGE_SETTINGS) return;
        int page = currentPage;
        int next = page + 1;
        int previous = page - 1;
        int candidate = -1;
        if (next <= PAGE_SETTINGS
                && (swipePagePreviewCache[next] == null || swipePagePreviewCache[next].isRecycled())) {
            candidate = next;
        } else if (previous >= PAGE_HOME
                && (swipePagePreviewCache[previous] == null || swipePagePreviewCache[previous].isRecycled())) {
            candidate = previous;
        }
        if (candidate < PAGE_HOME || candidate > PAGE_SETTINGS) return;
        getOrCreateSwipePagePreview(candidate);
        // renderPagePreview 会重建一次当前页的控件引用；留出时间恢复当前滚动位置，再准备另一侧。
        if (currentPage == page && !isFinishing()) {
            handler.removeCallbacks(adjacentPreviewPreloadRunnable);
            handler.postDelayed(adjacentPreviewPreloadRunnable, 700L);
        }
    }

    private void onSwipeProgress(float rawX) {
        if (!swipeInProgress || swipeTargetPage < 0 || contentScrollView == null || swipePreviewImage == null) return;
        float dx = rawX - swipeDownX;
        if (swipeTargetPage > swipeOriginalPage) dx = Math.min(0f, dx);
        else dx = Math.max(0f, dx);
        float width = Math.max(1, contentScrollView.getWidth());
        dx = Math.max(-width, Math.min(width, dx));
        swipeDeltaX = dx;
        contentScrollView.setTranslationX(dx);
        float initial = swipeTargetPage > swipeOriginalPage ? width : -width;
        swipePreviewImage.setTranslationX(initial + dx);
        swipePreviewImage.setAlpha(1f);
        // 让玻璃背板随当前页面位移，并以有上限的频率重采样真实内容，而不是移动一张冻结截图。
    }

    private void finishSwipe(boolean commit) {
        if (!swipeInProgress || contentScrollView == null || swipePreviewImage == null) return;
        final int targetPage = swipeTargetPage;
        final int originalPage = swipeOriginalPage;
        final float width = Math.max(1, contentScrollView.getWidth());
        final boolean shouldCommit = commit && Math.abs(swipeDeltaX) >= width * 0.28f && targetPage >= 0;
        float offscreen = targetPage > originalPage ? -width : width;
        if (shouldCommit) {
            contentScrollView.animate().cancel();
            swipePreviewImage.animate().cancel();
            contentScrollView.animate().translationX(offscreen).setDuration(190L)
                    .setInterpolator(new DecelerateInterpolator(1.15f))
                    .withEndAction(() -> {
                        clearSwipePreview();
                        swipeInProgress = false;
                        swipeTargetPage = -1;
                        suppressNextPageTransitionAnimation = true;
                        showPage(targetPage);
                        if (contentScrollView != null) contentScrollView.setTranslationX(0f);
                                }).start();
            swipePreviewImage.animate().translationX(0f).setDuration(190L)
                    .setInterpolator(new DecelerateInterpolator(1.15f)).start();
        } else {
            contentScrollView.animate().cancel();
            swipePreviewImage.animate().cancel();
            float start = targetPage > originalPage ? width : -width;
            contentScrollView.animate().translationX(0f).setDuration(170L)
                    .setInterpolator(new DecelerateInterpolator(1.4f))
                    .withEndAction(() -> {
                        clearSwipePreview();
                        swipeInProgress = false;
                        swipeTargetPage = -1;
                                    if (currentPage <= PAGE_SETTINGS) {
                            handler.removeCallbacks(adjacentPreviewPreloadRunnable);
                            handler.postDelayed(adjacentPreviewPreloadRunnable, 700L);
                        }
                    }).start();
            swipePreviewImage.animate().translationX(start).alpha(0.9f).setDuration(170L)
                    .setInterpolator(new DecelerateInterpolator(1.4f)).start();
        }
    }

    private void clearSwipePreview() {
        if (swipePreviewImage != null) {
            swipePreviewImage.animate().cancel();
            swipePreviewImage.setImageDrawable(null);
            swipePreviewImage.setVisibility(View.GONE);
            swipePreviewImage.setTranslationX(0f);
            swipePreviewImage.setAlpha(1f);
        }
        // swipePreviewBitmap 归 swipePagePreviewCache 所有；此处只解除显示引用。
        swipePreviewBitmap = null;
        swipeDeltaX = 0f;
    }

    /** 原位刷新当前页面和配色，不重建 Activity/根布局，避免开关触发整屏闪烁并保留 BLE GATT。 */
    private void recreateTo(int page) {
        if (contentScrollView != null && currentPage >= PAGE_HOME && currentPage <= PAGE_THEME) {
            pageScrollPositions[currentPage] = contentScrollView.getScrollY();
        }
        if (preferences != null) {
            pageScale = Math.max(0.8f, Math.min(1.1f, preferences.getFloat(KEY_PAGE_SCALE, pageScale)));
        }
        loadTheme();
        applySystemBars();

        boolean wantsFloatingBar = preferences != null && preferences.getBoolean(KEY_FLOATING_BAR, true);
        boolean hasFloatingBar = glassBarShell != null;
        if (root == null || contentContainer == null || contentScrollView == null
                || wantsFloatingBar != hasFloatingBar) {
            // 仅导航底栏从固定/悬浮模式切换时才需重建窗口层级。
            if (preferences != null) preferences.edit().putInt(KEY_REOPEN_PAGE, page).apply();
            suppressNextPageTransitionAnimation = true;
            buildInterface();
            return;
        }
        if (preferences != null) preferences.edit().remove(KEY_REOPEN_PAGE).apply();

        root.setBackgroundColor(backgroundColor);
        contentContainer.setPadding(dp(18), dp(18) + (shouldDrawImageBehindStatusBar() ? getStatusBarHeight() : 0), dp(18), dp(126));
        applyCustomBackground();
        if (navigationShell != null && glassBarShell == null) {
            navigationShell.setBackground(createBottomNavigationPillDrawable(backgroundColor, 0));
            if (navigationShell instanceof ViewGroup) {
                ViewGroup shell = (ViewGroup) navigationShell;
                if (shell.getChildCount() > 0) shell.getChildAt(0).setBackgroundColor(dividerColor);
            }
        }
        for (int i = 0; i < navIcons.length; i++) {
            if (navIcons[i] != null) navIcons[i].setTextSize(21 * pageScale);
            if (navTitles[i] != null) navTitles[i].setTextSize(10 * pageScale);
        }
        suppressNextPageTransitionAnimation = true;
        showPage(page);
        updateBottomNavigation();
        if (glassBarShell != null) glassBarShell.invalidate();
        
    }

    @Override
    public void onBackPressed() {
        if (currentPage == PAGE_THEME) {
            showPage(PAGE_SETTINGS);
            return;
        }
        if (currentPage != PAGE_HOME) {
            showPage(PAGE_HOME);
            return;
        }
        if (monitoring && preferences != null && preferences.getBoolean(KEY_MONITOR, false)) {
            // Keep the Activity (and its established GATT callbacks) alive while the foreground monitor is running.
            moveTaskToBack(true);
            return;
        }
        super.onBackPressed();
    }

    // =========================================================
    // 底部导航
    // =========================================================

    private void buildBottomNavigation() {
        bottomNavigation = new LinearLayout(this);
        bottomNavigation.setOrientation(LinearLayout.HORIZONTAL);
        bottomNavigation.setGravity(Gravity.CENTER);
        bottomNavigation.setPadding(dp(7), dp(2), dp(7), dp(2));
        bottomNavigation.setBackgroundColor(Color.TRANSPARENT);
        bottomNavigation.setClipChildren(false);
        bottomNavigation.setClipToPadding(false);
        navContentFrame = new FrameLayout(this);
        navContentFrame.setClipChildren(false);
        navContentFrame.setClipToPadding(false);
        selectedNavPill = new View(this);
        applyBottomNavigationPillBackground();
        selectedNavPill.setElevation(0f);
        FrameLayout.LayoutParams indicatorParams = new FrameLayout.LayoutParams(dp(48), dp(54), Gravity.TOP | Gravity.START);
        navContentFrame.addView(selectedNavPill, indicatorParams);

        String[] titles = {"首页", "数据", "设备", "监测", "设置"};
        String[] icons = {"⌂", "▤", "◉", "∿", "⚙"};

        for (int i = 0; i < titles.length; i++) {
            final int index = i;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(4), dp(4), dp(4), dp(4));
            item.setClickable(true);
            item.setFocusable(true);

            TextView icon = createText(icons[i], 21, Typeface.NORMAL);
            icon.setGravity(Gravity.CENTER);
            TextView title = createText(titles[i], 10, Typeface.BOLD);
            title.setGravity(Gravity.CENTER);
            item.addView(icon, new LinearLayout.LayoutParams(-1, dp(26)));
            item.addView(title, new LinearLayout.LayoutParams(-1, dp(16)));
            item.setOnClickListener(v -> showPage(index));
            installNavigationGesture(item, index);

            navItems[i] = item;
            navIcons[i] = icon;
            navTitles[i] = title;
            bottomNavigation.addView(item, new LinearLayout.LayoutParams(0, dp(58), 1));
        }

        boolean floating = preferences != null && preferences.getBoolean(KEY_FLOATING_BAR, true);
        if (floating) {
            GlassBarShell shell = new GlassBarShell(this);
            glassBarShell = shell;
            navigationShell = shell;
            shell.setClipChildren(false);
            shell.setClipToPadding(false);
            // 玻璃底层占满胶囊；只给图标内容留内边距，避免玻璃面四周漏出透明边。
            shell.setPadding(0, 0, 0, 0);
            navContentFrame.addView(bottomNavigation, new FrameLayout.LayoutParams(-1, -1));
            FrameLayout.LayoutParams navInsetParams = new FrameLayout.LayoutParams(-1, -1);
            navInsetParams.setMargins(dp(7), dp(5), dp(7), dp(5));
            shell.addView(navContentFrame, navInsetParams);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, dp(72),
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            params.leftMargin = dp(22);
            params.rightMargin = dp(22);
            params.bottomMargin = dp(12);
            root.addView(shell, params);
            shell.setElevation(dp(3));
            shell.post(() -> {
                if (navigationShell == shell) {
                    shell.setTranslationX(0f);
                    shell.setTranslationY(0f);
                    if (preferences != null) {
                        preferences.edit().remove(KEY_NAV_BAR_OFFSET_X)
                                .remove(KEY_NAV_BAR_OFFSET_Y).apply();
                    }
                }
            });
        } else {
            FrameLayout shell = new FrameLayout(this);
            navigationShell = shell;
            shell.setBackground(createBottomNavigationPillDrawable(backgroundColor, 0));
            View line = new View(this);
            line.setBackgroundColor(dividerColor);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, dp(1), Gravity.TOP);
            shell.addView(line, lp);
            selectedNavPill.setVisibility(View.GONE);
            FrameLayout.LayoutParams rowLp = new FrameLayout.LayoutParams(-1, dp(66), Gravity.BOTTOM);
            navContentFrame.addView(bottomNavigation, rowLp);
            shell.addView(navContentFrame, new FrameLayout.LayoutParams(-1, -1));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, dp(68), Gravity.BOTTOM);
            root.addView(shell, params);
        }
        updateBottomNavigation();
    }

    /** 按 KernelSU FloatingBottomBar 的手势：拖动的是选中胶囊，松手才切换页面。 */
    private void installNavigationGesture(View item, int index) {
        final int touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        item.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    navDraggedIndex = index;
                    navDragStartIndex = index;
                    navTouchDownRawX = event.getRawX();
                    navTouchDownRawY = event.getRawY();
                    navDragLastRawX = event.getRawX();
                    navDragLastTime = SystemClock.uptimeMillis();
                    navDragActive = false;
                    navPointerMoved = false;
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    float rawX = event.getRawX();
                    float dx = rawX - navTouchDownRawX;
                    float dy = event.getRawY() - navTouchDownRawY;
                    if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
                        navPointerMoved = true;
                    }
                    boolean horizontalIntent = Math.abs(dx) > touchSlop
                            && Math.abs(dx) > Math.abs(dy) * 1.1f;
                    if (!navDragActive && horizontalIntent) {
                        navDragActive = true;
                        if (navPillPositionAnimator != null) {
                            navPillPositionAnimator.cancel();
                            navPillPositionAnimator = null;
                        }
                        if (selectedNavPill != null) {
                            selectedNavPill.animate().cancel();
                            View startItem = navItems[Math.max(0, Math.min(navItems.length - 1, navDragStartIndex))];
                            navDragOriginPillX = startItem != null ? startItem.getLeft() - dp(3)
                                    : selectedNavPill.getTranslationX();
                            selectedNavPill.setTranslationX(navDragOriginPillX);
                            selectedNavPill.setScaleX(78f / 56f);
                            selectedNavPill.setScaleY(78f / 56f);
                        }
                    }

                    // 一旦横向拖动成立，后续每一帧都跟手，不会因手指略微斜向移动而卡住。
                    if (navDragActive && selectedNavPill != null && navContentFrame != null) {
                        float pillWidth = selectedNavPill.getWidth();
                        View first = navItems[0];
                        View last = navItems[navItems.length - 1];
                        float minX = first == null ? 0f : first.getLeft() - dp(3);
                        float maxX = last == null ? minX : last.getRight() - pillWidth + dp(3);
                        float targetX = Math.max(minX, Math.min(maxX, navDragOriginPillX + dx));
                        selectedNavPill.setTranslationX(targetX);

                        // KSU 风格的速度形变：横向滑得越快，胶囊越沿运动方向拉伸。
                        long now = SystemClock.uptimeMillis();
                        long elapsed = Math.max(1L, now - navDragLastTime);
                        float itemWidth = Math.max(1f, first == null ? dp(60) : first.getWidth());
                        float normalizedVelocity = ((rawX - navDragLastRawX) / itemWidth)
                                * (1000f / elapsed) / 10f;
                        normalizedVelocity = Math.max(-0.2f, Math.min(0.2f, normalizedVelocity));
                        float pressScale = 78f / 56f;
                        selectedNavPill.setScaleX(pressScale / (1f - normalizedVelocity * 0.75f));
                        selectedNavPill.setScaleY(pressScale * (1f - normalizedVelocity * 0.25f));
                        navDragLastRawX = rawX;
                        navDragLastTime = now;

                        int target = findNavigationIndexAtRawX(rawX);
                        if (target != navDraggedIndex) {
                            navDraggedIndex = target;
                            previewNavigationDragIndex(target);
                        }
                    }
                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    boolean wasDrag = navDragActive;
                    boolean wasMoved = navPointerMoved;
                    int target = navDraggedIndex;
                    navDragActive = false;
                    navDraggedIndex = -1;
                    navDragStartIndex = -1;
                    navPointerMoved = false;
                    if (wasDrag && target >= PAGE_HOME && target <= PAGE_SETTINGS) {
                        if (target != currentPage) showPage(target);
                        else updateBottomNavigation();
                        if (preferences.getBoolean(KEY_HAPTIC_FEEDBACK, true)) {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);
                        }
                    } else if (!wasDrag && !wasMoved) {
                        view.performClick();
                    } else {
                        updateBottomNavigation();
                    }
                    animateNavigationPillRelease();
                            return true;
                }

                case MotionEvent.ACTION_CANCEL:
                    navDraggedIndex = -1;
                    navDragStartIndex = -1;
                    navDragActive = false;
                    navPointerMoved = false;
                    updateBottomNavigation();
                    animateNavigationPillRelease();
                            return true;

                default:
                    return true;
            }
        });
    }

    private void animateNavigationPillRelease() {
        if (selectedNavPill == null) return;
        selectedNavPill.animate().cancel();
        selectedNavPill.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300L)
                .setInterpolator(new android.view.animation.OvershootInterpolator(0.35f))
                .start();
    }

    private int findNavigationIndexAtRawX(float rawX) {
        int closest = Math.max(0, Math.min(navItems.length - 1, navDraggedIndex));
        float closestDistance = Float.MAX_VALUE;
        for (int i = 0; i < navItems.length; i++) {
            View item = navItems[i];
            if (item == null || item.getWidth() <= 0) continue;
            int[] location = new int[2];
            item.getLocationOnScreen(location);
            float center = location[0] + item.getWidth() * 0.5f;
            float distance = Math.abs(rawX - center);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = i;
            }
        }
        return closest;
    }

    /** 拖动中预览手指所在标签；选中胶囊本身保持连续跟手，不在每个标签间跳跃。 */
    private void previewNavigationDragIndex(int index) {
        for (int i = 0; i < navItems.length; i++) {
            if (navItems[i] == null) continue;
            boolean selected = i == index;
            int idleNavigationText = getBottomNavigationIdleTextColor();
            int selectedNavigationText = getBottomNavigationSelectedTextColor();
            navIcons[i].setTextColor(selected ? selectedNavigationText : idleNavigationText);
            navTitles[i].setTextColor(selected ? selectedNavigationText : idleNavigationText);
            navIcons[i].setTypeface(Typeface.create("sans-serif-medium", selected ? Typeface.BOLD : Typeface.NORMAL));
            navTitles[i].setTypeface(Typeface.create("sans-serif-medium", selected ? Typeface.BOLD : Typeface.NORMAL));
        }
    }

    private final class GlassBarShell extends FrameLayout {
        private final GlassBackdropView backdropView;

        GlassBarShell(Context context) {
            super(context);
            setWillNotDraw(true);
            // Transparent shell: the live page remains visible beneath this lightweight material layer.
            setBackgroundColor(Color.TRANSPARENT);
            setClipChildren(true);
            setClipToPadding(true);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override public void getOutline(View view, Outline outline) {
                    float scale = Math.max(65, Math.min(145, preferences == null ? 100
                            : preferences.getInt(KEY_GLASS_RADIUS, 100))) / 100f;
                    float radius = Math.min(view.getHeight() * 0.5f, view.getHeight() * 0.5f * scale);
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            setClipToOutline(true);
            backdropView = new GlassBackdropView(context);
            addView(backdropView, new FrameLayout.LayoutParams(-1, -1));
        }

        void refreshBackdrop() {
            // Redraw only the translucent material; no screenshots or pixel processing are involved.
            setAlpha(1f);
            if (backdropView != null) {
                backdropView.setAlpha(1f);
                backdropView.invalidate();
            }
        }
    }

    /**
     * 低开销实时液态材质。
     * 不缓存或模糊页面截图，底层内容由 Android 正常合成，因此滚动时不会出现冻结画面。
     */
    private final class GlassBackdropView extends View {
        private final Paint tintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        GlassBackdropView(Context context) {
            super(context);
            setWillNotDraw(false);
            setBackgroundColor(Color.TRANSPARENT);
            edgePaint.setStyle(Paint.Style.STROKE);
            shinePaint.setStyle(Paint.Style.STROKE);
            shinePaint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            final float w = getWidth();
            final float h = getHeight();
            if (w <= 0f || h <= 0f) return;

            int radiusScale = Math.max(65, Math.min(145, preferences == null ? 100
                    : preferences.getInt(KEY_GLASS_RADIUS, 100)));
            float radius = Math.min(h * 0.5f, h * 0.5f * radiusScale / 100f);
            RectF bounds = new RectF(0.5f, 0.5f, w - 0.5f, h - 0.5f);
            boolean glassEnabled = isBottomNavigationGlassEnabled();

            int topBase = getBottomNavigationTopColor();
            int bottomBase = getBottomNavigationBottomColor();
            if (!glassEnabled) {
                int solid = darkMode ? Color.rgb(27, 30, 36) : Color.rgb(248, 249, 252);
                tintPaint.setShader(null);
                tintPaint.setColor(solid);
                canvas.drawRoundRect(bounds, radius, radius, tintPaint);
                edgePaint.setStrokeWidth(dp(1));
                edgePaint.setColor(darkMode ? 0xFF373D47 : 0xFFE0E5EC);
                canvas.drawRoundRect(bounds, radius, radius, edgePaint);
                return;
            }

            int opacitySetting = Math.max(35, Math.min(95, preferences == null ? 62
                    : preferences.getInt(KEY_GLASS_OPACITY, 62)));
            float opacity = (opacitySetting - 35) / 60f;
            int depthSetting = Math.max(0, Math.min(100, preferences == null ? 45
                    : preferences.getInt(KEY_GLASS_REFRACTION, 45)));
            float materialDepth = depthSetting / 100f;
            if (materialDepth > 0f) {
                bottomBase = blendColors(bottomBase,
                        darkMode ? Color.BLACK : Color.rgb(222, 228, 236), materialDepth * 0.075f);
            }

            // 深浅模式使用各自独立的底色与透明层级；不读取桌面壁纸面板不透明度。
            int topAlpha = Math.round((darkMode ? 204f : 216f) + opacity * (darkMode ? 12f : 8f));
            int bottomAlpha = Math.round((darkMode ? 216f : 208f) + opacity * (darkMode ? 12f : 8f));
            int topTint = withAlpha(topBase, Math.min(250, topAlpha));
            int bottomTint = withAlpha(bottomBase, Math.min(250, bottomAlpha));
            tintPaint.setShader(new LinearGradient(0f, 0f, 0f, h,
                    new int[]{topTint, bottomTint}, null, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(bounds, radius, radius, tintPaint);
            tintPaint.setShader(null);

            int highlightSetting = Math.max(0, Math.min(100, preferences == null ? 58
                    : preferences.getInt(KEY_GLASS_HIGHLIGHT, 58)));
            if (highlightSetting > 0) {
                shinePaint.setStrokeWidth(dp(1.0f));
                shinePaint.setColor(withAlpha(Color.WHITE,
                        scaleAlpha(darkMode ? 48 : 110, highlightSetting / 100f)));
                float inset = Math.max(dp(18), radius * 0.58f);
                canvas.drawLine(inset, dp(1.2f), w - inset, dp(1.2f), shinePaint);

                edgePaint.setStrokeWidth(dp(0.75f));
                edgePaint.setColor(withAlpha(Color.WHITE,
                        scaleAlpha(darkMode ? 52 : 108, highlightSetting / 100f)));
                canvas.drawRoundRect(bounds, radius, radius, edgePaint);
                edgePaint.setColor(withAlpha(Color.BLACK,
                        scaleAlpha(darkMode ? 48 : 22, highlightSetting / 100f)));
                canvas.drawArc(new RectF(dp(1), dp(1), w - dp(1), h - dp(1)),
                        8f, 164f, false, edgePaint);
            }
        }
    }

    private void updateSelectedNavigationPill(boolean animated) {
        int selectedIndex = currentPage == PAGE_THEME ? PAGE_SETTINGS : currentPage;
        moveSelectedNavigationPill(selectedIndex, animated);
    }

    private void moveSelectedNavigationPill(int requestedIndex, boolean animated) {
        if (selectedNavPill == null || navContentFrame == null || glassBarShell == null) return;
        final int selectedIndex = Math.max(0, Math.min(navItems.length - 1, requestedIndex));
        View target = navItems[selectedIndex];
        if (target == null || target.getWidth() <= 0 || navContentFrame.getWidth() <= 0) {
            navContentFrame.postDelayed(() -> moveSelectedNavigationPill(selectedIndex, false), 32L);
            return;
        }

        // KSU 的指示器始终是单个圆润胶囊，只随选中位置移动；拖动形变由 scaleX/scaleY 体现。
        int pillHeight = Math.min(dp(54), Math.max(dp(42), navContentFrame.getHeight() - dp(8)));
        int pillWidth = target.getWidth() + dp(6);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selectedNavPill.getLayoutParams();
        int top = (navContentFrame.getHeight() - pillHeight) / 2;
        if (params.width != pillWidth || params.height != pillHeight || params.topMargin != top
                || params.leftMargin != 0 || params.rightMargin != 0) {
            params.width = pillWidth;
            params.height = pillHeight;
            params.gravity = Gravity.TOP | Gravity.START;
            params.topMargin = top;
            params.leftMargin = 0;
            params.rightMargin = 0;
            selectedNavPill.setLayoutParams(params);
        }
        float targetX = target.getLeft() - dp(3);
        if (navPillPositionAnimator != null) {
            navPillPositionAnimator.cancel();
            navPillPositionAnimator = null;
        }
        if (animated && selectedNavPill.getVisibility() == View.VISIBLE) {
            navPillPositionAnimator = ObjectAnimator.ofFloat(selectedNavPill, View.TRANSLATION_X, targetX);
            navPillPositionAnimator.setDuration(300L);
            navPillPositionAnimator.setInterpolator(new android.view.animation.OvershootInterpolator(0.35f));
            navPillPositionAnimator.start();
            selectedNavPill.animate().cancel();
            selectedNavPill.animate().scaleX(1f).scaleY(1f)
                    .setDuration(300L)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(0.35f))
                    .start();
        } else {
            selectedNavPill.setTranslationX(targetX);
            selectedNavPill.setScaleX(1f);
            selectedNavPill.setScaleY(1f);
        }
        selectedNavPill.setVisibility(View.VISIBLE);
        selectedNavPill.bringToFront();
        bottomNavigation.bringToFront();
    }

    private void updateBottomNavigation() {
        boolean floating = glassBarShell != null;
        for (int i = 0; i < navItems.length; i++) {
            if (navItems[i] == null) continue;
            boolean selected = i == (currentPage == PAGE_THEME ? PAGE_SETTINGS : currentPage);
            if (floating) {
                GradientDrawable clear = new GradientDrawable();
                clear.setColor(Color.TRANSPARENT);
                clear.setCornerRadius(dp(22));
                glassSurfaceSpecs.remove(navItems[i]);
                navItems[i].setBackground(clear);
                navItems[i].setElevation(0f);
            } else if (selected) {
                // 选中态仅用低饱和主题色轻铺底，不再用高比例主题色盖住文字。
                glassSurfaceSpecs.remove(navItems[i]);
                navItems[i].setBackground(createNavigationSelectionDrawable(22));
                navItems[i].setElevation(0f);
            } else {
                GradientDrawable shape = new GradientDrawable();
                shape.setColor(Color.TRANSPARENT);
                shape.setCornerRadius(dp(22));
                glassSurfaceSpecs.remove(navItems[i]);
                navItems[i].setBackground(shape);
                navItems[i].setElevation(0f);
            }
            int idleNavigationText = getBottomNavigationIdleTextColor();
            int selectedNavigationText = getBottomNavigationSelectedTextColor();
            navIcons[i].setTextColor(selected ? selectedNavigationText : idleNavigationText);
            navTitles[i].setTextColor(selected ? selectedNavigationText : idleNavigationText);
            navIcons[i].setTypeface(Typeface.create("sans-serif-medium", selected ? Typeface.BOLD : Typeface.NORMAL));
            navTitles[i].setTypeface(Typeface.create("sans-serif-medium", selected ? Typeface.BOLD : Typeface.NORMAL));
        }
        if (selectedNavPill != null) {
            if (floating) {
                selectedNavPill.setVisibility(View.VISIBLE);
                applyBottomNavigationPillBackground();
                updateSelectedNavigationPill(true);
            } else {
                selectedNavPill.setVisibility(View.GONE);
            }
        }
        if (glassBarShell != null) {
            glassBarShell.invalidate();
            glassBarShell.refreshBackdrop();
        } else if (navigationShell != null) {
            navigationShell.setBackground(createBottomNavigationPillDrawable(backgroundColor, 0));
        }
    }

    // =========================================================
    // 页面切换
    // =========================================================

    private void showPage(int page) {
        if (page < PAGE_HOME || page > PAGE_THEME || contentContainer == null) return;
        int previousPage = currentPage;
        boolean pageChanged = previousPage != page;
        if (!renderingSwipePreview && contentScrollView != null
                && previousPage >= PAGE_HOME && previousPage <= PAGE_THEME && pageChanged) {
            pageScrollPositions[previousPage] = contentScrollView.getScrollY();
            clearSwipePagePreviewCache();
            handler.removeCallbacks(adjacentPreviewPreloadRunnable);
        }
        boolean animate = preferences.getBoolean(KEY_STARTUP_ANIMATION, true)
                && !suppressNextPageTransitionAnimation
                && contentScrollView != null
                && pageChanged;
        suppressNextPageTransitionAnimation = false;

        if (contentScrollView != null) {
            contentScrollView.animate().cancel();
            contentScrollView.setAlpha(animate ? 0f : 1f);
            contentScrollView.setTranslationX(animate ? dp(page > previousPage ? 26 : -26) : 0f);
            contentScrollView.setScaleX(1f);
            contentScrollView.setScaleY(1f);
        }

        currentPage = page;
        if (!renderingSwipePreview) updateBottomNavigation();
        contentContainer.removeAllViews();
        populatePageContent(page);
        normalizePageContentAlignment();

        final int restoreY = Math.max(0, pageScrollPositions[page]);
        if (contentScrollView != null) {
            contentScrollView.post(() -> {
                if (contentScrollView != null && currentPage == page) {
                    contentScrollView.scrollTo(0, restoreY);
                }
            });
        }
        if (animate && contentScrollView != null) {
            contentScrollView.animate().alpha(1f).translationX(0f)
                    .setDuration(220L)
                    .setInterpolator(new DecelerateInterpolator(1.45f))
                    .start();
        }
        
        if (!renderingSwipePreview && page <= PAGE_SETTINGS) {
            handler.removeCallbacks(adjacentPreviewPreloadRunnable);
            handler.postDelayed(adjacentPreviewPreloadRunnable, 1000L);
        }
    }

    /**
     * 所有页面共用同一条左右内容基线：根容器留白 18dp，直接子项不再额外偏移。
     * 保留每个模块自己的上下间距，避免把标题、图表和分组卡片强行挤成同一节奏。
     */
    private void normalizePageContentAlignment() {
        if (contentContainer == null) return;
        contentContainer.setGravity(Gravity.TOP);
        for (int i = 0; i < contentContainer.getChildCount(); i++) {
            View child = contentContainer.getChildAt(i);
            ViewGroup.LayoutParams raw = child.getLayoutParams();
            if (raw instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) raw;
                if (lp.leftMargin != 0 || lp.rightMargin != 0) {
                    lp.leftMargin = 0;
                    lp.rightMargin = 0;
                    child.setLayoutParams(lp);
                }
            }
        }
    }

    /** 页面内容统一入口，允许滑动切页期间在临时容器里生成相邻页面预览。 */
    private void populatePageContent(int page) {
        switch (page) {
            case PAGE_HOME:
                buildHomePage();
                break;
            case PAGE_DATA:
                buildDataPage();
                break;
            case PAGE_DEVICE:
                buildDevicePage();
                break;
            case PAGE_MONITOR:
                buildMonitorPage();
                break;
            case PAGE_SETTINGS:
                buildSettingsPage();
                break;
            case PAGE_THEME:
                buildThemePage();
                break;
        }
    }

    // =========================================================
    // 首页
    // =========================================================

    private void buildHomePage() {
        LinearLayout pageHeading = new LinearLayout(this);
        pageHeading.setOrientation(LinearLayout.VERTICAL);
        TextView title = createText("总览", 24, Typeface.BOLD);
        title.setIncludeFontPadding(false);
        pageHeading.addView(title, new LinearLayout.LayoutParams(-1, dp(30)));
        TextView subtitle = createText("心率与设备状态", 11, Typeface.NORMAL);
        subtitle.setTextColor(secondaryTextColor);
        subtitle.setIncludeFontPadding(false);
        pageHeading.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(19)));
        contentContainer.addView(pageHeading, marginParams(-1, dp(52), 0, 0, 0, 12));

        // 主状态卡片：借鉴 KernelSU Manager 的状态卡与 Material You tonal surface 层级。
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18), dp(17), dp(18), dp(17));
        setGlassBackground(hero, blendColors(cardColor, accentColor, 0.97f), 22);
        if (Build.VERSION.SDK_INT >= 21) hero.setElevation(dp(isLiquidGlassEnabled() ? 1 : 0));

        LinearLayout heroTop = new LinearLayout(this);
        heroTop.setGravity(Gravity.CENTER_VERTICAL);
        TextView heroLabel = createText("实时心率", 15, Typeface.BOLD);
        heroLabel.setTextColor(accentColor);
        heroTop.addView(heroLabel, new LinearLayout.LayoutParams(0, dp(32), 1));

        homeStatus = createText(connected ? "已连接" : "等待设备", 12, Typeface.NORMAL);
        homeStatus.setGravity(Gravity.CENTER);
        homeStatus.setPadding(dp(9), dp(4), dp(9), dp(4));
        int statusSurfaceColor = darkMode ? Color.rgb(38, 63, 53) : Color.rgb(218, 242, 225);
        setGlassBackground(homeStatus, statusSurfaceColor, 18);
        homeStatus.setTextColor(connected ? Color.rgb(61, 157, 105) : secondaryTextColor);
        heroTop.addView(homeStatus, new LinearLayout.LayoutParams(-2, dp(32)));
        hero.addView(heroTop);

        LinearLayout heartRow = new LinearLayout(this);
        heartRow.setGravity(Gravity.CENTER_VERTICAL);
        homeHeart = createText(currentHeartRate > 0 ? String.valueOf(currentHeartRate) : "--", 54, Typeface.BOLD);
        homeHeart.setTextColor(textColor);
        heartRow.addView(homeHeart, new LinearLayout.LayoutParams(0, dp(82), 1));
        TextView bpm = createText("BPM", 13, Typeface.NORMAL);
        bpm.setTextColor(secondaryTextColor);
        heartRow.addView(bpm, new LinearLayout.LayoutParams(dp(52), dp(60)));
        hero.addView(heartRow, new LinearLayout.LayoutParams(-1, dp(84)));

        TextView hint = createText(
                connected ? "正在接收设备数据" : "连接蓝牙设备以开始记录",
                12, Typeface.NORMAL);
        hint.setTextColor(secondaryTextColor);
        hero.addView(hint, marginParams(-1, dp(26), 0, 0, 0, 12));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button monitorButton = createButton(monitoring ? "停止监测" : "开始监测");
        monitorButton.setOnClickListener(v -> {
            if (monitoring) stopMonitoring(); else startMonitoring();
            showPage(PAGE_HOME);
        });
        Button connectButton = createButton(connected ? "设备详情" : "连接设备");
        connectButton.setOnClickListener(v -> {
            if (connected) showPage(PAGE_DEVICE); else startDeviceScan();
        });
        actions.addView(monitorButton, new LinearLayout.LayoutParams(0, dp(48), 1));
        LinearLayout.LayoutParams connectParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        connectParams.leftMargin = dp(12);
        actions.addView(connectButton, connectParams);
        hero.addView(actions);
        contentContainer.addView(hero, marginParams(-1, -2, 0, 0, 0, 16));

        LinearLayout chartCard = createCard();
        LinearLayout chartHeader = new LinearLayout(this);
        chartHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView chartTitle = createText("心率趋势", 16, Typeface.BOLD);
        chartHeader.addView(chartTitle, new LinearLayout.LayoutParams(0, dp(36), 1));
        TextView chartCaption = createText("历史记录", 11, Typeface.NORMAL);
        chartCaption.setTextColor(secondaryTextColor);
        chartHeader.addView(chartCaption, new LinearLayout.LayoutParams(-2, dp(36)));
        chartCard.addView(chartHeader);
        chart = new HeartRateChart(this);
        HeartDatabase db = new HeartDatabase(this);
        chart.setValues(db.getRecentValues(80));
        chartCard.addView(chart, marginParams(-1, dp(210), 0, 8, 0, 0));
        contentContainer.addView(chartCard, marginParams(-1, -2, 0, 0, 0, 16));

        LinearLayout ecgCard = createCard();
        LinearLayout ecgHeader = new LinearLayout(this);
        ecgHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView ecgTitle = createText("心电图", 16, Typeface.BOLD);
        ecgHeader.addView(ecgTitle, new LinearLayout.LayoutParams(0, dp(38), 1));
        final EcgChart ecgChart = new EcgChart(this);
        // 点按切换两种实时模式：刷新模式（从左往右） / 平移模式（从右往左）
        final TextView ecgMode = createText(ecgChart.isEraseMode() ? "刷新模式" : "平移模式", 12, Typeface.NORMAL);
        ecgMode.setTextColor(accentColor);
        ecgMode.setGravity(Gravity.CENTER);
        ecgMode.setPadding(dp(10), 0, dp(10), 0);
        ecgMode.setOnClickListener(v -> {
            boolean erase = !ecgChart.isEraseMode();
            ecgChart.setEraseMode(erase);
            ecgMode.setText(erase ? "刷新模式" : "平移模式");
        });
        ecgHeader.addView(ecgMode, new LinearLayout.LayoutParams(-2, dp(38)));
        ecgCard.addView(ecgHeader);
        ecgCard.addView(ecgChart, marginParams(-1, dp(220), 0, 6, 0, 0));
        contentContainer.addView(ecgCard, marginParams(-1, -2, 0, 0, 0, 16));

        LinearLayout todayTitleRow = new LinearLayout(this);
        todayTitleRow.setOrientation(LinearLayout.HORIZONTAL);
        todayTitleRow.setGravity(Gravity.CENTER_VERTICAL);
        todayTitleRow.addView(createText("今日统计", 16, Typeface.BOLD), new LinearLayout.LayoutParams(0, dp(36), 1));
        contentContainer.addView(todayTitleRow, marginParams(-1, dp(36), 0, 0, 0, 8));
        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        homeMin = addStat(stats, "最低 BPM", db.getTodayMin() > 0 ? String.valueOf(db.getTodayMin()) : "--");
        homeAvg = addStat(stats, "平均 BPM", db.getTodayAverage() > 0 ? String.valueOf(db.getTodayAverage()) : "--");
        homeMax = addStat(stats, "最高 BPM", db.getTodayMax() > 0 ? String.valueOf(db.getTodayMax()) : "--");
        contentContainer.addView(stats, marginParams(-1, dp(112), 0, 0, 0, 16));

        LinearLayout deviceCard = createCard();
        LinearLayout deviceTitleRow = new LinearLayout(this);
        deviceTitleRow.setOrientation(LinearLayout.HORIZONTAL);
        deviceTitleRow.setGravity(Gravity.CENTER_VERTICAL);
        deviceTitleRow.addView(createText("设备信息", 16, Typeface.BOLD), new LinearLayout.LayoutParams(0, dp(34), 1));
        deviceCard.addView(deviceTitleRow, marginParams(-1, dp(34), 0, 0, 0, 4));
        homeDevice = addInfoRow(deviceCard, "已连接设备", connectedName);
        homeBattery = addInfoRow(deviceCard, "设备电量", currentBattery >= 0 ? currentBattery + "%" : "未读取到");
        homeRssi = addInfoRow(deviceCard, "蓝牙信号", currentRssi != 0 ? currentRssi + " dBm" : "--");
        homeCount = addInfoRow(deviceCard, "今日测量次数", String.valueOf(getTodayCount()));
        homeLast = addInfoRow(deviceCard, "最近测量时间", getLastTime());
        contentContainer.addView(deviceCard, marginParams(-1, -2, 0, 0, 0, 16));

    }


    // =========================================================
    // 数据页面
    // =========================================================

    private void buildDataPage() {
        LinearLayout dataTitleRow = new LinearLayout(this);
        dataTitleRow.setOrientation(LinearLayout.HORIZONTAL);
        dataTitleRow.setGravity(Gravity.CENTER_VERTICAL);
        dataTitleRow.addView(createText("数据", 24, Typeface.BOLD), new LinearLayout.LayoutParams(0, dp(44), 1));
        contentContainer.addView(dataTitleRow, marginParams(-1, dp(44), 0, 0, 0, 12));

        LinearLayout rangeRow = new LinearLayout(this);
        rangeRow.setOrientation(LinearLayout.HORIZONTAL);
        rangeRow.setGravity(Gravity.CENTER_VERTICAL);
        rangeRow.setWeightSum(3f);
        String[] rangeNames = {"今日", "近7天", "近30天"};
        Button[] rangeButtons = new Button[rangeNames.length];
        for (int i = 0; i < rangeNames.length; i++) {
            final int range = i;
            Button rangeButton = createButton(rangeNames[i]);
            rangeButtons[i] = rangeButton;
            // Three compact single-line tabs: no checkmark suffix, no default Button min-width, no text wrapping.
            rangeButton.setTextSize(12 * pageScale);
            rangeButton.setSingleLine(true);
            rangeButton.setMaxLines(1);
            rangeButton.setEllipsize(android.text.TextUtils.TruncateAt.END);
            rangeButton.setPadding(0, 0, 0, 0);
            rangeButton.setMinWidth(0);
            rangeButton.setMinHeight(dp(40));
            rangeButton.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, dp(40), 1f);
            if (i > 0) rp.leftMargin = dp(4);
            rangeRow.addView(rangeButton, rp);
            rangeButton.setOnClickListener(v -> {
                selectedDataRange = range;
                refreshDataRange();
                for (int j = 0; j < rangeButtons.length; j++) {
                    rangeButtons[j].setTextColor(j == selectedDataRange ? accentColor : textColor);
                    rangeButtons[j].setAlpha(j == selectedDataRange ? 1f : 0.78f);
                }
            });
            rangeButton.setTextColor(i == selectedDataRange ? accentColor : textColor);
            rangeButton.setAlpha(i == selectedDataRange ? 1f : 0.78f);
        }
        contentContainer.addView(rangeRow, marginParams(-1, dp(40), 0, 0, 0, 10));

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        dataMinValue = addStat(stats, "最低 BPM", "--");
        dataAvgValue = addStat(stats, "平均 BPM", "--");
        dataMaxValue = addStat(stats, "最高 BPM", "--");
        contentContainer.addView(stats, marginParams(-1, dp(105), 0, 0, 0, 8));

        dataCountText = createText("记录数：--", 14, Typeface.NORMAL);
        dataCountText.setTextColor(secondaryTextColor);
        contentContainer.addView(dataCountText, marginParams(-1, -2, 0, 0, 0, 10));

        LinearLayout chartCard = createCard();
        LinearLayout chartHeader = new LinearLayout(this);
        chartHeader.setGravity(Gravity.CENTER_VERTICAL);
        chartHeader.addView(createText("心率趋势", 17, Typeface.BOLD), new LinearLayout.LayoutParams(0, dp(42), 1));
        Button refreshChart = createButton("刷新");
        refreshChart.setOnClickListener(v -> refreshDataRange());
        chartHeader.addView(refreshChart, new LinearLayout.LayoutParams(dp(64), dp(42)));
        chartCard.addView(chartHeader);
        dataChart = new HeartRateChart(this);
        chartCard.addView(dataChart, marginParams(-1, dp(235), 0, 4, 0, 0));
        contentContainer.addView(chartCard, marginParams(-1, -2, 0, 0, 0, 14));
        refreshDataRange();

        TextView historyTitle = createText("最近记录", 17, Typeface.BOLD);
        contentContainer.addView(historyTitle, marginParams(-1, dp(48), 0, 0, 0, 0));
        historyContainer = new LinearLayout(this);
        historyContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.addView(historyContainer, marginParams(-1, -2, 0, 0, 0, 12));
        renderHistoryRecords();

        Button export = createButton("导出 CSV");
        export.setOnClickListener(v -> exportData());
        contentContainer.addView(export, marginParams(-1, dp(52), 0, 12, 0, 0));
        Button importCsv = createButton("从 CSV 导入 / 合并历史数据");
        importCsv.setOnClickListener(v -> chooseCsvToImport());
        contentContainer.addView(importCsv, marginParams(-1, dp(52), 0, 8, 0, 0));
    }

    private long[] getSelectedDataRangeBounds() {
        Calendar end = Calendar.getInstance();
        long endExclusive = end.getTimeInMillis() + 1L;
        Calendar start = (Calendar) end.clone();
        if (selectedDataRange == 0) {
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
        } else if (selectedDataRange == 1) {
            start.add(Calendar.DAY_OF_YEAR, -7);
        } else {
            start.add(Calendar.DAY_OF_YEAR, -30);
        }
        return new long[]{start.getTimeInMillis(), endExclusive};
    }

    private void refreshDataRange() {
        if (dataMinValue == null || dataAvgValue == null || dataMaxValue == null || dataCountText == null) return;
        HeartDatabase db = new HeartDatabase(this);
        long[] bounds = getSelectedDataRangeBounds();
        HeartDatabase.Summary summary = db.getSummary(bounds[0], bounds[1]);
        dataMinValue.setText(summary.min > 0 ? String.valueOf(summary.min) : "--");
        dataAvgValue.setText(summary.average > 0 ? String.valueOf(summary.average) : "--");
        dataMaxValue.setText(summary.max > 0 ? String.valueOf(summary.max) : "--");
        String period = selectedDataRange == 0 ? "今日" : selectedDataRange == 1 ? "近 7 天" : "近 30 天";
        dataCountText.setText(period + "记录：" + summary.count + " 次");
        if (dataChart != null) dataChart.setValues(db.getValuesBetween(bounds[0], bounds[1], 100));
    }

    private void renderHistoryRecords() {

        if (historyContainer == null) return;
        historyContainer.removeAllViews();

        HeartDatabase db = new HeartDatabase(this);
        ArrayList<HeartDatabase.Record> raw = db.getRecentRecords(300);
        ArrayList<HeartDatabase.Record> records = new ArrayList<>();

        // 每 10 秒最多显示一条，按最新记录优先。
        long lastBucket = Long.MIN_VALUE;
        for (HeartDatabase.Record record : raw) {
            long bucket = record.timestamp / 10000L;
            if (bucket != lastBucket) {
                records.add(record);
                lastBucket = bucket;
            }
            if (records.size() >= 60) break;
        }

        if (records.isEmpty()) {
            TextView empty = createText("暂时没有心率记录", 15, Typeface.NORMAL);
            empty.setTextColor(secondaryTextColor);
            historyContainer.addView(
                    empty,
                    marginParams(-1, dp(70), 0, 0, 0, 10)
            );
            return;
        }

        for (HeartDatabase.Record record : records) {
            LinearLayout row = createCard();
            row.addView(createText(record.heartRate + " BPM", 22, Typeface.BOLD));

            TextView timeText = createText(
                    formatTime(record.timestamp),
                    14,
                    Typeface.NORMAL
            );
            timeText.setTextColor(secondaryTextColor);
            row.addView(timeText);

            historyContainer.addView(
                    row,
                    marginParams(-1, dp(78), 0, 0, 0, 8)
            );
        }
    }

    // =========================================================
    // 设备页面
    // =========================================================

    private void buildDevicePage() {
        LinearLayout pageTitle = new LinearLayout(this);
        pageTitle.setOrientation(LinearLayout.HORIZONTAL);
        pageTitle.setGravity(Gravity.CENTER_VERTICAL);
        pageTitle.addView(createText("设备", 24, Typeface.BOLD), new LinearLayout.LayoutParams(0, dp(44), 1));
        contentContainer.addView(pageTitle, marginParams(-1, dp(44), 0, 0, 0, 14));

        LinearLayout state =
                createCard();

        deviceStatus =
                addInfoRow(
                        state,
                        "状态",
                        connected
                                ? "已连接"
                                : "未连接"
                );

        deviceName =
                addInfoRow(
                        state,
                        "名称",
                        connectedName
                );

        deviceAddress =
                addInfoRow(
                        state,
                        "地址",
                        connectedAddress.isEmpty()
                                ? "--"
                                : connectedAddress
                );

        deviceRssi =
                addInfoRow(
                        state,
                        "信号",
                        currentRssi != 0
                                ? currentRssi + " dBm"
                                : "--"
                );

        deviceBattery =
                addInfoRow(
                        state,
                        "电量",
                        currentBattery >= 0
                                ? currentBattery + "%"
                                : "未读取到"
                );

        deviceSteps = addInfoRow(state, "步数", currentSteps >= 0 ? String.valueOf(currentSteps) : "未读取到");
        devicePacketCount = addInfoRow(state, "收到的数据包", receivedBlePacketCount + " 包");

        contentContainer.addView(
                state,
                marginParams(
                        -1,
                        -2,
                        0,
                        0,
                        0,
                        12
                )
        );

        LinearLayout actions =
                new LinearLayout(this);

        actions.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button scan =
                createButton(
                        "扫描设备"
                );

        scan.setOnClickListener(
                v -> startDeviceScan()
        );

        actions.addView(
                scan,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        Button reconnect =
                createButton(
                        "自动连接"
                );

        reconnect.setOnClickListener(
                v -> autoConnect()
        );

        LinearLayout.LayoutParams rp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                );

        rp.leftMargin =
                dp(8);

        actions.addView(
                reconnect,
                rp
        );

        contentContainer.addView(
                actions,
                marginParams(
                        -1,
                        dp(52),
                        0,
                        0,
                        0,
                        8
                )
        );
        Button savedDevices = createButton("已保存设备 / 多设备管理");
        savedDevices.setOnClickListener(v -> showSavedDevicesDialog());
        contentContainer.addView(savedDevices, marginParams(-1, dp(48), 0, 0, 0, 12));

        Button disconnect =
                createButton(
                        "断开当前设备"
                );

        disconnect.setOnClickListener(
                v -> disconnectDevice()
        );

        contentContainer.addView(
                disconnect,
                marginParams(
                        -1,
                        dp(52),
                        0,
                        0,
                        0,
                        12
                )
        );

        LinearLayout capabilities =
                createCard();

        deviceCapabilities =
                addInfoRow(
                        capabilities,
                        "心率服务",
                        "检测中"
                );

        deviceBatteryCapability = addInfoRow(
                capabilities,
                "电池服务",
                currentBattery >= 0 ? "已读取电量" : "检测中"
        );

        deviceManufacturer =
                addInfoRow(
                        capabilities,
                        "厂商",
                        manufacturer
                );

        deviceModel =
                addInfoRow(
                        capabilities,
                        "型号",
                        model
                );

        deviceFirmware =
                addInfoRow(
                        capabilities,
                        "固件",
                        firmware
                );

        contentContainer.addView(
                capabilities,
                marginParams(-1, -2, 0, 0, 0, 16)
        );

        LinearLayout diagnosticsCard =
                createCard();

        diagnosticsCard.addView(
                createText(
                        "协议诊断（高级）",
                        20,
                        Typeface.BOLD
                )
        );

        TextView diagnosticsDescription =
                createText(
                        "列出手环开放出来的全部蓝牙服务和特征值 UUID，" +
                                "用来判断能不能走标准协议读取步数/睡眠等数据。" +
                                "需要先连接上设备。",
                        13,
                        Typeface.NORMAL
                );

        diagnosticsDescription.setTextColor(
                secondaryTextColor
        );

        diagnosticsCard.addView(
                diagnosticsDescription,
                marginParams(
                        -1,
                        -2,
                        0,
                        3,
                        0,
                        10
                )
        );

        Button diagnosticsButton =
                createButton(
                        "查看蓝牙服务列表"
                );

        diagnosticsButton.setOnClickListener(
                v -> showServiceDumpDialog()
        );

        diagnosticsCard.addView(
                diagnosticsButton,
                marginParams(
                        -1,
                        dp(48),
                        0,
                        8,
                        0,
                        0
                )
        );
        Button reportButton = createButton("生成并分享诊断报告");
        reportButton.setOnClickListener(v -> shareDiagnosticReport());
        diagnosticsCard.addView(reportButton, marginParams(-1, dp(48), 0, 8, 0, 0));

        contentContainer.addView(
                diagnosticsCard,
                marginParams(
                        -1,
                        -2,
                        0,
                        12,
                        0,
                        0
                )
        );
    }

    // =========================================================
    // 监测页面
    // =========================================================

    private void buildMonitorPage() {
        LinearLayout pageTitle = new LinearLayout(this);
        pageTitle.setOrientation(LinearLayout.HORIZONTAL);
        pageTitle.setGravity(Gravity.CENTER_VERTICAL);
        pageTitle.addView(createText("监测", 24, Typeface.BOLD), new LinearLayout.LayoutParams(0, dp(44), 1));
        contentContainer.addView(pageTitle, marginParams(-1, dp(44), 0, 0, 0, 14));

        LinearLayout card =
                createCard();

        monitorStatus = createText(getMonitoringStatusLabel(), 18, Typeface.BOLD);
        monitorStatus.setTextColor(isMonitoringActively() ? Color.rgb(48, 190, 110) : secondaryTextColor);

        card.addView(monitorStatus);

        monitorHeart =
                createText(
                        currentHeartRate > 0
                                ? currentHeartRate + " BPM"
                                : "-- BPM",
                        52,
                        Typeface.BOLD
                );

        card.addView(
                monitorHeart,
                marginParams(
                        -1,
                        dp(75),
                        0,
                        12,
                        0,
                        0
                )
        );

        Button monitorButton =
                createButton(
                        monitoring
                                ? "停止监测"
                                : "开始监测"
                );

        monitorButton.setOnClickListener(
                v -> {

                    if (monitoring) {
                        stopMonitoring();
                    } else {
                        startMonitoring();
                    }

                    showPage(PAGE_MONITOR);
                }
        );

        card.addView(
                monitorButton,
                marginParams(
                        -1,
                        dp(54),
                        0,
                        0,
                        0,
                        0
                )
        );

        contentContainer.addView(
                card,
                marginParams(
                        -1,
                        -2,
                        0,
                        0,
                        0,
                        14
                )
        );

        LinearLayout tip =
                createCard();

        tip.addView(
                createText(
                        "监测说明",
                        18,
                        Typeface.BOLD
                )
        );

        TextView tipText =
                createText(
                        "开启监测后，App 会持续接收兼容 BLE 心率设备发送的实时心率数据，并保存到本地历史记录。",
                        14,
                        Typeface.NORMAL
                );

        tipText.setTextColor(
                secondaryTextColor
        );

        tip.addView(
                tipText,
                marginParams(
                        -1,
                        -2,
                        0,
                        8,
                        0,
                        0
                )
        );

        contentContainer.addView(tip, marginParams(-1, -2, 0, 0, 0, 16));
    }

    // =========================================================
    // 设置页面
    // =========================================================

    private void buildSettingsPage() {

        contentContainer.addView(createText("设置", 24, Typeface.BOLD),
                marginParams(-1, dp(38), 0, 0, 0, 0));
        TextView settingsSubtitle = createText("监测 · 提醒 · 外观 · 数据", 12, Typeface.NORMAL);
        settingsSubtitle.setTextColor(secondaryTextColor);
        settingsSubtitle.setIncludeFontPadding(false);
        contentContainer.addView(settingsSubtitle, marginParams(-1, -2, 0, 0, 0, 16));

        // 标题用自适应高度并统一左右留白，不再用固定 34dp 把文字挤进卡片边缘。
        LinearLayout monitoringOptions = createKsuCard();
        TextView monitoringHeading = createText("监测与耗电", 18, Typeface.BOLD);
        monitoringHeading.setIncludeFontPadding(false);
        monitoringHeading.setPadding(dp(18), dp(14), dp(18), dp(10));
        monitoringOptions.addView(monitoringHeading, new LinearLayout.LayoutParams(-1, -2));
        addPrefRow(monitoringOptions, createSwitchRow(
                "低功耗模式",
                "优先使用均衡蓝牙连接，并降低电量读取频率；心率通知仍按设备数据实时接收",
                preferences.getBoolean(KEY_LOW_POWER_MONITOR, true),
                (buttonView, enabled) -> {
                    preferences.edit().putBoolean(KEY_LOW_POWER_MONITOR, enabled).apply();
                    if (connected && bluetoothGatt != null) {
                        try { bluetoothGatt.requestConnectionPriority(enabled ? BluetoothGatt.CONNECTION_PRIORITY_BALANCED : BluetoothGatt.CONNECTION_PRIORITY_HIGH); } catch (Exception ignored) { }
                        handler.removeCallbacks(batteryPollRunnable);
                        handler.postDelayed(batteryPollRunnable, enabled ? 60000L : 10000L);
                    }
                    toast(enabled ? "低功耗模式已开启" : "已切换到响应优先模式");
                }));
        contentContainer.addView(monitoringOptions, marginParams(-1, -2, 0, 0, 0, 12));


        LinearLayout alertCard =
                createCard();

        alertCard.addView(
                createText(
                        "心率提醒",
                        20,
                        Typeface.BOLD
                )
        );

        Button threshold =
                createButton(
                        "设置心率提醒范围"
                );

        threshold.setOnClickListener(
                v -> showThresholdDialog()
        );

        alertCard.addView(
                threshold,
                marginParams(
                        -1,
                        dp(52),
                        0,
                        12,
                        0,
                        0
                )
        );
        alertCard.addView(createSwitchRow(
                "超出范围时通知我",
                "收到新心率后，低于/高于阈值会发送系统通知",
                preferences.getBoolean(KEY_HEART_ALERTS, false),
                (buttonView, enabled) -> {
                    preferences.edit().putBoolean(KEY_HEART_ALERTS, enabled).apply();
                    if (enabled && Build.VERSION.SDK_INT >= 33
                            && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
                    }
                    if (enabled) toast("心率范围提醒已开启");
                }), marginParams(-1, -2, 0, 8, 0, 0));

        contentContainer.addView(
                alertCard,
                marginParams(
                        -1,
                        -2,
                        0,
                        0,
                        0,
                        10
                )
        );


        LinearLayout overlayCard =
                createCard();

        overlayCard.addView(
                createText(
                        "悬浮窗",
                        20,
                        Typeface.BOLD
                )
        );

        TextView overlayDescription =
                createText(
                        "实时显示心率，可调整大小、透明度和锁定状态",
                        13,
                        Typeface.NORMAL
                );

        overlayDescription.setTextColor(
                secondaryTextColor
        );

        overlayCard.addView(
                overlayDescription,
                marginParams(
                        -1,
                        -2,
                        0,
                        3,
                        0,
                        10
                )
        );

        Button overlayButton =
                createButton(
                        preferences.getBoolean(
                                KEY_OVERLAY,
                                false
                        )
                                ? "悬浮窗：已开启"
                                : "悬浮窗：未开启"
                );

        overlayButton.setOnClickListener(
                v -> toggleOverlay()
        );

        overlayCard.addView(
                overlayButton,
                marginParams(
                        -1,
                        dp(48),
                        0,
                        0,
                        0,
                        10
                )
        );

        LinearLayout sizeHeader =
                new LinearLayout(this);

        sizeHeader.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView sizeTitle =
                createText(
                        "悬浮窗大小",
                        14,
                        Typeface.BOLD
                );

        TextView sizeValue =
                createText(
                        getOverlaySizePercent() + "%",
                        14,
                        Typeface.BOLD
                );

        sizeValue.setTextColor(
                accentColor
        );

        sizeValue.setGravity(
                Gravity.RIGHT |
                        Gravity.CENTER_VERTICAL
        );

        sizeHeader.addView(
                sizeTitle,
                new LinearLayout.LayoutParams(
                        0,
                        dp(32),
                        1
                )
        );

        sizeHeader.addView(
                sizeValue,
                new LinearLayout.LayoutParams(
                        dp(60),
                        dp(32)
                )
        );

        overlayCard.addView(
                sizeHeader,
                marginParams(
                        -1,
                        dp(32),
                        0,
                        0,
                        0,
                        0
                )
        );

        SeekBar sizeSeekBar =
                new SeekBar(this);

        sizeSeekBar.setMax(80);

        int currentSizeProgress =
                getOverlaySizePercent() - 50;

        currentSizeProgress =
                Math.max(
                        0,
                        Math.min(
                                80,
                                currentSizeProgress
                        )
                );

        sizeSeekBar.setProgress(
                currentSizeProgress
        );

        sizeSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        int percent =
                                50 + progress;

                        sizeValue.setText(
                                percent + "%"
                        );

                        float scale =
                                percent / 100.0f;

                        preferences.edit()
                                .putFloat(
                                        KEY_OVERLAY_SIZE,
                                        scale
                                )
                                .apply();

                        sendOverlaySettingsBroadcast();
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }
                }
        );

        overlayCard.addView(
                sizeSeekBar,
                marginParams(
                        -1,
                        dp(42),
                        0,
                        0,
                        0,
                        5
                )
        );

        LinearLayout alphaHeader =
                new LinearLayout(this);

        alphaHeader.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView alphaTitle =
                createText(
                        "悬浮窗透明度",
                        14,
                        Typeface.BOLD
                );

        TextView alphaValue =
                createText(
                        getOverlayAlphaPercent() + "%",
                        14,
                        Typeface.BOLD
                );

        alphaValue.setTextColor(
                accentColor
        );

        alphaValue.setGravity(
                Gravity.RIGHT |
                        Gravity.CENTER_VERTICAL
        );

        alphaHeader.addView(
                alphaTitle,
                new LinearLayout.LayoutParams(
                        0,
                        dp(32),
                        1
                )
        );

        alphaHeader.addView(
                alphaValue,
                new LinearLayout.LayoutParams(
                        dp(60),
                        dp(32)
                )
        );

        overlayCard.addView(
                alphaHeader,
                marginParams(
                        -1,
                        dp(32),
                        0,
                        5,
                        0,
                        0
                )
        );

        TextView alphaHint = createText("只调整胶囊底色和边缘透明度，爱心和数字始终保持实色", 12, Typeface.NORMAL);
        alphaHint.setTextColor(secondaryTextColor);
        overlayCard.addView(alphaHint, marginParams(-1, -2, 0, 0, 0, 2));

        SeekBar alphaSeekBar =
                new SeekBar(this);

        alphaSeekBar.setMax(70);

        int currentAlphaProgress =
                getOverlayAlphaPercent() - 30;

        currentAlphaProgress =
                Math.max(
                        0,
                        Math.min(
                                70,
                                currentAlphaProgress
                        )
                );

        alphaSeekBar.setProgress(
                currentAlphaProgress
        );

        alphaSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        int percent =
                                30 + progress;

                        alphaValue.setText(
                                percent + "%"
                        );

                        float alpha =
                                percent / 100.0f;

                        preferences.edit()
                                .putFloat(
                                        KEY_OVERLAY_ALPHA,
                                        alpha
                                )
                                .apply();

                        sendOverlaySettingsBroadcast();
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }
                }
        );

        overlayCard.addView(
                alphaSeekBar,
                marginParams(
                        -1,
                        dp(42),
                        0,
                        0,
                        0,
                        5
                )
        );

        boolean locked =
                preferences.getBoolean(
                        KEY_OVERLAY_LOCKED,
                        false
                );

        Button lockButton =
                createButton(
                        locked
                                ? "悬浮窗：已锁定"
                                : "悬浮窗：未锁定"
                );

        lockButton.setOnClickListener(
                v -> {

                    boolean newLocked =
                            !preferences.getBoolean(
                                    KEY_OVERLAY_LOCKED,
                                    false
                            );

                    preferences.edit()
                            .putBoolean(
                                    KEY_OVERLAY_LOCKED,
                                    newLocked
                            )
                            .apply();

                    lockButton.setText(
                            newLocked
                                    ? "悬浮窗：已锁定"
                                    : "悬浮窗：未锁定"
                    );

                    sendOverlaySettingsBroadcast();

                }
        );

        overlayCard.addView(
                lockButton,
                marginParams(
                        -1,
                        dp(48),
                        0,
                        5,
                        0,
                        8
                )
        );

        TextView lockDescription =
                createText(
                        locked
                                ? "已锁定：无法拖动悬浮窗，点击悬浮窗可打开 App"
                                : "未锁定：可以拖动悬浮窗位置",
                        12,
                        Typeface.NORMAL
                );

        lockDescription.setTextColor(
                secondaryTextColor
        );

        overlayCard.addView(
                lockDescription,
                marginParams(
                        -1,
                        -2,
                        0,
                        0,
                        0,
                        8
                )
        );

        Button overlayPermission =
                createButton(
                        "打开悬浮窗权限"
                );

        overlayPermission.setOnClickListener(
                v -> openOverlayPermission()
        );

        overlayCard.addView(
                overlayPermission,
                marginParams(
                        -1,
                        dp(48),
                        0,
                        8,
                        0,
                        0
                )
        );

        contentContainer.addView(
                overlayCard,
                marginParams(
                        -1,
                        -2,
                        0,
                        0,
                        0,
                        10
                )
        );

        // 外观与主题已合并进「主题设置」页
        LinearLayout themeEntry = createKsuCard();
        themeEntry.addView(createArrowRow("主题设置", "颜色、明暗模式、配色风格与底栏样式", themeSummary(),
                v -> showPage(PAGE_THEME)));
        contentContainer.addView(themeEntry, marginParams(-1, -2, 0, 0, 0, 10));

        // 开发工具默认隐藏；开启“显示开发者功能”后才展示，不删除原有模拟和诊断能力。
        if (preferences.getBoolean(KEY_SHOW_DEVELOPER, false)) {
        LinearLayout dataCard =
                createCard();

        dataCard.addView(
                createText(
                        "数据管理",
                        20,
                        Typeface.BOLD
                )
        );

        Button export =
                createButton(
                        "导出全部数据"
                );

        export.setOnClickListener(
                v -> exportData()
        );

        Button clear =
                createButton(
                        "清空全部数据"
                );

        clear.setOnClickListener(
                v -> confirmClearData()
        );

        dataCard.addView(
                export,
                marginParams(
                        -1,
                        dp(52),
                        0,
                        12,
                        0,
                        0
                )
        );
        Button importBackup = createButton("从 CSV 导入 / 合并历史数据");
        importBackup.setOnClickListener(v -> chooseCsvToImport());
        dataCard.addView(importBackup, marginParams(-1, dp(52), 0, 8, 0, 0));

        dataCard.addView(
                clear,
                marginParams(
                        -1,
                        dp(52),
                        0,
                        8,
                        0,
                        0
                )
        );

        contentContainer.addView(
                dataCard,
                marginParams(
                        -1,
                        -2,
                        0,
                        0,
                        0,
                        10
                )
        );

        }

        LinearLayout about = createCard();
        about.addView(createText("关于", 18, Typeface.BOLD));
        TextView aboutDesc = createText("通用 BLE 心率监测工具 · 版本 1.0", 13, Typeface.NORMAL);
        aboutDesc.setTextColor(secondaryTextColor);
        about.addView(aboutDesc, marginParams(-1, -2, 0, 4, 0, 12));

        LinearLayout wechatRow = new LinearLayout(this);
        wechatRow.setOrientation(LinearLayout.HORIZONTAL);
        wechatRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView wechatLabel = createText("微信", 14, Typeface.BOLD);
        TextView wechatValue = createText("shuangniano", 14, Typeface.NORMAL);
        wechatValue.setTextColor(secondaryTextColor);
        wechatRow.addView(wechatLabel, new LinearLayout.LayoutParams(0, dp(42), 1));
        wechatRow.addView(wechatValue, new LinearLayout.LayoutParams(-2, dp(42)));
        wechatRow.setPadding(dp(12), 0, dp(12), 0);
        setGlassBackground(wechatRow, darkMode ? Color.rgb(39,39,42) : Color.rgb(244,244,248), 14, dividerColor);
        wechatRow.setOnClickListener(v -> copyContact("shuangniano", "微信号已复制"));
        about.addView(wechatRow, marginParams(-1, dp(44), 0, 0, 0, 8));

        LinearLayout qqRow = new LinearLayout(this);
        qqRow.setOrientation(LinearLayout.HORIZONTAL);
        qqRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView qqLabel = createText("QQ", 14, Typeface.BOLD);
        TextView qqValue = createText("1420911614", 14, Typeface.NORMAL);
        qqValue.setTextColor(secondaryTextColor);
        qqRow.addView(qqLabel, new LinearLayout.LayoutParams(0, dp(42), 1));
        qqRow.addView(qqValue, new LinearLayout.LayoutParams(-2, dp(42)));
        qqRow.setPadding(dp(12), 0, dp(12), 0);
        setGlassBackground(qqRow, darkMode ? Color.rgb(39,39,42) : Color.rgb(244,244,248), 14, dividerColor);
        qqRow.setOnClickListener(v -> copyContact("1420911614", "QQ 号已复制"));
        applyPressFeedback(qqRow, 0.988f);
        about.addView(qqRow, marginParams(-1, dp(44), 0, 0, 0, 12));

        TextView aboutNote = createText("问题反馈、功能建议与兼容性问题都可以通过上方联系方式反馈。点击联系方式可复制。", 13, Typeface.NORMAL);
        aboutNote.setTextColor(secondaryTextColor);
        aboutNote.setLineSpacing(dp(2), 1f);
        about.addView(aboutNote, marginParams(-1, -2, 0, 0, 0, 12));

        addPrefRow(about, createArrowRow("原神官网", "点击打开原神官方网站", "访问官网", v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://ys.mihoyo.com/")));
            } catch (Exception ignored) {
                toast("未找到可打开网页的应用");
            }
        }));
        contentContainer.addView(about, marginParams(-1, -2, 0, 0, 0, 12));

        if (preferences.getBoolean(KEY_SHOW_DEVELOPER, false)) {
        LinearLayout devCard = createCard();
        devCard.addView(createText("开发者模式", 18, Typeface.BOLD));
        TextView devDesc = createText("开启后每秒随机模拟一个心率，实时心率、心率趋势、心电图用的是同一个数。连接真实设备时不模拟。", 13, Typeface.NORMAL);
        devDesc.setTextColor(secondaryTextColor);
        devDesc.setLineSpacing(dp(2), 1f);
        devCard.addView(devDesc, marginParams(-1, -2, 0, 4, 0, 12));
        final Button devButton = createButton(
                preferences.getBoolean(KEY_DEVELOPER, false) ? "开发者模式：已开启" : "开发者模式：已关闭");
        devButton.setOnClickListener(v -> {
            boolean on = !preferences.getBoolean(KEY_DEVELOPER, false);
            preferences.edit().putBoolean(KEY_DEVELOPER, on).apply();
            devButton.setText(on ? "开发者模式：已开启" : "开发者模式：已关闭");
            if (on) {
                startDevSim();
            } else {
                stopDevSim();
                clearEcgCharts();
            }
        });
        devCard.addView(devButton, marginParams(-1, dp(50), 0, 0, 0, 0));
        contentContainer.addView(devCard, marginParams(-1, -2, 0, 0, 0, 12));
        }
    }

    // =========================================================
    // 悬浮窗设置读取
    // =========================================================

    private void migrateOverlaySizePreference() {
        if (preferences.getBoolean(KEY_OVERLAY_SIZE_MIGRATION, false)) return;
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.contains(KEY_OVERLAY_SIZE)) {
            float oldSize = preferences.getFloat(KEY_OVERLAY_SIZE, 0.90f);
            // 旧版默认/常用尺寸偏大，首次升级时缩小约 10%，以后完全尊重用户调节。
            editor.putFloat(KEY_OVERLAY_SIZE, Math.max(0.50f, Math.min(1.30f, oldSize * 0.90f)));
        } else {
            editor.putFloat(KEY_OVERLAY_SIZE, 0.90f);
        }
        editor.putBoolean(KEY_OVERLAY_SIZE_MIGRATION, true).apply();
    }

    private int getOverlaySizePercent() {

        float size =
                preferences.getFloat(
                        KEY_OVERLAY_SIZE,
                        0.90f
                );

        size =
                Math.max(
                        0.50f,
                        Math.min(
                                1.30f,
                                size
                        )
                );

        return Math.round(
                size * 100f
        );
    }

    private int getOverlayAlphaPercent() {

        float alpha =
                preferences.getFloat(
                        KEY_OVERLAY_ALPHA,
                        0.92f
                );

        alpha =
                Math.max(
                        0.30f,
                        Math.min(
                                1.0f,
                                alpha
                        )
                );

        return Math.round(
                alpha * 100f
        );
    }

    private void sendOverlaySettingsBroadcast() {

        Intent intent =
                new Intent(
                        ACTION_OVERLAY_SETTINGS
                );

        intent.setPackage(
                getPackageName()
        );

        intent.putExtra(
                KEY_OVERLAY_SIZE,
                preferences.getFloat(
                        KEY_OVERLAY_SIZE,
                        1.0f
                )
        );

        intent.putExtra(
                KEY_OVERLAY_ALPHA,
                preferences.getFloat(
                        KEY_OVERLAY_ALPHA,
                        0.92f
                )
        );

        intent.putExtra(
                KEY_OVERLAY_LOCKED,
                preferences.getBoolean(
                        KEY_OVERLAY_LOCKED,
                        false
                )
        );

        sendBroadcast(intent);
    }

    // =========================================================
    // BLE 自动连接
    // =========================================================

    private void autoConnect() {

        if (shouldUseBackgroundBleService()) {
            ensureBackgroundBleService();
            return;
        }
        if (!hasBluetoothConnectPermission()) {
            requestRequiredPermissions();
            return;
        }

        if (bluetoothAdapter == null) {

            toast("设备不支持蓝牙功能");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {

            toast("请先开启手机蓝牙");
            return;
        }

        String address =
                preferences.getString(
                        KEY_DEVICE_ADDRESS,
                        ""
                );

        if (!address.isEmpty()) {

            try {

                BluetoothDevice device =
                        bluetoothAdapter
                                .getRemoteDevice(
                                        address
                                );

                connectDevice(device);
                return;

            } catch (Exception ignored) {
            }
        }

        startDeviceScan();
    }

    // =========================================================
    // BLE 扫描
    // =========================================================

    private void startDeviceScan() {

        if (!hasBluetoothPermission()) {
            scanAfterPermission = true;
            requestRequiredPermissions();
            return;
        }
        scanAfterPermission = false;

        if (bluetoothAdapter == null || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            lastBluetoothDiagnostic = "此手机没有可用的低功耗蓝牙（BLE）适配器";
            toast(lastBluetoothDiagnostic);
            return;
        }

        try {
            if (!bluetoothAdapter.isEnabled()) {
                lastBluetoothDiagnostic = "手机蓝牙未开启";
                toast("请先打开手机蓝牙，再回来点连接设备");
                try {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, 1002);
                } catch (Exception ignored) { }
                return;
            }
        } catch (SecurityException permissionError) {
            lastBluetoothDiagnostic = "系统拒绝读取蓝牙状态，请检查附近设备权限";
            toast(lastBluetoothDiagnostic);
            requestRequiredPermissions();
            return;
        }

        if (!isBleLocationReady()) {
            lastBluetoothDiagnostic = "系统定位开关已关闭，当前系统不会返回 BLE 扫描结果";
            toast("请先打开系统‘位置信息’开关，再扫描附近手环");
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } catch (Exception ignored) { }
            return;
        }

        if (scanning) return;

        try {
            bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        } catch (SecurityException permissionError) {
            bluetoothScanner = null;
        }
        if (bluetoothScanner == null) {
            lastBluetoothDiagnostic = "系统没有返回 BLE 扫描器；请确认蓝牙已开启后重试";
            toast(lastBluetoothDiagnostic);
            return;
        }

        discoveredDevices.clear();
        discoveredRssi.clear();
        discoveredNames.clear();
        handler.removeCallbacks(scanTimeoutRunnable);
        handler.removeCallbacks(scanEarlyFinishRunnable);
        scanning = true;
        lastBluetoothDiagnostic = "正在扫描 BLE 广播";

        try {
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .setReportDelay(0L)
                    .build();
            // 不加厂商/服务过滤器，避免漏掉使用私有服务 UUID 或不广播设备名称的手环。
            bluetoothScanner.startScan(null, settings, scanCallback);
        } catch (SecurityException permissionError) {
            scanning = false;
            lastBluetoothDiagnostic = "启动扫描时系统拒绝权限：" + permissionError.getClass().getSimpleName();
            toast("蓝牙扫描权限不足，请在应用权限中允许‘附近设备’和‘位置信息’");
            return;
        } catch (IllegalStateException | IllegalArgumentException scanError) {
            scanning = false;
            lastBluetoothDiagnostic = "启动 BLE 扫描失败：" + scanError.getClass().getSimpleName();
            toast("无法开始扫描：" + scanError.getClass().getSimpleName() + "，请关闭后重新打开蓝牙");
            return;
        }

        toast("正在快速扫描附近设备；发现设备后会立即显示");
        handler.postDelayed(scanTimeoutRunnable, 6000L);
    }

    /** Android 6–11 会在系统定位服务关闭时屏蔽 BLE 扫描；部分 Android 12+ 定制系统也如此。 */
    private boolean isBleLocationReady() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager == null) return true;
            if (Build.VERSION.SDK_INT >= 28) return locationManager.isLocationEnabled();
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
            // 某些定制 ROM 不允许查询该状态时，不阻断扫描，让系统回调提供真实错误。
            return true;
        }
    }

    private String scanErrorText(int code) {
        switch (code) {
            case ScanCallback.SCAN_FAILED_ALREADY_STARTED: return "扫描任务已启动";
            case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED: return "系统蓝牙服务注册失败，请切换蓝牙开关后重试";
            case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED: return "手机蓝牙芯片不支持此扫描模式";
            case ScanCallback.SCAN_FAILED_INTERNAL_ERROR: return "系统蓝牙内部错误，请关闭再打开蓝牙";
            case 5: return "扫描被系统限流，请等待几秒再试";
            default: return "未知扫描错误";
        }
    }

    private void stopScan() {
        handler.removeCallbacks(scanTimeoutRunnable);
        handler.removeCallbacks(scanEarlyFinishRunnable);
        if (!scanning) return;
        scanning = false;

        if (bluetoothScanner != null) {

            try {

                bluetoothScanner.stopScan(
                        scanCallback
                );

            } catch (Exception ignored) {
            }
        }

        if (!discoveredDevices.isEmpty()) {
            lastBluetoothDiagnostic = "扫描结束，共发现 " + discoveredDevices.size() + " 台 BLE 设备";
            showDeviceDialog();
        } else {
            lastBluetoothDiagnostic = "扫描完成但没有设备返回；请确认手环处于可连接状态且未被其他手机/运动健康 App 占用";
            toast("没有发现 BLE 设备。请确认手环已唤醒、靠近手机，并暂时断开其他运动健康 App");
        }
    }

    private final ScanCallback scanCallback =
            new ScanCallback() {

                @Override
                public void onScanResult(
                        int callbackType,
                        ScanResult result
                ) {

                    BluetoothDevice device =
                            result.getDevice();

                    if (device == null) {
                        return;
                    }

                    String address =
                            device.getAddress();

                    if (address == null) {
                        return;
                    }

                    boolean firstNearbyDevice = discoveredDevices.isEmpty()
                            && !discoveredRssi.containsKey(address);
                    if (!discoveredRssi.containsKey(address)) {
                        discoveredDevices.add(device);
                    }
                    discoveredRssi.put(address, result.getRssi());
                    if (firstNearbyDevice) {
                        // 低延迟模式已开启；首个设备出现后再收集约 1.3 秒，避免空等完整扫描窗口。
                        handler.removeCallbacks(scanEarlyFinishRunnable);
                        handler.postDelayed(scanEarlyFinishRunnable, 1300L);
                    }
                    String name = null;
                    try {
                        if (result.getScanRecord() != null) name = result.getScanRecord().getDeviceName();
                        if (name == null || name.trim().isEmpty()) name = device.getName();
                    } catch (SecurityException ignored) { }
                    if (name != null && !name.trim().isEmpty()) {
                        discoveredNames.put(address, name.trim());
                    } else if (!discoveredNames.containsKey(address)) {
                        discoveredNames.put(address, "未命名 BLE 设备");
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    scanning = false;
                    handler.removeCallbacks(scanTimeoutRunnable);
                    handler.removeCallbacks(scanEarlyFinishRunnable);
                    lastBluetoothDiagnostic = scanErrorText(errorCode) + "（错误码 " + errorCode + "）";
                    runOnUiThread(() -> toast("蓝牙扫描失败：" + lastBluetoothDiagnostic));
                }
            };

    // =========================================================
    // 设备选择
    // =========================================================

    private void showDeviceDialog() {
        if (isFinishing() || discoveredDevices.isEmpty()) return;

        final Dialog dialog = new Dialog(this);
        LinearLayout panel = createCard();
        // 设备选择弹窗跟随主题效果；第二个独立开关专门控制底部页面切换栏。
        glassSurfaceSpecs.remove(panel);
        applySelectorGlassBackground(panel, cardColor, 22);
        panel.setElevation(dp(isSelectorLiquidGlassEnabled() ? 2 : 0));
        panel.setPadding(dp(18), dp(16), dp(18), dp(16));

        TextView title = createText("选择蓝牙设备", 21, Typeface.BOLD);
        panel.addView(title, marginParams(-1, dp(36), 0, 0, 0, 2));
        TextView summary = createText("已发现 " + discoveredDevices.size() + " 台附近设备，请选择要直连的手环。", 12, Typeface.NORMAL);
        summary.setTextColor(secondaryTextColor);
        panel.addView(summary, marginParams(-1, -2, 0, 0, 0, 12));

        ScrollView listScroll = new ScrollView(this);
        listScroll.setFillViewport(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < discoveredDevices.size(); i++) {
            final BluetoothDevice device = discoveredDevices.get(i);
            final String address = device.getAddress();
            Integer foundRssi = discoveredRssi.get(address);
            final String rssiText = foundRssi == null ? "信号未知" : foundRssi + " dBm";

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(13), dp(10), dp(13), dp(10));
            applySelectorGlassBackground(row, darkMode ? Color.rgb(39, 40, 44) : Color.rgb(249, 250, 253), 15);

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            String scannedName = discoveredNames.get(address);
            TextView deviceNameText = createText(
                    scannedName == null || scannedName.trim().isEmpty() ? getDeviceName(device) : scannedName,
                    15, Typeface.BOLD);
            TextView addressText = createText(address == null ? "地址不可用" : address, 11, Typeface.NORMAL);
            addressText.setTextColor(secondaryTextColor);
            info.addView(deviceNameText);
            info.addView(addressText, marginParams(-1, -2, 0, 2, 0, 0));
            row.addView(info, new LinearLayout.LayoutParams(0, -2, 1f));

            TextView signal = createText(rssiText, 11, Typeface.BOLD);
            signal.setTextColor(accentColor);
            signal.setGravity(Gravity.CENTER);
            signal.setPadding(dp(8), dp(5), dp(8), dp(5));
            applySelectorGlassBackground(signal, blendColors(cardColor, accentColor, 0.90f), 12);
            row.addView(signal, new LinearLayout.LayoutParams(-2, -2));

            row.setOnClickListener(v -> {
                dialog.dismiss();
                connectDevice(device);
            });
            applyPressFeedback(row, 0.985f);
            list.addView(row, marginParams(-1, -2, 0, 0, 0, 8));
        }
        listScroll.addView(list);
        panel.addView(listScroll, new LinearLayout.LayoutParams(-1, Math.min(dp(330), dp(86) * discoveredDevices.size())));

        Button cancel = createButton("取消");
        cancel.setOnClickListener(v -> dialog.dismiss());
        panel.addView(cancel, marginParams(-1, dp(46), 0, 8, 0, 0));

        dialog.setContentView(panel);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            window.setLayout(Math.min(screenWidth - dp(32), dp(420)), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();
        if (dialog.getWindow() != null) {
            Window popupWindow = dialog.getWindow();
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            popupWindow.setBackgroundDrawableResource(android.R.color.transparent);
            popupWindow.setLayout(Math.min(screenWidth - dp(32), dp(420)), ViewGroup.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams popupParams = popupWindow.getAttributes();
            popupParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            popupParams.y = dp(72);
            popupWindow.setAttributes(popupParams);
        }
    }

    // =========================================================
    // BLE 连接
    // =========================================================

    private void connectDevice(
            BluetoothDevice device
    ) {

        if (device == null) {
            return;
        }

        if (!hasBluetoothConnectPermission()) {

            requestRequiredPermissions();
            return;
        }

        stopScan();
        userRequestedDisconnect = false;
        handler.removeCallbacks(reconnectRunnable);

        connectedName =
                getDeviceName(device);

        connectedAddress =
                device.getAddress();

        preferences.edit()
                .putString(
                        KEY_DEVICE_ADDRESS,
                        connectedAddress
                )
                .putString(
                        KEY_DEVICE_NAME,
                        connectedName
                )
                .apply();
        saveKnownDevice(connectedName, connectedAddress);

        toast("正在连接设备：" + connectedName);

        try {
            lastBluetoothDiagnostic = "正在连接 " + connectedName + "（" + connectedAddress + "）";
            handler.removeCallbacks(connectTimeoutRunnable);
            handler.removeCallbacks(serviceDiscoveryTimeoutRunnable);
            servicesDiscovered = false;
            resetGattOperationQueue();
            if (bluetoothGatt != null) {
                BluetoothGatt oldGatt = bluetoothGatt;
                bluetoothGatt = null;
                try { oldGatt.disconnect(); } catch (Exception ignored) { }
                try { oldGatt.close(); } catch (Exception ignored) { }
            }
            connected = false;
            currentRssi = 0;
            updateStatusViews();

            bluetoothGatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
            if (bluetoothGatt != null && preferences != null) {
                preferences.edit().putLong(KEY_ACTIVITY_GATT_LAST_SEEN, System.currentTimeMillis()).apply();
            }
            if (bluetoothGatt == null) {
                lastBluetoothDiagnostic = "系统未创建 BLE GATT 连接对象";
                toast("系统没有创建 BLE 连接，请重新扫描设备");
                scheduleReconnect();
            } else {
                handler.postDelayed(connectTimeoutRunnable, 18000L);
            }
        } catch (SecurityException permissionError) {
            lastBluetoothDiagnostic = "连接被系统拒绝：缺少蓝牙连接权限";
            toast(lastBluetoothDiagnostic);
            requestRequiredPermissions();
        } catch (Exception error) {
            lastBluetoothDiagnostic = "创建 BLE 连接失败：" + error.getClass().getSimpleName();
            toast("蓝牙连接发起失败：" + error.getClass().getSimpleName());
            scheduleReconnect();
        }
    }

    private String getDeviceName(
            BluetoothDevice device
    ) {

        try {

            String name =
                    device.getName();

            if (name != null &&
                    !name.trim().isEmpty()) {

                return name;
            }

        } catch (Exception ignored) {
        }

        try {
            String address = device.getAddress();
            String scanned = discoveredNames.get(address);
            if (scanned != null && !scanned.trim().isEmpty() && !"未命名 BLE 设备".equals(scanned)) return scanned;
            if (address != null && address.equals(connectedAddress)
                    && connectedName != null && !connectedName.trim().isEmpty()
                    && !"未连接".equals(connectedName)) return connectedName;
        } catch (Exception ignored) { }
        return "BLE 设备（未广播名称）";
    }

    // =========================================================
    // GATT
    // =========================================================

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (gatt != bluetoothGatt) {
                try { gatt.close(); } catch (Exception ignored) { }
                return;
            }
            runOnUiThread(() -> {
                if (gatt != bluetoothGatt) return;
                if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                    handler.removeCallbacks(connectTimeoutRunnable);
                    handler.removeCallbacks(serviceDiscoveryTimeoutRunnable);
                    servicesDiscovered = false;
                    connected = true;
                    preferences.edit().putLong(KEY_ACTIVITY_GATT_LAST_SEEN, System.currentTimeMillis())
                            .putBoolean(HeartControlWidgetProvider.KEY_WIDGET_CONNECTED, true).apply();
                    HeartControlWidgetProvider.refreshWidgets(MainActivity.this);
                    reconnectAttempts = 0;
                    connectedName = getDeviceName(gatt.getDevice());
                    connectedAddress = gatt.getDevice().getAddress();
                    lastBluetoothDiagnostic = "BLE 已连接，等待服务发现";
                    if (monitoring) updateMonitoringServiceNotification(false);
                    updateStatusViews();
                    toast("蓝牙链路已连接，正在读取设备服务…");
                    try {
                        if (Build.VERSION.SDK_INT >= 21) {
                            int priority = preferences.getBoolean(KEY_LOW_POWER_MONITOR, true)
                                    ? BluetoothGatt.CONNECTION_PRIORITY_BALANCED
                                    : BluetoothGatt.CONNECTION_PRIORITY_HIGH;
                            try { gatt.requestConnectionPriority(priority); } catch (Exception ignored) { }
                        }
                        if (hasBluetoothConnectPermission()) {
                            boolean started = gatt.discoverServices();
                            if (started) {
                                handler.postDelayed(serviceDiscoveryTimeoutRunnable, 12000L);
                            } else {
                                lastBluetoothDiagnostic = "Android 未接受服务发现请求";
                                toast("连接已建立，但系统没有启动服务发现，请重试连接");
                                handler.postDelayed(() -> {
                                    if (gatt == bluetoothGatt && connected) {
                                        try { gatt.disconnect(); } catch (Exception ignored) { }
                                    }
                                }, 900L);
                            }
                        }
                    } catch (SecurityException permissionError) {
                        lastBluetoothDiagnostic = "读取蓝牙服务被权限拒绝";
                        toast("连接成功，但没有读取设备服务的权限");
                    } catch (Exception discoveryError) {
                        lastBluetoothDiagnostic = "服务发现异常：" + discoveryError.getClass().getSimpleName();
                        toast("连接成功，但读取设备服务失败：" + discoveryError.getClass().getSimpleName());
                    }
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED || status != BluetoothGatt.GATT_SUCCESS) {
                    handler.removeCallbacks(connectTimeoutRunnable);
                    handler.removeCallbacks(serviceDiscoveryTimeoutRunnable);
                    servicesDiscovered = false;
                    connected = false;
                    if (preferences != null) preferences.edit().putBoolean(HeartControlWidgetProvider.KEY_WIDGET_CONNECTED, false).apply();
                    HeartControlWidgetProvider.refreshWidgets(MainActivity.this);
                    if (monitoring) updateMonitoringServiceNotification(false);
                    clearEcgCharts();
                    handler.removeCallbacks(batteryPollRunnable);
                    resetGattOperationQueue();
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        lastBluetoothDiagnostic = "GATT 连接断开，错误码 " + status + "：" + gattStatusText(status);
                        toast("蓝牙连接失败（" + status + "）：" + gattStatusText(status));
                    } else {
                        lastBluetoothDiagnostic = "设备主动断开连接";
                    }
                    updateStatusViews();
                    try { gatt.close(); } catch (Exception ignored) { }
                    if (gatt == bluetoothGatt) bluetoothGatt = null;
                    if (!userRequestedDisconnect && !connectedAddress.isEmpty()) scheduleReconnect();
                }
            });
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (gatt != bluetoothGatt) return;
            handler.removeCallbacks(serviceDiscoveryTimeoutRunnable);
            servicesDiscovered = status == BluetoothGatt.GATT_SUCCESS;
            if (status != BluetoothGatt.GATT_SUCCESS) {
                lastBluetoothDiagnostic = "服务发现失败，GATT 错误码 " + status;
                serviceDumpText = "服务发现失败，错误码：" + status + "。请确认手环未被其他 App 占用，然后断开并重新连接。";
                runOnUiThread(() -> {
                    toast("设备已连接，但服务发现失败（" + status + "）；请先关闭其他运动健康 App 后重试");
                    refreshDevicePage();
                });
                handler.postDelayed(() -> {
                    if (gatt == bluetoothGatt && connected) {
                        try { gatt.disconnect(); } catch (Exception ignored) { }
                    }
                }, 700L);
                return;
            }

            lastBluetoothDiagnostic = "已发现 " + gatt.getServices().size() + " 项 GATT 服务";
            serviceDumpText = buildServiceDump(gatt);
            boolean isXiaomiBand = gatt.getService(MI_FEE0_SERVICE) != null || gatt.getService(MI_FEE1_SERVICE) != null;

            BluetoothGattService heartService = gatt.getService(HEART_RATE_SERVICE);
            if (heartService != null) {
                BluetoothGattCharacteristic measurement = heartService.getCharacteristic(HEART_RATE_MEASUREMENT);
                if (measurement != null) enableHeartRateNotification(gatt, measurement);
                // Mi Band 2/3 等设备把测量控制放在标准 2A39 UUID 中，但指令是厂商协议。
                // 只对识别到的小米 FEE0/FEE1 服务发送该指令，避免误操作其他厂商设备。
                BluetoothGattCharacteristic control = heartService.getCharacteristic(HEART_RATE_CONTROL_POINT);
                if (isXiaomiBand && monitoring && control != null) {
                    queueCharacteristicWrite(gatt, control, new byte[]{0x15, 0x01});
                }
            }

            // 订阅并读取标准电量，以及旧款 FF0C / 3512 私有电量特征。
            for (BluetoothGattService service : gatt.getServices()) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    UUID uuid = characteristic.getUuid();
                    if (BATTERY_LEVEL.equals(uuid) || MI_BATTERY_CHARACTERISTIC.equals(uuid)
                            || MI_BAND2_BATTERY_CHARACTERISTIC.equals(uuid)) {
                        enableBatteryNotification(gatt, characteristic);
                        queueCharacteristicRead(gatt, characteristic);
                    }
                    if (MI_LEGACY_STEP_CHARACTERISTIC.equals(uuid) || MI_BAND2_STEP_CHARACTERISTIC.equals(uuid)) {
                        enableCharacteristicNotification(gatt, characteristic);
                        queueCharacteristicRead(gatt, characteristic);
                    }
                    if (isXiaomiBand && MI_LEGACY_DEVICE_INFO_CHARACTERISTIC.equals(uuid)) {
                        queueCharacteristicRead(gatt, characteristic);
                    }
                    if (isXiaomiBand && MI_LEGACY_LE_PARAMS_CHARACTERISTIC.equals(uuid)) {
                        queueCharacteristicRead(gatt, characteristic);
                    }
                    if (isXiaomiBand && MI_LEGACY_DATE_TIME_CHARACTERISTIC.equals(uuid)) {
                        // 按旧款小米手环 FF0A 格式同步手机本地时间，避免手环时间长期漂移。
                        queueCharacteristicWrite(gatt, characteristic, buildLegacyDateTimePacket());
                    }
                    // 旧款手环会通过这些通知特征发送状态、活动或传感器数据。
                    // 这里只订阅，不把未知二进制包误当成心率；真实心率仍按 2A37 解析。
                    if (MI_LEGACY_NOTIFY_CHARACTERISTIC.equals(uuid)
                            || MI_LEGACY_ACTIVITY_CHARACTERISTIC.equals(uuid)
                            || MI_LEGACY_SENSOR_CHARACTERISTIC.equals(uuid)) {
                        enableCharacteristicNotification(gatt, characteristic);
                    }
                }
            }

            BluetoothGattService infoService = gatt.getService(DEVICE_INFORMATION_SERVICE);
            if (infoService != null) readDeviceInfo(gatt, infoService);
            handler.removeCallbacks(batteryPollRunnable);
            long firstBatteryPoll = preferences != null && preferences.getBoolean(KEY_LOW_POWER_MONITOR, true) ? 5000L : 1200L;
            handler.postDelayed(batteryPollRunnable, firstBatteryPoll);
            try { gatt.readRemoteRssi(); } catch (Exception ignored) { }
            runOnUiThread(() -> {
                if (monitoring) setXiaomiHeartRateMode(true);
                if (preferences.getBoolean(HeartControlWidgetProvider.KEY_WIDGET_PENDING_START, false)) {
                    preferences.edit().putBoolean(HeartControlWidgetProvider.KEY_WIDGET_PENDING_START, false).apply();
                    if (!monitoring) startMonitoring();
                    else updateMonitoringServiceNotification(true);
                    if (preferences.getBoolean(KEY_OVERLAY, false) && Settings.canDrawOverlays(MainActivity.this)) startOverlayService();
                }
                refreshDevicePage();
                updateStatusViews();
                if (monitoring) updateMonitoringServiceNotification(false);
                HeartControlWidgetProvider.refreshWidgets(MainActivity.this);
                if (heartService == null || heartService.getCharacteristic(HEART_RATE_MEASUREMENT) == null) {
                    lastBluetoothDiagnostic = "连接成功，但未发现标准 180D/2A37 实时心率特征。该型号可能需要私有协议/认证。";
                    toast("蓝牙已连接，但没有标准实时心率通道；打开‘设备 → 查看蓝牙服务列表’可以诊断具体型号");
                } else {
                    lastBluetoothDiagnostic = "已发现标准心率服务并请求订阅通知；等待设备发送真实数据";
                }
                serviceDumpText = buildServiceDump(gatt);
            });
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            handleCharacteristicData(gatt, characteristic, characteristic.getValue());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
            // Android 13+ 推荐回调：直接使用系统交付的本次数据快照，避免读取到下一包覆盖后的 value。
            handleCharacteristicData(gatt, characteristic, value);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) handleCharacteristicDataRead(gatt, characteristic, characteristic.getValue());
            finishGattOperation(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) handleCharacteristicDataRead(gatt, characteristic, value);
            finishGattOperation(gatt);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            finishGattOperation(gatt);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            finishGattOperation(gatt);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (gatt == bluetoothGatt && status == BluetoothGatt.GATT_SUCCESS) {
                currentRssi = rssi;
                runOnUiThread(() -> updateStatusViews());
            }
        }
    };

    private String gattStatusText(int status) {
        switch (status) {
            case 8: return "连接超时";
            case 19: return "远端设备主动关闭连接";
            case 22: return "本机蓝牙栈关闭连接";
            case 133: return "系统 GATT 通用错误；常见原因是设备已被其他 App 占用、手环未唤醒或系统连接缓存异常";
            default: return "请检查手环是否靠近、唤醒且未连接其他手机";
        }
    }

    // =========================================================
    // BLE GATT 串行操作队列 / 通知订阅 / 电量读取
    // =========================================================

    private void enqueueGattOperation(BluetoothGatt gatt, GattOperation operation) {
        if (gatt == null || operation == null) return;
        handler.post(() -> {
            if (gatt != bluetoothGatt || !connected) return;
            gattOperationQueue.addLast(operation);
            drainGattOperationQueue();
        });
    }

    private void drainGattOperationQueue() {
        if (gattOperationRunning || gattOperationQueue.isEmpty()) return;
        BluetoothGatt gatt = bluetoothGatt;
        if (gatt == null || !connected || !hasBluetoothConnectPermission()) {
            gattOperationQueue.clear();
            return;
        }
        GattOperation operation = gattOperationQueue.pollFirst();
        gattOperationRunning = true;
        activeOperationGatt = gatt;
        final int generation = ++gattOperationGeneration;
        boolean started;
        try {
            started = operation.start(gatt);
        } catch (Exception error) {
            started = false;
        }
        if (!started) {
            handler.postDelayed(() -> completeGattOperation(gatt, generation), 120L);
        } else {
            // 部分旧手环不会回调某些操作；超时后继续队列，避免整个数据通道永久堵死。
            handler.postDelayed(() -> completeGattOperation(gatt, generation), 7000L);
        }
    }

    private void finishGattOperation(BluetoothGatt gatt) {
        handler.post(() -> {
            if (!gattOperationRunning || activeOperationGatt != gatt) return;
            completeGattOperation(gatt, gattOperationGeneration);
        });
    }

    private void completeGattOperation(BluetoothGatt gatt, int generation) {
        if (!gattOperationRunning || activeOperationGatt != gatt || generation != gattOperationGeneration) return;
        gattOperationRunning = false;
        activeOperationGatt = null;
        handler.postDelayed(this::drainGattOperationQueue, 55L);
    }

    private void resetGattOperationQueue() {
        handler.post(() -> {
            gattOperationQueue.clear();
            gattOperationRunning = false;
            activeOperationGatt = null;
            gattOperationGeneration++;
        });
    }

    private void queueCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic == null || (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) == 0) return;
        enqueueGattOperation(gatt, target -> target.readCharacteristic(characteristic));
    }

    private void queueCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value) {
        if (characteristic == null || value == null) return;
        enqueueGattOperation(gatt, target -> {
            int properties = characteristic.getProperties();
            if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            } else if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            } else {
                return false;
            }
            characteristic.setValue(value);
            return target.writeCharacteristic(characteristic);
        });
    }

    private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) return;
        int properties = characteristic.getProperties();
        if ((properties & (BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_INDICATE)) == 0) return;
        enqueueGattOperation(gatt, target -> {
            if (!target.setCharacteristicNotification(characteristic, true)) return false;
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CONFIG);
            if (descriptor == null) return false;
            byte[] enableValue = (properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
                    && (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0
                    ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    : BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            descriptor.setValue(enableValue);
            return target.writeDescriptor(descriptor);
        });
    }

    private void enableHeartRateNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        enableCharacteristicNotification(gatt, characteristic);
    }

    private void enableBatteryNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        enableCharacteristicNotification(gatt, characteristic);
    }

    private void requestBatteryLevel(BluetoothGatt gatt) {
        if (gatt == null || !connected || gatt != bluetoothGatt || !hasBluetoothConnectPermission()) return;
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                UUID uuid = characteristic.getUuid();
                if (BATTERY_LEVEL.equals(uuid) || MI_BATTERY_CHARACTERISTIC.equals(uuid)
                        || MI_BAND2_BATTERY_CHARACTERISTIC.equals(uuid)) {
                    queueCharacteristicRead(gatt, characteristic);
                }
            }
        }
    }

    private void setXiaomiHeartRateMode(boolean enable) {
        if (bluetoothGatt == null) {
            if (shouldUseBackgroundBleService()) {
                Intent mode = new Intent(HeartMonitoringService.ACTION_SET_HEART_RATE_MODE).setPackage(getPackageName());
                mode.putExtra(HeartMonitoringService.EXTRA_ENABLE_HEART_RATE_MODE, enable);
                sendBroadcast(mode);
            }
            return;
        }
        if (!connected) return;
        boolean xiaomi = bluetoothGatt.getService(MI_FEE0_SERVICE) != null || bluetoothGatt.getService(MI_FEE1_SERVICE) != null;
        if (!xiaomi) return;
        BluetoothGattService hrService = bluetoothGatt.getService(HEART_RATE_SERVICE);
        if (hrService == null) return;
        BluetoothGattCharacteristic control = hrService.getCharacteristic(HEART_RATE_CONTROL_POINT);
        if (control == null) return;
        // Mi Band 2 协议：15 01=连续心率，15 00=停止；只对已识别的小米服务使用。
        queueCharacteristicWrite(bluetoothGatt, control, new byte[]{0x15, (byte) (enable ? 0x01 : 0x00)});
    }

    private void handleCharacteristicData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] data) {
        if (gatt != bluetoothGatt || characteristic == null || data == null) return;
        UUID uuid = characteristic.getUuid();
        recordBlePacket(uuid, data);
        if (BATTERY_LEVEL.equals(uuid) || MI_BATTERY_CHARACTERISTIC.equals(uuid) || MI_BAND2_BATTERY_CHARACTERISTIC.equals(uuid)) {
            int level = parseBatteryLevel(uuid, data);
            if (level >= 0) {
                currentBattery = level;
                runOnUiThread(this::updateStatusViews);
            }
            return;
        }
        if (MI_LEGACY_STEP_CHARACTERISTIC.equals(uuid) || MI_BAND2_STEP_CHARACTERISTIC.equals(uuid)) {
            int steps = parseStepCount(data);
            if (steps >= 0) {
                currentSteps = steps;
                runOnUiThread(this::updateStatusViews);
            }
            return;
        }
        if (HEART_RATE_MEASUREMENT.equals(uuid)) {
            int heart = parseHeartRate(data);
            if (heart >= 25 && heart <= 240) sendHeartRate(heart);
        }
    }

    private void handleCharacteristicDataRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] data) {
        if (gatt != bluetoothGatt || characteristic == null || data == null) return;
        UUID uuid = characteristic.getUuid();
        recordBlePacket(uuid, data);
        if (BATTERY_LEVEL.equals(uuid) || MI_BATTERY_CHARACTERISTIC.equals(uuid) || MI_BAND2_BATTERY_CHARACTERISTIC.equals(uuid)) {
            int level = parseBatteryLevel(uuid, data);
            if (level >= 0) currentBattery = level;
        } else if (MI_LEGACY_STEP_CHARACTERISTIC.equals(uuid) || MI_BAND2_STEP_CHARACTERISTIC.equals(uuid)) {
            int steps = parseStepCount(data);
            if (steps >= 0) currentSteps = steps;
        } else if (MANUFACTURER.equals(uuid)) {
            manufacturer = bytesToString(data);
        } else if (MODEL_NUMBER.equals(uuid)) {
            model = bytesToString(data);
        } else if (FIRMWARE.equals(uuid)) {
            firmware = bytesToString(data);
        }
        runOnUiThread(() -> {
            updateStatusViews();
            refreshDevicePage();
        });
    }

    private byte[] buildLegacyDateTimePacket() {
        Calendar now = Calendar.getInstance();
        return new byte[]{
                (byte) (now.get(Calendar.YEAR) - 2000),
                (byte) (now.get(Calendar.MONTH) + 1),
                (byte) now.get(Calendar.DAY_OF_MONTH),
                (byte) now.get(Calendar.HOUR_OF_DAY),
                (byte) now.get(Calendar.MINUTE),
                (byte) now.get(Calendar.SECOND),
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff
        };
    }

    private void recordBlePacket(UUID uuid, byte[] data) {
        if (uuid == null || data == null || data.length == 0) return;
        synchronized (latestBlePackets) {
            receivedBlePacketCount++;
            String key = uuid.toString();
            latestBlePackets.remove(key);
            latestBlePackets.put(key, bytesToHex(data));
            while (latestBlePackets.size() > 24) {
                String oldest = latestBlePackets.keySet().iterator().next();
                latestBlePackets.remove(oldest);
            }
        }
        handler.post(() -> {
            if (devicePacketCount != null) devicePacketCount.setText(receivedBlePacketCount + " 包");
        });
    }

    private String bytesToHex(byte[] data) {
        if (data == null || data.length == 0) return "--";
        StringBuilder out = new StringBuilder(data.length * 3);
        for (byte b : data) {
            if (out.length() > 0) out.append(' ');
            out.append(String.format(Locale.US, "%02X", b & 0xff));
        }
        return out.toString();
    }

    private int parseBatteryLevel(UUID uuid, byte[] data) {
        if (data == null || data.length == 0) return -1;
        int level;
        if (MI_BAND2_BATTERY_CHARACTERISTIC.equals(uuid) && data.length >= 2 && (data[0] & 0xff) == 0x0f) {
            // Mi Band 2/3 battery packet: 0F, percentage, charging state, date/time ...
            level = data[1] & 0xff;
        } else {
            level = data[0] & 0xff;
        }
        return level >= 0 && level <= 100 ? level : -1;
    }

    private int parseStepCount(byte[] data) {
        if (data == null || data.length < 4) return -1;
        long value = ((long) data[0] & 0xffL)
                | (((long) data[1] & 0xffL) << 8)
                | (((long) data[2] & 0xffL) << 16)
                | (((long) data[3] & 0xffL) << 24);
        return value >= 0 && value <= 2000000L ? (int) value : -1;
    }

    private void readDeviceInfo(BluetoothGatt gatt, BluetoothGattService service) {
        if (gatt == null || service == null) return;
        queueCharacteristicRead(gatt, service.getCharacteristic(MANUFACTURER));
        queueCharacteristicRead(gatt, service.getCharacteristic(MODEL_NUMBER));
        queueCharacteristicRead(gatt, service.getCharacteristic(FIRMWARE));
    }

    // =========================================================
    // 心率解析
    // =========================================================

    private int parseHeartRate(
            byte[] data
    ) {

        if (data == null ||
                data.length < 2) {
            return 0;
        }

        int flags =
                data[0] & 0xff;

        boolean sixteenBit =
                (flags & 0x01) != 0;

        if (sixteenBit) {

            if (data.length < 3) {
                return 0;
            }

            return (data[1] & 0xff)
                    |
                    ((data[2] & 0xff) << 8);

        } else {

            return data[1] & 0xff;
        }
    }

    // =========================================================
    // 心率处理
    // =========================================================

    private void sendHeartRate(int heartRate) {
        // BLE GATT 的 2A37 通知直接进入主线程，更新主界面、数据库、趋势图、心电图和悬浮窗。
        // 不发送也不接收 HEART_RATE 广播，避免蓝牙数据依赖广播中转。
        if (Looper.myLooper() == Looper.getMainLooper()) {
            updateHeartRate(heartRate);
        } else {
            handler.post(() -> updateHeartRate(heartRate));
        }
    }

    /** 断开连接后清空心电图，只显示横线。 */
    private void clearEcgCharts() {
        for (EcgChart ecg : ecgCharts) ecg.clearSamples();
    }

    /** Update the foreground UI from the service without inserting the same BLE sample twice. */
    private void updateHeartRateFromService(int heartRate) {
        if (heartRate <= 0 || heartRate > 250) return;
        currentHeartRate = heartRate;
        for (int i = ecgCharts.size() - 1; i >= 0; i--) {
            EcgChart ecg = ecgCharts.get(i);
            if (ecg == null || !ecg.isShown()) ecgCharts.remove(i);
            else ecg.onHeartRateSample(heartRate);
        }
        if (homeHeart != null) homeHeart.setText(String.valueOf(heartRate));
        if (monitorHeart != null) monitorHeart.setText(heartRate + " BPM");
        if (chart != null) chart.addValue(heartRate);
        if (currentPage == PAGE_DATA) refreshDataRange();
        updateHomeStats();
    }

    private void updateHeartRate(
            int heartRate
    ) {

        currentHeartRate = heartRate;
        // Persist the real sample once for app history, widget trend, and overlay display.
        // The widget graph is driven by incoming BLE measurements, never by a fake animation.
        HeartControlWidgetProvider.recordHeartRate(this, heartRate);
        maybeNotifyHeartRateThreshold(heartRate);
        if (monitoring) updateMonitoringServiceNotification(false);

        // 仅在设备真正上报一条新心率时触发一次心电样式脉冲，不做循环播放。
        for (int i = ecgCharts.size() - 1; i >= 0; i--) {
            EcgChart ecg = ecgCharts.get(i);
            if (ecg == null || !ecg.isShown()) {
                ecgCharts.remove(i);
            } else {
                ecg.onHeartRateSample(heartRate);
            }
        }

        HeartDatabase db =
                new HeartDatabase(this);

        db.insertHeartRate(
                heartRate,
                System.currentTimeMillis()
        );
        if (currentPage == PAGE_DATA) refreshDataRange();

        if (homeHeart != null) {

            homeHeart.setText(
                    String.valueOf(
                            heartRate
                    )
            );
        }

        if (monitorHeart != null) {

            monitorHeart.setText(
                    heartRate +
                            " BPM"
            );
        }

        if (chart != null) {
            chart.addValue(heartRate);
        }
        updateHomeStats();

        // =====================================================
        // 关键修复：
        // 悬浮窗不再依赖 monitoring。
        // 只要悬浮窗开关打开，就实时更新。
        // =====================================================

        if (preferences.getBoolean(
                KEY_OVERLAY,
                false
        )) {

            updateOverlay(
                    heartRate
            );
        }
    }

    private void updateOverlay(
            int heartRate
    ) {

        Intent intent =
                new Intent(
                        "com.shuangnian.UPDATE_OVERLAY"
                );

        intent.setPackage(
                getPackageName()
        );

        intent.putExtra(
                "heart_rate",
                heartRate
        );

        sendBroadcast(intent);
    }

    // =========================================================
    // 监测
    // =========================================================

    private void updateMonitoringServiceNotification(boolean start) {
        if (preferences == null || !preferences.getBoolean(KEY_MONITOR, monitoring)) return;
        Intent intent = start
                ? new Intent(this, HeartMonitoringService.class).setAction(HeartMonitoringService.ACTION_START)
                : new Intent(HeartMonitoringService.ACTION_UPDATE).setPackage(getPackageName());
        intent.putExtra(HeartMonitoringService.EXTRA_HEART_RATE, currentHeartRate);
        intent.putExtra(HeartMonitoringService.EXTRA_DEVICE_NAME, connected ? connectedName : preferences.getString(KEY_DEVICE_NAME, connectedName));
        intent.putExtra(HeartMonitoringService.EXTRA_CONNECTED, connected);
        try {
            if (start) {
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent); else startService(intent);
            } else {
                // Send a package-scoped broadcast for ordinary updates; do not try to launch a foreground service on every beat.
                sendBroadcast(intent);
            }
        } catch (Exception ignored) {
            // If the OS blocks the foreground service, the core BLE UI and local storage still remain usable.
        }
    }

    private void stopMonitoringForegroundService() {
        // 悬浮窗单独开启时仍依赖后台 BLE 心率更新，不能随着“停止监测”把它的连接服务一并杀掉。
        if (preferences != null && preferences.getBoolean(KEY_OVERLAY, false)) {
            Intent keepOverlayFeed = new Intent(this, HeartMonitoringService.class)
                    .setAction(HeartMonitoringService.ACTION_START);
            try {
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(keepOverlayFeed);
                else startService(keepOverlayFeed);
            } catch (Exception ignored) { }
            return;
        }
        try { stopService(new Intent(this, HeartMonitoringService.class)); } catch (Exception ignored) { }
    }

    private void maybeNotifyHeartRateThreshold(int heartRate) {
        if (preferences == null || !preferences.getBoolean(KEY_HEART_ALERTS, false) || heartRate <= 0) return;
        int low = preferences.getInt("low_threshold", 50);
        int high = preferences.getInt("high_threshold", 120);
        if (heartRate >= low && heartRate <= high) return;
        long now = System.currentTimeMillis();
        long last = preferences.getLong(KEY_LAST_HEART_ALERT_AT, 0L);
        if (now - last < 60000L) return;
        preferences.edit().putLong(KEY_LAST_HEART_ALERT_AT, now).apply();
        HeartMonitoringService.postThresholdAlert(this, heartRate, low, high, connectedName);
    }

    private boolean isMonitoringActively() {
        return connected && monitoring;
    }

    private String getMonitoringStatusLabel() {
        if (!connected) return monitoring ? "◷ 等待设备重新连接" : "○ 等待设备连接";
        return monitoring ? "● 正在监测" : "○ 已连接，尚未开始";
    }

    private void startMonitoring() {

        // 运行状态必须建立在真实 BLE 连接之上。未连接时只引导连接，不写入运行状态。
        if (!connected) {
            monitoring = false;
            preferences.edit().putBoolean(KEY_MONITOR, false).apply();
            updateStatusViews();
            toast("尚未连接心率设备，请先连接设备，再开始监测");
            startDeviceScan();
            return;
        }

        monitoring = true;
        setXiaomiHeartRateMode(true);

        preferences.edit()
                .putBoolean(
                        KEY_MONITOR,
                        true
                )
                .apply();
        updateMonitoringServiceNotification(true);

        if (preferences.getBoolean(
                KEY_OVERLAY,
                false
        )) {

            startOverlayService();
        }

        if (monitorStatus != null) {
            monitorStatus.setText(getMonitoringStatusLabel());
            monitorStatus.setTextColor(isMonitoringActively() ? Color.rgb(48, 190, 110) : secondaryTextColor);
        }
        HeartControlWidgetProvider.refreshWidgets(this);
    }

    private void stopMonitoring() {

        monitoring = false;
        setXiaomiHeartRateMode(false);

        preferences.edit()
                .putBoolean(
                        KEY_MONITOR,
                        false
                )
                .apply();
        stopMonitoringForegroundService();
        if (!preferences.getBoolean(KEY_OVERLAY, false)
                && preferences.getBoolean(KEY_MAIN_ACTIVITY_VISIBLE, false)
                && hasBluetoothConnectPermission()) {
            handler.postDelayed(() -> {
                if (!shouldUseBackgroundBleService() && bluetoothGatt == null && !isFinishing()) autoConnect();
            }, 650L);
        }

        // 不再强制关闭悬浮窗。
        // 悬浮窗现在与监测开关独立。
        if (!preferences.getBoolean(
                KEY_OVERLAY,
                false
        )) {

            stopOverlayService();
        }

        if (monitorStatus != null) {
            monitorStatus.setText(getMonitoringStatusLabel());
            monitorStatus.setTextColor(isMonitoringActively() ? Color.rgb(48, 190, 110) : secondaryTextColor);
        }
        HeartControlWidgetProvider.refreshWidgets(this);
    }

    // =========================================================
    // 悬浮窗
    // =========================================================

    private void toggleOverlay() {

        if (!Settings.canDrawOverlays(this)) {

            new AppDialogBuilder(false, true)
                    .setTitle("悬浮窗权限")
                    .setMessage("元芳查过了：开启这项权限后，心率悬浮窗才能显示在其他应用上方。你可以随时在系统设置中关闭。")
                    .setPositiveButton(
                            "去开启",
                            (dialog, which) ->
                                    openOverlayPermission()
                    )
                    .setNegativeButton(
                            "取消",
                            null
                    )
                    .show();

            return;
        }

        boolean enabled =
                preferences.getBoolean(
                        KEY_OVERLAY,
                        false
                );

        enabled = !enabled;

        preferences.edit()
                .putBoolean(
                        KEY_OVERLAY,
                        enabled
                )
                .apply();
        // Keep the launcher widget in sync when the user changes the overlay in-app.
        HeartControlWidgetProvider.refreshWidgets(this);

        if (enabled) {
            startOverlayService();
        } else {
            stopOverlayService();
        }

        showPage(
                PAGE_SETTINGS
        );
    }

    private void startOverlayService() {

        if (!Settings.canDrawOverlays(this)) {
            return;
        }

        // The overlay needs fresh BLE heart-rate samples even after MainActivity leaves the screen.
        // Start the foreground service while the Activity is visible; it takes GATT ownership on onStop.
        if (preferences != null && preferences.getBoolean(KEY_OVERLAY, false)) {
            Intent monitorService = new Intent(this, HeartMonitoringService.class)
                    .setAction(HeartMonitoringService.ACTION_START);
            try {
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(monitorService);
                else startService(monitorService);
            } catch (Exception ignored) { }
        }

        Intent intent =
                new Intent(
                        this,
                        HeartOverlayService.class
                );

        intent.putExtra(
                "heart_rate",
                currentHeartRate
        );

        try {

            if (Build.VERSION.SDK_INT >= 26) {

                startForegroundService(
                        intent
                );

            } else {

                startService(intent);
            }

        } catch (Exception e) {

            toast("悬浮窗启动失败，请检查系统权限");
        }
    }

    private void stopOverlayService() {

        try {

            Intent intent =
                    new Intent(
                            this,
                            HeartOverlayService.class
                    );

            stopService(intent);

        } catch (Exception ignored) {
        }
        if (preferences != null && !preferences.getBoolean(KEY_MONITOR, false)) {
            try { stopService(new Intent(this, HeartMonitoringService.class)); } catch (Exception ignored) { }
            if (preferences.getBoolean(KEY_MAIN_ACTIVITY_VISIBLE, false) && hasBluetoothConnectPermission()) {
                handler.postDelayed(() -> {
                    if (!shouldUseBackgroundBleService() && bluetoothGatt == null && !isFinishing()) autoConnect();
                }, 450L);
            }
        }
    }

    private void openOverlayPermission() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                    );

            intent.setData(
                    Uri.parse(
                            "package:" +
                                    getPackageName()
                    )
            );

            startActivity(intent);

        } catch (Exception e) {

            Intent intent =
                    new Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                    );

            startActivity(intent);
        }
    }

    // =========================================================
    // 主题
    // =========================================================

    private String paletteStyleLabel(String key) {
        switch (key) {
            case "Neutral": return "中性";
            case "Vibrant": return "鲜明";
            case "Expressive": return "表现力";
            case "Rainbow": return "彩虹";
            case "FruitSalad": return "果彩";
            case "Monochrome": return "单色";
            case "Fidelity": return "忠于主题色";
            case "Content": return "内容取色";
            default: return "柔和自然";
        }
    }

    private String colorSpecLabel(String key) {
        return "SPEC_2021".equals(key) ? "旧版规范" : "新版规范";
    }

    private void changeTheme(
            String theme
    ) {

        preferences.edit()
                .putString(
                        KEY_THEME,
                        theme
                )
                .apply();

        loadTheme();
        HeartControlWidgetProvider.refreshWidgets(this);

        recreateTo(currentPage == PAGE_THEME ? PAGE_THEME : PAGE_HOME);
    }

    // =========================================================
    // 心率提醒
    // =========================================================

    private void showThresholdDialog() {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                dp(20),
                dp(5),
                dp(20),
                dp(5)
        );

        EditText low =
                new EditText(this);

        low.setHint("低心率阈值，例如 50");
        low.setText(String.valueOf(preferences.getInt("low_threshold", 50)));
        low.setInputType(2);

        EditText high =
                new EditText(this);

        high.setHint("高心率阈值，例如 120");
        high.setText(String.valueOf(preferences.getInt("high_threshold", 120)));
        high.setInputType(2);

        box.addView(low);
        box.addView(high);

        new AppDialogBuilder()
                .setTitle("心率提醒范围")
                .setView(box)
                .setPositiveButton(
                        "保存",
                        (dialog, which) -> {

                            try {

                                int lowValue =
                                        Integer.parseInt(
                                                low.getText()
                                                        .toString()
                                        );

                                int highValue =
                                        Integer.parseInt(
                                                high.getText()
                                                        .toString()
                                        );

                                if (lowValue < 0 || highValue <= lowValue || highValue > 250) {
                                    toast("阈值无效，请确认低值小于高值且不超过 250");
                                    return;
                                }
                                preferences.edit()
                                        .putInt("low_threshold", lowValue)
                                        .putInt("high_threshold", highValue)
                                        .apply();

                            } catch (Exception e) {

                                toast("请输入有效的心率数值");
                            }
                        }
                )
                .setNegativeButton(
                        "取消",
                        null
                )
                .show();
    }

    private void saveKnownDevice(String name, String address) {
        if (preferences == null || address == null || address.trim().isEmpty()) return;
        Set<String> saved = new HashSet<>(preferences.getStringSet(KEY_SAVED_DEVICE_ADDRESSES, new HashSet<>()));
        saved.add(address);
        preferences.edit()
                .putStringSet(KEY_SAVED_DEVICE_ADDRESSES, saved)
                .putString("saved_device_name_" + address, name == null ? "BLE 设备" : name)
                .apply();
    }

    private ArrayList<String> getSavedDeviceAddresses() {
        Set<String> saved = preferences == null ? null : preferences.getStringSet(KEY_SAVED_DEVICE_ADDRESSES, null);
        return saved == null ? new ArrayList<>() : new ArrayList<>(saved);
    }

    private void showSavedDevicesDialog() {
        ArrayList<String> addresses = getSavedDeviceAddresses();
        if (addresses.isEmpty()) {
            toast("还没有保存过设备，先扫描并连接一台手环即可自动保存");
            return;
        }
        String[] labels = new String[addresses.size()];
        for (int i = 0; i < addresses.size(); i++) {
            String address = addresses.get(i);
            String name = preferences.getString("saved_device_name_" + address, "BLE 设备");
            labels[i] = name + "\n" + maskBluetoothAddress(address) + (address.equals(connectedAddress) && connected ? " · 已连接" : "");
        }
        new AppDialogBuilder(true)
                .setTitle("已保存设备 · 点击连接")
                .setItems(labels, (dialog, which) -> {
                    String address = addresses.get(which);
                    try {
                        BluetoothDevice device = bluetoothAdapter == null ? null : bluetoothAdapter.getRemoteDevice(address);
                        if (device != null) connectDevice(device);
                    } catch (Exception error) {
                        toast("无法读取这台设备，请重新扫描并保存");
                    }
                })
                .setNeutralButton("移除记录", (dialog, which) -> showRemoveSavedDeviceDialog())
                .setNegativeButton("关闭", null)
                .show();
    }

    private void showRemoveSavedDeviceDialog() {
        ArrayList<String> addresses = getSavedDeviceAddresses();
        if (addresses.isEmpty()) { toast("没有可移除的保存记录"); return; }
        String[] labels = new String[addresses.size()];
        for (int i = 0; i < addresses.size(); i++) {
            String address = addresses.get(i);
            labels[i] = preferences.getString("saved_device_name_" + address, "BLE 设备") + " · " + maskBluetoothAddress(address);
        }
        new AppDialogBuilder(true).setTitle("移除保存记录")
                .setItems(labels, (dialog, which) -> {
                    String address = addresses.get(which);
                    Set<String> saved = new HashSet<>(preferences.getStringSet(KEY_SAVED_DEVICE_ADDRESSES, new HashSet<>()));
                    saved.remove(address);
                    preferences.edit().putStringSet(KEY_SAVED_DEVICE_ADDRESSES, saved).remove("saved_device_name_" + address).apply();
                    toast("已移除设备保存记录；不会主动断开当前连接");
                })
                .setNegativeButton("取消", null).show();
    }

    private void chooseCsvToImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/csv", "text/comma-separated-values", "application/vnd.ms-excel", "text/plain", "application/octet-stream"});
        try {
            startActivityForResult(intent, REQUEST_IMPORT_CSV);
        } catch (Exception error) {
            toast("系统文件选择器无法打开");
        }
    }

    private void importCsv(Uri uri) {
        toast("正在读取 CSV 并合并历史数据…");
        new Thread(() -> {
            ArrayList<HeartDatabase.Record> imported = new ArrayList<>();
            int invalidRows = 0;
            String resultMessage;
            boolean success = false;
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            parser.setLenient(false);
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                if (input == null) throw new IllegalStateException("文件无法读取");
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    String row = line.trim().replace("\uFEFF", "");
                    if (row.isEmpty() || row.startsWith("时间") || row.startsWith("time") || row.startsWith("Time")) continue;
                    String[] parts = row.split(",", -1);
                    if (parts.length < 2) { invalidRows++; continue; }
                    try {
                        Date date = parser.parse(parts[0].trim());
                        int rate = Integer.parseInt(parts[1].trim());
                        if (date == null || rate < 25 || rate > 240) { invalidRows++; continue; }
                        imported.add(new HeartDatabase.Record(rate, date.getTime()));
                    } catch (Exception badRow) { invalidRows++; }
                }
                int inserted = new HeartDatabase(this).insertRecords(imported);
                resultMessage = "导入完成：新增 " + inserted + " 条" + (invalidRows > 0 ? "，跳过无效行 " + invalidRows + " 条" : "");
                success = true;
            } catch (Exception error) {
                resultMessage = "导入失败：请选择本应用导出的 UTF-8 CSV 文件";
            }
            final String message = resultMessage;
            final boolean importedSuccessfully = success;
            runOnUiThread(() -> {
                toast(message);
                if (importedSuccessfully) showPage(PAGE_DATA);
            });
        }, "HeartCsvImport").start();
    }

    // =========================================================
    // 数据导出
    // =========================================================

    private void exportData() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        intent.putExtra(Intent.EXTRA_TITLE, "心率监测数据_" + stamp + ".csv");
        try {
            startActivityForResult(intent, REQUEST_EXPORT_CSV);
        } catch (Exception error) {
            // Some heavily customized systems omit the save picker; fall back to sharing the CSV content.
            try {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/csv");
                share.putExtra(Intent.EXTRA_TEXT, new HeartDatabase(this).exportCsv());
                startActivity(Intent.createChooser(share, "分享心率 CSV"));
            } catch (Exception ignored) {
                toast("无法打开系统保存或分享面板");
            }
        }
    }

    private void saveCsvToUri(Uri uri) {
        toast("正在导出心率数据…");
        new Thread(() -> {
            String message;
            try (OutputStream output = getContentResolver().openOutputStream(uri, "wt")) {
                if (output == null) throw new IllegalStateException("保存位置不可写");
                output.write(new HeartDatabase(this).exportCsv().getBytes("UTF-8"));
                output.flush();
                message = "心率 CSV 已保存，可以用于备份或在其他设备导入";
            } catch (Exception error) {
                message = "CSV 保存失败，请换一个有写入权限的位置";
            }
            final String result = message;
            runOnUiThread(() -> toast(result));
        }, "HeartCsvExport").start();
    }

    private void confirmClearData() {

        new AppDialogBuilder(false, true)
                .setTitle("清除历史数据")
                .setMessage("元芳提醒：这会删除全部本地心率记录，且无法撤销。确认继续？")
                .setPositiveButton(
                        "确定删除",
                        (dialog, which) -> {

                            HeartDatabase db =
                                    new HeartDatabase(
                                            this
                                    );

                            db.clearAll();

                            toast("历史记录已清除");

                            showPage(
                                    PAGE_DATA
                            );
                        }
                )
                .setNegativeButton(
                        "取消",
                        null
                )
                .show();
    }

    // =========================================================
    // 重连
    // =========================================================

    private void scheduleReconnect() {
        if (userRequestedDisconnect || connectedAddress == null || connectedAddress.isEmpty()) return;
        handler.removeCallbacks(reconnectRunnable);
        int attempt = Math.min(reconnectAttempts++, 4);
        long delay = Math.min(60000L, 3000L * (1L << attempt));
        handler.postDelayed(reconnectRunnable, delay);
    }

    // =========================================================
    // 协议诊断
    // =========================================================

    private String buildServiceDump(
            BluetoothGatt gatt
    ) {

        if (gatt == null) {
            return "尚未连接设备，没有数据。";
        }

        StringBuilder builder =
                new StringBuilder();

        List<BluetoothGattService> services =
                gatt.getServices();

        if (services == null ||
                services.isEmpty()) {

            return "没有发现任何服务（可能还没连接完成，稍后再试）。";
        }

        builder.append(
                "设备：" + connectedName + " (" + connectedAddress + ")\n"
        );
        builder.append("连接诊断：").append(lastBluetoothDiagnostic).append("\n\n");

        int serviceIndex = 1;

        for (BluetoothGattService service : services) {

            builder.append(
                    serviceIndex +
                            ". Service: " +
                            service.getUuid()
                                    .toString() +
                            "\n"
            );

            serviceIndex++;

            List<BluetoothGattCharacteristic>
                    characteristics =
                    service.getCharacteristics();

            if (characteristics == null ||
                    characteristics.isEmpty()) {

                builder.append(
                        "   （无特征值）\n"
                );

                continue;
            }

            for (BluetoothGattCharacteristic
                    characteristic : characteristics) {

                builder.append(
                        "   - Characteristic: " +
                                characteristic.getUuid()
                                        .toString() +
                                "  properties=" +
                                describeProperties(
                                        characteristic
                                                .getProperties()
                                ) +
                                "\n"
                );
            }

            builder.append("\n");
        }

        builder.append("最近收到的数据包（十六进制）\n");
        builder.append("总收包数：").append(receivedBlePacketCount).append("\n");
        synchronized (latestBlePackets) {
            if (latestBlePackets.isEmpty()) {
                builder.append("还没有收到数据包；有些手环要先完成配对或认证才会推送数据。\n");
            } else {
                for (Map.Entry<String, String> packet : latestBlePackets.entrySet()) {
                    builder.append("   ").append(packet.getKey()).append(" : ")
                            .append(packet.getValue()).append("\n");
                }
            }
        }
        return builder.toString();
    }

    private String describeProperties(
            int properties
    ) {

        StringBuilder builder =
                new StringBuilder();

        if ((properties &
                BluetoothGattCharacteristic
                        .PROPERTY_READ) != 0) {

            builder.append("READ,");
        }

        if ((properties &
                BluetoothGattCharacteristic
                        .PROPERTY_WRITE) != 0) {

            builder.append("WRITE,");
        }

        if ((properties &
                BluetoothGattCharacteristic
                        .PROPERTY_WRITE_NO_RESPONSE) != 0) {

            builder.append("WRITE_NO_RESPONSE,");
        }

        if ((properties &
                BluetoothGattCharacteristic
                        .PROPERTY_NOTIFY) != 0) {

            builder.append("NOTIFY,");
        }

        if ((properties &
                BluetoothGattCharacteristic
                        .PROPERTY_INDICATE) != 0) {

            builder.append("INDICATE,");
        }

        if (builder.length() == 0) {

            return "-";
        }

        return builder.substring(
                0,
                builder.length() - 1
        );
    }

    private String maskBluetoothAddress(String address) {
        if (address == null || address.trim().isEmpty()) return "未连接";
        String[] parts = address.split(":");
        if (parts.length < 6) return "已连接（地址已隐藏）";
        return parts[0] + ":" + parts[1] + ":**:**:**:" + parts[5];
    }

    private void shareDiagnosticReport() {
        StringBuilder report = new StringBuilder();
        report.append("心率监测诊断报告\n");
        report.append("生成时间：").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n");
        report.append("系统：Android ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        report.append("手机：").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        try {
            android.content.pm.PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            report.append("App 版本：").append(pi.versionName == null ? "未知" : pi.versionName).append("\n");
        } catch (Exception ignored) { report.append("App 版本：未知\n"); }
        report.append("BLE 连接：").append(connected ? "已连接" : "未连接").append("\n");
        report.append("设备：").append(connectedName).append("\n");
        report.append("设备地址（默认隐藏部分）：").append(maskBluetoothAddress(connectedAddress)).append("\n");
        report.append("监测状态：").append(getMonitoringStatusLabel()).append("\n");
        report.append("当前心率：").append(currentHeartRate > 0 ? currentHeartRate + " BPM" : "暂无").append("\n");
        report.append("电量：").append(currentBattery >= 0 ? currentBattery + "%" : "未读取").append("\n");
        report.append("步数：").append(currentSteps >= 0 ? String.valueOf(currentSteps) : "未读取").append("\n");
        report.append("蓝牙信号：").append(currentRssi != 0 ? currentRssi + " dBm" : "未读取").append("\n");
        report.append("接收数据包：").append(receivedBlePacketCount).append("\n");
        report.append("今天记录：").append(getTodayCount()).append(" 条\n");
        report.append("连接诊断：").append(lastBluetoothDiagnostic).append("\n\n");
        String dump = serviceDumpText;
        if (connected && bluetoothGatt != null) {
            try { dump = buildServiceDump(bluetoothGatt); } catch (Exception ignored) { }
        }
        if (dump == null || dump.trim().isEmpty()) dump = "暂无 GATT 服务详情";
        if (connectedAddress != null && !connectedAddress.isEmpty()) dump = dump.replace(connectedAddress, maskBluetoothAddress(connectedAddress));
        report.append("服务与原始数据诊断\n").append(dump);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "心率监测诊断报告");
        send.putExtra(Intent.EXTRA_TEXT, report.toString());
        startActivity(Intent.createChooser(send, "分享诊断报告"));
    }

    private void showServiceDumpDialog() {

        String content;
        if (connected && bluetoothGatt != null) {
            try { serviceDumpText = buildServiceDump(bluetoothGatt); } catch (Exception ignored) { }
        }
        content = serviceDumpText.isEmpty()
                ? "还没有数据。\n当前状态：" + lastBluetoothDiagnostic + "\n请先在‘设备’页连接手环，连接成功后回到这里再点一次。"
                : serviceDumpText;

        ScrollView scroll =
                new ScrollView(this);

        TextView text =
                new TextView(this);

        text.setText(
                content
        );

        text.setTextIsSelectable(
                true
        );

        text.setPadding(
                dp(20),
                dp(10),
                dp(20),
                dp(10)
        );

        text.setTextSize(
                12
        );

        scroll.addView(
                text
        );

        new AppDialogBuilder()
                .setTitle(
                        "蓝牙服务/特征值列表"
                )
                .setView(
                        scroll
                )
                .setPositiveButton(
                        "复制",
                        (dialog, which) -> {

                            ClipboardManager clipboard =
                                    (ClipboardManager)
                                            getSystemService(
                                                    CLIPBOARD_SERVICE
                                            );

                            if (clipboard != null) {

                                clipboard.setPrimaryClip(
                                        ClipData.newPlainText(
                                                "service_dump",
                                                content
                                        )
                                );

                                toast(
                                        "已复制，去发给我吧"
                                );
                            }
                        }
                )
                .setNegativeButton(
                        "关闭",
                        null
                )
                .show();
    }

    // =========================================================
    // 页面刷新
    // =========================================================

    private String getHeartRateCapabilityLabel() {
        if (!connected) return "未连接";
        if (!servicesDiscovered || bluetoothGatt == null) return "检测中";
        BluetoothGattService service = bluetoothGatt.getService(HEART_RATE_SERVICE);
        if (service != null && service.getCharacteristic(HEART_RATE_MEASUREMENT) != null) return "标准实时心率（180D/2A37）";
        if (bluetoothGatt.getService(MI_FEE0_SERVICE) != null || bluetoothGatt.getService(MI_FEE1_SERVICE) != null) return "发现小米扩展服务；心率可能需私有协议";
        return "未发现标准心率通道";
    }

    private String getBatteryCapabilityLabel() {
        if (!connected) return "未连接";
        if (currentBattery >= 0) return "已读取：" + currentBattery + "%";
        if (!servicesDiscovered || bluetoothGatt == null) return "检测中";
        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                UUID uuid = characteristic.getUuid();
                if (BATTERY_LEVEL.equals(uuid) || MI_BATTERY_CHARACTERISTIC.equals(uuid) || MI_BAND2_BATTERY_CHARACTERISTIC.equals(uuid)) return "支持读取，等待设备返回";
            }
        }
        return "未发现可读电量特征";
    }

    private void refreshDevicePage() {

        if (deviceStatus != null) {

            deviceStatus.setText(
                    connected
                            ? "已连接"
                            : "未连接"
            );
        }

        if (deviceName != null) {

            deviceName.setText(
                    connectedName
            );
        }

        if (deviceAddress != null) {

            deviceAddress.setText(
                    connectedAddress.isEmpty()
                            ? "--"
                            : connectedAddress
            );
        }

        if (deviceRssi != null) {

            deviceRssi.setText(
                    currentRssi != 0
                            ? currentRssi + " dBm"
                            : "--"
            );
        }

        if (deviceBattery != null) {

            deviceBattery.setText(
                    currentBattery >= 0
                            ? currentBattery + "%"
                            : "不可用"
            );
        }
        if (deviceSteps != null) deviceSteps.setText(currentSteps >= 0 ? String.valueOf(currentSteps) : "未读取到");
        if (devicePacketCount != null) devicePacketCount.setText(receivedBlePacketCount + " 包");

        if (deviceManufacturer != null) {

            deviceManufacturer.setText(
                    manufacturer
            );
        }

        if (deviceModel != null) {

            deviceModel.setText(
                    model
            );
        }

        if (deviceFirmware != null) {

            deviceFirmware.setText(
                    firmware
            );
        }

        if (deviceCapabilities != null) {
            deviceCapabilities.setText(getHeartRateCapabilityLabel());
        }
        if (deviceBatteryCapability != null) {
            deviceBatteryCapability.setText(getBatteryCapabilityLabel());
        }
    }

    private void updateStatusViews() {

        if (homeStatus != null) {

            homeStatus.setText(
                    connected
                            ? "● 已连接"
                            : "○ 未连接"
            );

            homeStatus.setTextColor(
                    connected
                            ? Color.rgb(
                                    48,
                                    190,
                                    110
                            )
                            : secondaryTextColor
            );
        }

        if (homeDevice != null) {

            homeDevice.setText(
                    connectedName
            );
        }

        if (monitorStatus != null) {
            monitorStatus.setText(getMonitoringStatusLabel());
            monitorStatus.setTextColor(isMonitoringActively() ? Color.rgb(48, 190, 110) : secondaryTextColor);
        }

        if (homeBattery != null) {

            homeBattery.setText(
                    currentBattery >= 0
                            ? currentBattery + "%"
                            : "不可用"
            );
        }

        if (homeRssi != null) {

            homeRssi.setText(
                    currentRssi != 0
                            ? currentRssi + " dBm"
                            : "--"
            );
        }

        refreshDevicePage();
        HeartControlWidgetProvider.refreshWidgets(this);
    }

    private void updateHomeStats() {

        if (homeMin == null) {
            return;
        }

        HeartDatabase db =
                new HeartDatabase(this);

        int min =
                db.getTodayMin();

        int avg =
                db.getTodayAverage();

        int max =
                db.getTodayMax();

        homeMin.setText(
                min > 0
                        ? String.valueOf(min)
                        : "--"
        );

        homeAvg.setText(
                avg > 0
                        ? String.valueOf(avg)
                        : "--"
        );

        homeMax.setText(
                max > 0
                        ? String.valueOf(max)
                        : "--"
        );

        if (homeCount != null) {

            homeCount.setText(
                    String.valueOf(
                            db.getTodayCount()
                    )
            );
        }

        if (homeLast != null) {

            homeLast.setText(
                    getLastTime()
            );
        }
    }

    // =========================================================
    // UI 工具
    // =========================================================

    // =========================================================
    // 主题设置页（按 KernelSU 管理器的「主题设置」页组织）
    // =========================================================

    private boolean isThemeCustom() {
        if (preferences == null) return false;
        if (preferences.contains(KEY_THEME_CUSTOM)) return preferences.getBoolean(KEY_THEME_CUSTOM, false);
        // 兼容旧版本：以前选过主题色或开过系统动态颜色，就视为已启用自定义
        return preferences.contains(KEY_ACCENT_COLOR) || preferences.getBoolean(KEY_DYNAMIC_COLOR, false);
    }

    private boolean supportsSpec2025(String style) {
        return "TonalSpot".equals(style) || "Neutral".equals(style)
                || "Vibrant".equals(style) || "Expressive".equals(style);
    }

    /** 当前主题色在列表中的位置：0 = 默认（系统取色 / 默认蓝），-1 = 不在列表里。 */
    private int currentKeyColorIndex() {
        if (preferences.getBoolean(KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= 31) && Build.VERSION.SDK_INT >= 31) return 0;
        int c = preferences.getInt(KEY_ACCENT_COLOR, DEFAULT_ACCENT);
        for (int i = 1; i < KEY_COLOR_VALUES.length; i++) {
            if (KEY_COLOR_VALUES[i] == c) return i;
        }
        return Build.VERSION.SDK_INT >= 31 ? -1 : 0;
    }

    private String keyColorName(int index) {
        if (index == 0) return Build.VERSION.SDK_INT >= 31 ? "默认（系统取色）" : "默认（蓝色）";
        if (index < 0) return "默认蓝色";
        return KEY_COLOR_NAMES[index];
    }

    private int keyColorValue(int index) {
        if (index > 0) return KEY_COLOR_VALUES[index];
        return accentColor;
    }

    private String themeModeLabel(String key) {
        if ("light".equals(key)) return "浅色";
        if ("dark".equals(key)) return "深色";
        if ("amoled".equals(key)) return "纯黑";
        return "跟随系统";
    }

    private String themeSummary() {
        String mode = themeModeLabel(preferences.getString(KEY_THEME, "system"));
        if (preferences.getBoolean(KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= 31) && Build.VERSION.SDK_INT >= 31) {
            return mode + " · 动态取色";
        }
        if (!isThemeCustom()) return mode;
        return mode + " · " + keyColorName(currentKeyColorIndex());
    }

    private int blendColors(int top, int bottom, float ratio) {
        float r = Math.max(0f, Math.min(1f, ratio));
        return Color.rgb(
                Math.round(Color.red(top) * r + Color.red(bottom) * (1f - r)),
                Math.round(Color.green(top) * r + Color.green(bottom) * (1f - r)),
                Math.round(Color.blue(top) * r + Color.blue(bottom) * (1f - r)));
    }

    private View roundedView(int color, int radiusDp) {
        View v = new View(this);
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        v.setBackground(d);
        return v;
    }

    /** KSU 风格分组卡片；液态玻璃开关会统一影响所有设置分组。 */
    private LinearLayout createKsuCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        setGlassBackground(card, cardColor, 18);
        card.setClipToOutline(true);
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(0));
        return card;
    }

    private LinearLayout createPrefRow(String title, String summary, View end) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(11), dp(18), dp(11));
        row.setMinimumHeight(dp(58));
        row.setClipChildren(true);
        row.setClipToPadding(true);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setMinimumWidth(0);
        TextView titleView = createText(title, 15, Typeface.NORMAL);
        titleView.setMaxLines(2);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        titleView.setIncludeFontPadding(false);
        texts.addView(titleView);
        if (summary != null && summary.length() > 0) {
            TextView sub = createText(summary, 11, Typeface.NORMAL);
            sub.setTextColor(secondaryTextColor);
            sub.setLineSpacing(dp(1), 1f);
            sub.setMaxLines(2);
            sub.setEllipsize(android.text.TextUtils.TruncateAt.END);
            sub.setIncludeFontPadding(false);
            texts.addView(sub, marginParams(-1, -2, 0, 3, 0, 0));
        }
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));
        if (end != null) {
            LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-2, -2);
            ep.leftMargin = dp(12);
            row.addView(end, ep);
        }
        return row;
    }

    private void applyPressFeedback(View view, float pressScale) {
        if (view == null) return;
        view.setOnTouchListener((target, event) -> {
            boolean scaleEnabled = preferences == null || preferences.getBoolean(KEY_TOUCH_FEEDBACK, true);
            boolean hapticEnabled = preferences != null && preferences.getBoolean(KEY_HAPTIC_FEEDBACK, true);
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (scaleEnabled) {
                    target.animate().cancel();
                    target.animate().scaleX(pressScale).scaleY(pressScale).setDuration(85L).start();
                }
                if (hapticEnabled) target.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                target.animate().cancel();
                target.animate().scaleX(1f).scaleY(1f).setDuration(scaleEnabled ? 135L : 0L)
                        .setInterpolator(new DecelerateInterpolator(1.5f)).start();
            }
            return false;
        });
    }

    private LinearLayout createArrowRow(String title, String summary, CharSequence value, View.OnClickListener click) {
        TextView end = createText("", 14, Typeface.NORMAL);
        SpannableStringBuilder text = new SpannableStringBuilder(value == null ? "" : value);
        text.append(text.length() == 0 ? "›" : "  ›");
        end.setText(text);
        end.setTextColor(secondaryTextColor);
        LinearLayout row = createPrefRow(title, summary, end);
        row.setOnClickListener(click);
        applyPressFeedback(row, 0.988f);
        try {
            android.util.TypedValue tv = new android.util.TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true)) {
                row.setForeground(getDrawable(tv.resourceId));
            }
        } catch (Exception ignored) { }
        return row;
    }

    private LinearLayout createSwitchRow(String title, String summary, boolean checked,
                                         CompoundButton.OnCheckedChangeListener listener) {
        final Switch sw = new Switch(this);
        sw.setChecked(checked);
        sw.setPadding(0, 0, 0, 0);
        sw.setMinWidth(dp(46));
        sw.setMaxWidth(dp(58));
        sw.setGravity(Gravity.CENTER);
        sw.setShowText(false);
        int[][] states = {{android.R.attr.state_checked}, {}};
        sw.setThumbTintList(new ColorStateList(states, new int[]{Color.WHITE, Color.rgb(205, 205, 212)}));
        sw.setTrackTintList(new ColorStateList(states, new int[]{accentColor, dividerColor}));
        sw.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && preferences != null
                    && preferences.getBoolean(KEY_HAPTIC_FEEDBACK, true)) {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            }
            return false;
        });
        sw.setOnCheckedChangeListener(listener);
        LinearLayout row = createPrefRow(title, summary, sw);
        row.setOnClickListener(v -> sw.toggle());
        applyPressFeedback(row, 0.988f);
        return row;
    }

    private void addPrefRow(LinearLayout card, View row) {
        if (card.getChildCount() > 0) {
            View line = new View(this);
            line.setBackgroundColor(dividerColor);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 1);
            lp.leftMargin = dp(18);
            lp.rightMargin = dp(18);
            card.addView(line, lp);
        }
        card.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void buildThemePage() {
        // 顶栏：返回 + 标题
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = createText("‹", 34, Typeface.NORMAL);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v -> showPage(PAGE_SETTINGS));
        applyPressFeedback(back, 0.94f);
        bar.addView(back, new LinearLayout.LayoutParams(dp(44), dp(44)));
        bar.addView(createText("主题", 24, Typeface.BOLD), new LinearLayout.LayoutParams(0, dp(44), 1));
        contentContainer.addView(bar, marginParams(-1, dp(48), 0, 0, 0, 4));

        // 实时预览
        View preview = buildThemePreview();
        LinearLayout.LayoutParams pp = (LinearLayout.LayoutParams) preview.getLayoutParams();
        pp.gravity = Gravity.CENTER_HORIZONTAL;
        pp.topMargin = dp(10);
        pp.bottomMargin = dp(18);
        contentContainer.addView(preview, pp);

        // 明暗模式
        contentContainer.addView(buildThemeModeTabs(), marginParams(-1, dp(48), 0, 0, 0, 12));

        // 动态取色：Android 12 及以上读取系统 Monet 主色；低版本可继续使用手动颜色。
        LinearLayout dynamicCard = createKsuCard();
        addPrefRow(dynamicCard, createSwitchRow("动态取色", Build.VERSION.SDK_INT >= 31
                        ? "跟随系统壁纸的动态配色，并应用到强调色和选中状态"
                        : "此功能需要 Android 12 或更高版本", 
                preferences.getBoolean(KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= 31),
                (button, enabled) -> {
                    if (Build.VERSION.SDK_INT < 31 && enabled) {
                        preferences.edit().putBoolean(KEY_DYNAMIC_COLOR, false).apply();
                        toast("动态取色需要 Android 12 或更高版本");
                        recreateTo(PAGE_THEME);
                        return;
                    }
                    preferences.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply();
                    recreateTo(PAGE_THEME);
                }));
        contentContainer.addView(dynamicCard, marginParams(-1, -2, 0, 0, 0, 12));

        // 主题色
        final boolean custom = isThemeCustom();
        LinearLayout colorCard = createKsuCard();
        addPrefRow(colorCard, createSwitchRow("自定义主题色", "开启后可手动选择主题色与配色风格；动态取色可单独控制", custom,
                (b, on) -> {
                    preferences.edit().putBoolean(KEY_THEME_CUSTOM, on).apply();
                    recreateTo(PAGE_THEME);
                }));
        if (custom) {
            final int idx = currentKeyColorIndex();
            SpannableString colorValue = new SpannableString("● " + keyColorName(idx));
            colorValue.setSpan(new ForegroundColorSpan(keyColorValue(idx)), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            addPrefRow(colorCard, createArrowRow("主题色", null, colorValue, v -> showKeyColorDialog()));
            if (idx != 0) {
                String style = preferences.getString(KEY_PALETTE_STYLE, "TonalSpot");
                String spec = preferences.getString(KEY_COLOR_SPEC, "SPEC_2025");
                addPrefRow(colorCard, createArrowRow("配色风格", null, paletteStyleLabel(style), v -> showPaletteStyleDialog()));
                String specSummary = ("SPEC_2025".equals(spec) && !supportsSpec2025(style))
                        ? "当前配色风格不支持新版规范，已自动使用旧版" : null;
                addPrefRow(colorCard, createArrowRow("色彩规范", specSummary, colorSpecLabel(spec), v -> showColorSpecDialog()));
            }
        }
        contentContainer.addView(colorCard, marginParams(-1, -2, 0, 0, 0, 12));

        // 背景来源：优先显示系统当前桌面壁纸；关闭后恢复已导入图片或系统主题底色。
        LinearLayout backgroundCard = createKsuCard();
        boolean useSystemWallpaper = isSystemWallpaperEnabled();
        String savedBackground = preferences.getString(KEY_CUSTOM_BACKGROUND_URI, null);
        boolean hasSavedBackground = savedBackground != null && !savedBackground.trim().isEmpty();
        String wallpaperHint = Build.VERSION.SDK_INT >= 33
                ? "自动读取系统当前桌面壁纸；首次开启需在系统设置中单独允许‘所有文件访问’，不再手动选图"
                : "自动读取系统当前桌面壁纸；首次开启需要照片存储读取权限，壁纸变化后重新进入应用自动同步";
        addPrefRow(backgroundCard, createSwitchRow("使用桌面壁纸",
                wallpaperHint,
                useSystemWallpaper,
                (button, enabled) -> {
                    if (enabled) {
                        beginDesktopWallpaperRead();
                    } else {
                        preferences.edit().putBoolean(KEY_USE_SYSTEM_WALLPAPER, false).apply();
                        recreateTo(PAGE_THEME);
                    }
                }));
        if (useSystemWallpaper) {
            addPrefRow(backgroundCard, createGlassSliderRow("全局面板不透明度",
                    "统一调节内容卡片、图表和弹窗的底色；底部页面选择栏保持独立样式",
                    KEY_WALLPAPER_PANEL_OPACITY, 0, 100, 48, "%"));
            addPrefRow(backgroundCard, createArrowRow("重新读取桌面壁纸", "重新从系统读取当前壁纸，无需手动选择图片", "刷新", v -> beginDesktopWallpaperRead()));
        }
        addPrefRow(backgroundCard, createArrowRow("自定义背景图片",
                useSystemWallpaper ? "自定义图片会保留，关闭壁纸背景后自动恢复" : "从相册或文件中导入图片，作为应用页面背景",
                !hasSavedBackground ? "选择图片" : (useSystemWallpaper ? "已保存" : "已设置 · 点击更换"),
                v -> launchBackgroundPicker()));
        addPrefRow(backgroundCard, createArrowRow("清除自定义背景",
                "删除导入图片；桌面壁纸开关不受影响", "清除", v -> {
                    preferences.edit().remove(KEY_CUSTOM_BACKGROUND_URI).apply();
                    recreateTo(PAGE_THEME);
                }));
        contentContainer.addView(backgroundCard, marginParams(-1, -2, 0, 0, 0, 12));

        // 桌面小组件外观独立于应用主题：可以跟随应用，也可以固定为黑色或白色模式。
        LinearLayout widgetStyleCard = createKsuCard();
        String widgetMode = preferences.getString(
                HeartControlWidgetProvider.KEY_WIDGET_COLOR_MODE, "follow");
        addPrefRow(widgetStyleCard, createArrowRow("小组件外观",
                "单独选择桌面心率小组件的黑色或白色样式，不改变应用本身主题",
                widgetAppearanceLabel(widgetMode), v -> showWidgetAppearanceDialog()));
        contentContainer.addView(widgetStyleCard, marginParams(-1, -2, 0, 0, 0, 12));

        // 两项独立控制：页面主题与底部页面切换栏分别渲染，不能共用同一个开关。
        LinearLayout glassCard = createKsuCard();
        addPrefRow(glassCard, createSwitchRow("主题液态玻璃", "控制应用页面、卡片和图表的玻璃效果，不控制底部切换栏", isLiquidGlassEnabled(),
                (button, enabled) -> {
                    preferences.edit().putBoolean(KEY_LIQUID_GLASS, enabled).apply();
                    refreshGlobalGlassAppearance();
                }));
        addPrefRow(glassCard, createSwitchRow("底部切换栏液态玻璃", "独立控制下方页面切换栏的液态玻璃，不跟随主题开关", isBottomNavigationGlassEnabled(),
                (button, enabled) -> {
                    preferences.edit().putBoolean(KEY_LIQUID_GLASS_NAV_BAR, enabled).apply();
                    applyBottomNavigationGlassSetting();
                }));
        addPrefRow(glassCard, createArrowRow("玻璃色调", "全局玻璃统一使用同一色调", glassTintLabel(preferences.getString(KEY_GLASS_TINT, "clear")),
                v -> showGlassTintDialog()));
        addPrefRow(glassCard, createGlassSliderRow("透光程度", "调节背景透出量", KEY_GLASS_OPACITY, 35, 95, 62, "%"));
        addPrefRow(glassCard, createGlassSliderRow("材质层次", "调节玻璃上下色层差异，不使用页面截图模糊", KEY_GLASS_REFRACTION, 0, 100, 45, "%"));
        addPrefRow(glassCard, createGlassSliderRow("边缘反光", "调节上下边缘的亮暗层次", KEY_GLASS_HIGHLIGHT, 0, 100, 58, "%"));
        addPrefRow(glassCard, createGlassSliderRow("圆角幅度", "统一调整卡片与导航圆角", KEY_GLASS_RADIUS, 65, 145, 100, "%"));
        contentContainer.addView(glassCard, marginParams(-1, -2, 0, 0, 0, 12));


        // 底栏样式
        LinearLayout barCard = createKsuCard();
        addPrefRow(barCard, createSwitchRow("悬浮底栏", "启用悬浮玻璃导航；长按导航并横向拖动可切换页面，按压时会自然放大",
                preferences.getBoolean(KEY_FLOATING_BAR, true),
                (b, on) -> {
                    preferences.edit().putBoolean(KEY_FLOATING_BAR, on).apply();
                    recreateTo(PAGE_THEME);
                }));
        contentContainer.addView(barCard, marginParams(-1, -2, 0, 0, 0, 12));

        // 交互与启动效果：所有选项持久保存，默认保留原有动画和触觉反馈。
        LinearLayout interactionCard = createKsuCard();
        addPrefRow(interactionCard, createSwitchRow("应用打开与切页动画",
                "控制页面切换动画；首次打开时卡片会按所选方向弹入",
                preferences.getBoolean(KEY_STARTUP_ANIMATION, true),
                (b, on) -> preferences.edit().putBoolean(KEY_STARTUP_ANIMATION, on).apply()));
        addPrefRow(interactionCard, createArrowRow("卡片弹入方向", "下次启动时生效，也可在选择后预览",
                startupDirectionLabel(preferences.getString(KEY_STARTUP_DIRECTION, "up")), v -> showStartupDirectionDialog()));
        addPrefRow(interactionCard, createSwitchRow("按键触摸反馈",
                "只控制按下时的轻微缩放动画",
                preferences.getBoolean(KEY_TOUCH_FEEDBACK, true),
                (b, on) -> preferences.edit().putBoolean(KEY_TOUCH_FEEDBACK, on).apply()));
        addPrefRow(interactionCard, createSwitchRow("按键触觉反馈 / 振动",
                "独立控制点击时的系统轻振动，不影响触摸缩放",
                preferences.getBoolean(KEY_HAPTIC_FEEDBACK, true),
                (b, on) -> preferences.edit().putBoolean(KEY_HAPTIC_FEEDBACK, on).apply()));
        addPrefRow(interactionCard, createSwitchRow("显示开发者功能",
                "默认隐藏调试、模拟数据等开发工具；开启后在设置页显示，原有功能不会删除",
                preferences.getBoolean(KEY_SHOW_DEVELOPER, false),
                (b, on) -> {
                    preferences.edit().putBoolean(KEY_SHOW_DEVELOPER, on).apply();
                    if (!on) {
                        preferences.edit().putBoolean(KEY_DEVELOPER, false).apply();
                        stopDevSim();
                    }
                }));
        contentContainer.addView(interactionCard, marginParams(-1, -2, 0, 0, 0, 12));

        // 页面缩放
        LinearLayout scaleCard = createKsuCard();
        final TextView percent = createText(Math.round(pageScale * 100) + "%", 14, Typeface.NORMAL);
        percent.setTextColor(secondaryTextColor);
        scaleCard.addView(createPrefRow("页面缩放", "调整界面整体大小", percent), new LinearLayout.LayoutParams(-1, -2));
        SeekBar scaleBar = new SeekBar(this);
        scaleBar.setMax(30); // 80% – 110%
        scaleBar.setProgress(Math.round(pageScale * 100) - 80);
        scaleBar.setProgressTintList(ColorStateList.valueOf(accentColor));
        scaleBar.setThumbTintList(ColorStateList.valueOf(accentColor));
        scaleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                percent.setText((80 + progress) + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                int snapped = Math.round((80 + seekBar.getProgress()) / 5f) * 5;
                float value = Math.max(80, Math.min(110, snapped)) / 100f;
                if (Math.abs(value - pageScale) < 0.001f) return;
                preferences.edit().putFloat(KEY_PAGE_SCALE, value).apply();
                recreateTo(PAGE_THEME);
            }
        });
        scaleCard.addView(scaleBar, marginParams(-1, dp(36), 14, 0, 14, 14));
        contentContainer.addView(scaleCard, marginParams(-1, -2, 0, 0, 0, 12));
    }

    /** 手机外框预览：颜色取自当前真实配色，改设置后自动刷新。 */
    private View buildThemePreview() {
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        int w = (int) (dm.widthPixels * 0.40f);
        int h = Math.min(dp(300), w * dm.heightPixels / dm.widthPixels);
        boolean floating = preferences.getBoolean(KEY_FLOATING_BAR, true);
        int tone = darkMode ? Color.rgb(39, 39, 42) : Color.rgb(235, 235, 242);
        int accentTint = blendColors(accentColor, tone, 0.30f);

        FrameLayout frame = new FrameLayout(this);
        setGlassBackground(frame, backgroundColor, 18);
        frame.setClipToOutline(true);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        TextView title = createText("心率监测", 9, Typeface.BOLD);
        title.setPadding(dp(10), dp(14), 0, dp(6));
        col.addView(title, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, dp(36));
        ap.setMargins(dp(8), 0, dp(8), dp(6));
        col.addView(roundedView(accentTint, 7), ap);

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, 0, 1f);
        bp.setMargins(dp(8), 0, dp(8), dp(6));
        col.addView(roundedView(tone, 7), bp);
        for (int i = 0; i < 2; i++) {
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(12));
            sp.setMargins(dp(8), 0, dp(8), dp(6));
            col.addView(roundedView(tone, 5), sp);
        }
        View spacer = new View(this);
        col.addView(spacer, new LinearLayout.LayoutParams(-1, dp(floating ? 40 : 36)));
        frame.addView(col, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        for (int i = 0; i < 4; i++) {
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(11), dp(11));
            ip.setMargins(dp(floating ? 5 : 9), 0, dp(floating ? 5 : 9), 0);
            nav.addView(roundedView(i == 0 ? accentColor : secondaryTextColor, 3), ip);
        }
        if (floating) {
            nav.setBackground(createBottomNavigationPillDrawable(cardColor, 13));
            nav.setPadding(dp(8), 0, dp(8), 0);
            FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(-2, dp(26), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            np.bottomMargin = dp(8);
            frame.addView(nav, np);
        } else {
            glassSurfaceSpecs.remove(nav);
            nav.setBackground(createBottomNavigationPillDrawable(cardColor, 0));
            frame.addView(nav, new FrameLayout.LayoutParams(-1, dp(32), Gravity.BOTTOM));
        }

        frame.setLayoutParams(new LinearLayout.LayoutParams(w, h));
        return frame;
    }

    private View buildThemeModeTabs() {
        int tone = darkMode ? Color.rgb(39, 39, 42) : Color.rgb(235, 235, 242);
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4));
        setGlassBackground(tabs, tone, 16);

        final String current = preferences.getString(KEY_THEME, "system");
        String[] keys = {"system", "light", "dark", "amoled"};
        String[] labels = {"跟随系统", "浅色", "深色", "纯黑"};
        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            boolean selected = key.equals(current);
            TextView tab = createText(labels[i], 13, selected ? Typeface.BOLD : Typeface.NORMAL);
            tab.setGravity(Gravity.CENTER);
            tab.setTextColor(selected ? accentColor : secondaryTextColor);
            if (selected) {
                setGlassBackground(tab, blendColors(cardColor, accentColor, 0.88f), 12);
            }
            tab.setOnClickListener(v -> {
                if (!key.equals(current)) changeTheme(key);
            });
            tabs.addView(tab, new LinearLayout.LayoutParams(0, -1, 1f));
        }
        return tabs;
    }

    private String widgetAppearanceLabel(String mode) {
        if ("black".equals(mode)) return "黑色模式";
        if ("white".equals(mode)) return "白色模式";
        return "跟随应用主题";
    }

    private void showWidgetAppearanceDialog() {
        final String[] keys = {"follow", "black", "white"};
        final String[] labels = {"跟随应用主题", "黑色模式", "白色模式"};
        String current = preferences.getString(HeartControlWidgetProvider.KEY_WIDGET_COLOR_MODE, "follow");
        int checked = 0;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(current)) checked = i;
        }
        new AppDialogBuilder(true).setTitle("小组件外观")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    preferences.edit()
                            .putString(HeartControlWidgetProvider.KEY_WIDGET_COLOR_MODE, keys[which])
                            .remove("widget_monochrome_mode")
                            .apply();
                    dialog.dismiss();
                    updateSettingRowValue("小组件外观", labels[which]);
                    HeartControlWidgetProvider.refreshWidgets(MainActivity.this);
                    toast("小组件已切换为" + labels[which]);
                }).setNegativeButton("取消", null).show();
    }

    private void showStartupDirectionDialog() {
        final String[] keys = {"up", "down", "left", "right"};
        final String[] labels = {"从上方弹入", "从下方弹入", "从左侧弹入", "从右侧弹入"};
        String selected = preferences.getString(KEY_STARTUP_DIRECTION, "up");
        int checked = 0;
        for (int i = 0; i < keys.length; i++) if (keys[i].equals(selected)) checked = i;
        new AppDialogBuilder(true).setTitle("卡片弹入方向")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    preferences.edit().putString(KEY_STARTUP_DIRECTION, keys[which]).apply();
                    dialog.dismiss();
                    updateSettingRowValue("卡片弹入方向", startupDirectionLabel(keys[which]));
                    // 原位演示选中方向，不重新创建页面，避免设置项触发闪屏。
                    animateContentEntrance(keys[which]);
                }).setNegativeButton("取消", null).show();
    }

    private void showKeyColorDialog() {
        CharSequence[] items = new CharSequence[KEY_COLOR_NAMES.length];
        for (int i = 0; i < items.length; i++) {
            SpannableString item = new SpannableString("● " + keyColorName(i));
            if (i > 0) item.setSpan(new ForegroundColorSpan(KEY_COLOR_VALUES[i]), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            items[i] = item;
        }
        new AppDialogBuilder(true).setTitle("主题色")
                .setSingleChoiceItems(items, Math.max(0, currentKeyColorIndex()), (dialog, which) -> {
                    SharedPreferences.Editor e = preferences.edit().putBoolean(KEY_THEME_CUSTOM, true);
                    if (which == 0) {
                        if (Build.VERSION.SDK_INT >= 31) {
                            e.putBoolean(KEY_DYNAMIC_COLOR, true);
                        } else {
                            e.putBoolean(KEY_DYNAMIC_COLOR, false).putInt(KEY_ACCENT_COLOR, DEFAULT_ACCENT);
                        }
                    } else {
                        e.putBoolean(KEY_DYNAMIC_COLOR, false).putInt(KEY_ACCENT_COLOR, KEY_COLOR_VALUES[which]);
                    }
                    e.apply();
                    dialog.dismiss();
                    recreateTo(PAGE_THEME);
                }).setNegativeButton("取消", null).show();
    }

    private void showPaletteStyleDialog() {
        String[] labels = new String[STYLE_KEYS.length];
        int checked = 0;
        String selected = preferences.getString(KEY_PALETTE_STYLE, "TonalSpot");
        for (int i = 0; i < STYLE_KEYS.length; i++) {
            labels[i] = paletteStyleLabel(STYLE_KEYS[i]);
            if (STYLE_KEYS[i].equals(selected)) checked = i;
        }
        new AppDialogBuilder(true).setTitle("配色风格")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    preferences.edit().putString(KEY_PALETTE_STYLE, STYLE_KEYS[which]).apply();
                    dialog.dismiss();
                    recreateTo(PAGE_THEME);
                }).setNegativeButton("取消", null).show();
    }

    private void showColorSpecDialog() {
        final String[] keys = {"SPEC_2021", "SPEC_2025"};
        String[] labels = {"旧版色彩规范", "新版色彩规范"};
        int checked = "SPEC_2021".equals(preferences.getString(KEY_COLOR_SPEC, "SPEC_2025")) ? 0 : 1;
        new AppDialogBuilder(true).setTitle("色彩规范")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    preferences.edit().putString(KEY_COLOR_SPEC, keys[which]).apply();
                    dialog.dismiss();
                    recreateTo(PAGE_THEME);
                }).setNegativeButton("取消", null).show();
    }

    /** 桌面壁纸模式下所有面板共用的不透明度；壁纸关闭时返回 100%，保持原有主题外观。 */
    private int getWallpaperPanelOpacity() {
        if (preferences == null || !isSystemWallpaperEnabled()) return 100;
        return Math.max(0, Math.min(100, preferences.getInt(KEY_WALLPAPER_PANEL_OPACITY, 48)));
    }

    private int applyWallpaperPanelOpacity(int color) {
        if (!isSystemWallpaperEnabled()) return color;
        float opacity = getWallpaperPanelOpacity() / 100f;
        return withAlpha(color, scaleAlpha(Color.alpha(color), opacity));
    }

    private boolean isLiquidGlassEnabled() {
        return preferences == null || preferences.getBoolean(KEY_LIQUID_GLASS, true);
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)), Color.red(color), Color.green(color), Color.blue(color));
    }

    private int scaleAlpha(int alpha, float factor) {
        return Math.max(0, Math.min(255, Math.round(alpha * Math.max(0f, factor))));
    }

    /** 全局玻璃色调，所有调用 createGlassDrawable 的组件共用同一设置。 */
    private int glassTintColor() {
        String tint = preferences == null ? "clear" : preferences.getString(KEY_GLASS_TINT, "clear");
        switch (tint) {
            case "theme": return accentColor;
            case "ice": return Color.rgb(155, 211, 245);
            case "smoke": return darkMode ? Color.rgb(120, 132, 151) : Color.rgb(148, 157, 174);
            case "warm": return Color.rgb(235, 218, 190);
            default: return darkMode ? Color.rgb(210, 218, 231) : Color.WHITE;
        }
    }

    private String glassTintLabel(String tint) {
        switch (tint) {
            case "theme": return "主题色透光";
            case "ice": return "冰蓝";
            case "smoke": return "烟灰";
            case "warm": return "暖雾";
            default: return "清透白";
        }
    }

    private void showGlassTintDialog() {
        final String[] keys = {"clear", "theme", "ice", "smoke", "warm"};
        String[] labels = {"清透白", "主题色透光", "冰蓝", "烟灰", "暖雾"};
        String current = preferences.getString(KEY_GLASS_TINT, "clear");
        int checked = 0;
        for (int i = 0; i < keys.length; i++) if (keys[i].equals(current)) checked = i;
        new AppDialogBuilder(true).setTitle("全局玻璃色调")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    preferences.edit().putString(KEY_GLASS_TINT, keys[which]).apply();
                    dialog.dismiss();
                    updateSettingRowValue("玻璃色调", glassTintLabel(keys[which]));
                    refreshGlobalGlassAppearance();
                }).setNegativeButton("取消", null).show();
    }

    private View createGlassSliderRow(String title, String summary, String key, int min, int max, int defaultValue, String suffix) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(18), dp(11), dp(18), dp(10));
        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = createText(title, 13, Typeface.NORMAL);
        TextView value = createText("", 12, Typeface.NORMAL);
        value.setTextColor(accentColor);
        heading.addView(label, new LinearLayout.LayoutParams(0, -2, 1f));
        heading.addView(value, new LinearLayout.LayoutParams(-2, -2));
        wrap.addView(heading);
        TextView sub = createText(summary, 11, Typeface.NORMAL);
        sub.setTextColor(secondaryTextColor);
        wrap.addView(sub, marginParams(-1, -2, 0, dp(2), 0, 0));
        SeekBar slider = new SeekBar(this);
        slider.setMax(Math.max(1, max - min));
        int initial = Math.max(min, Math.min(max, preferences.getInt(key, defaultValue)));
        slider.setProgress(initial - min);
        slider.setProgressTintList(ColorStateList.valueOf(accentColor));
        slider.setThumbTintList(ColorStateList.valueOf(accentColor));
        value.setText(initial + suffix);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value.setText((min + progress) + suffix);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                int selected = min + seekBar.getProgress();
                if ("%".equals(suffix) && key.equals(KEY_GLASS_RADIUS)) selected = Math.max(min, Math.min(max, Math.round(selected / 5f) * 5));
                preferences.edit().putInt(key, selected).apply();
                refreshGlobalGlassAppearance();
            }
        });
        wrap.addView(slider, marginParams(-1, dp(34), -6, dp(1), -6, 0));
        return wrap;
    }

    /** 全局玻璃弹窗样式；设置选项可选择居中，其余提醒保持顶部布局。 */
    private final class AppDialogBuilder extends AlertDialog.Builder {
        private final boolean centerInScreen;
        private final boolean allowYuanfangTitle;

        // 默认弹窗不添加元芳标识；只有明确指定的应用提醒弹窗才允许添加。
        AppDialogBuilder() { this(false, false); }

        AppDialogBuilder(boolean centerInScreen) { this(centerInScreen, false); }

        AppDialogBuilder(boolean centerInScreen, boolean allowYuanfangTitle) {
            super(MainActivity.this);
            this.centerInScreen = centerInScreen;
            this.allowYuanfangTitle = allowYuanfangTitle;
        }

        @Override public AlertDialog show() {
            AlertDialog dialog = super.show();
            // 元芳标题只允许用于显式标记的应用提醒；设置页/主题页始终禁止。
            boolean decorateTitle = allowYuanfangTitle
                    && currentPage != PAGE_SETTINGS
                    && currentPage != PAGE_THEME;
            styleAppDialog(dialog, centerInScreen, decorateTitle);
            return dialog;
        }
    }

    private void styleAppDialog(AlertDialog dialog, boolean centerInScreen, boolean decorateTitle) {
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window != null) {
            // 选择框玻璃必须画在真正承载弹窗内容的窗口下方；仅设置 Window 背景时，
            // Android 主题自带的不透明 parentPanel 会把它完全盖住，看起来就像开关失效。
            boolean selectorGlass = isSelectorLiquidGlassEnabled();
            window.setBackgroundDrawable(createGlassDrawable(cardColor, 22, selectorGlass));
            View decor = window.getDecorView();
            if (decor != null) {
                int[] panelIds = {
                        getResources().getIdentifier("parentPanel", "id", "android"),
                        getResources().getIdentifier("topPanel", "id", "android"),
                        getResources().getIdentifier("contentPanel", "id", "android"),
                        getResources().getIdentifier("customPanel", "id", "android"),
                        getResources().getIdentifier("buttonPanel", "id", "android")
                };
                for (int panelId : panelIds) {
                    if (panelId == 0) continue;
                    View panelView = decor.findViewById(panelId);
                    if (panelView != null) panelView.setBackgroundColor(Color.TRANSPARENT);
                }
                // 将玻璃表面放到 DecorView，内部面板透明后，独立的选择框开关才会真正可见。
                decor.setBackground(createGlassDrawable(cardColor, 22, selectorGlass));
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            window.setLayout(Math.min(screenWidth - dp(40), dp(420)), ViewGroup.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.28f;
            if (centerInScreen) {
                attributes.gravity = Gravity.CENTER;
                attributes.y = 0;
            } else {
                attributes.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                attributes.y = dp(72);
            }
            window.setAttributes(attributes);
        }
        // 元芳只作为指定应用提醒的轻量副标识；设置页、主题页与普通选项弹窗不添加该标识。
        int alertTitleId = getResources().getIdentifier("alertTitle", "id", "android");
        TextView title = alertTitleId != 0 ? dialog.findViewById(alertTitleId) : null;
        if (title != null) {
            CharSequence rawTitle = title.getText();
            String rawTitleText = rawTitle == null ? "" : rawTitle.toString();
            if (decorateTitle && !rawTitleText.contains("元芳") && !rawTitleText.trim().isEmpty()) {
                String eyebrow = "◈ 元芳 · ";
                SpannableStringBuilder decorated = new SpannableStringBuilder(eyebrow);
                decorated.setSpan(new ForegroundColorSpan(accentColor), 0, eyebrow.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                decorated.setSpan(new RelativeSizeSpan(0.72f), 0, eyebrow.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                decorated.setSpan(new StyleSpan(Typeface.BOLD), 0, eyebrow.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                decorated.append(rawTitle);
                title.setText(decorated);
            }
            title.setTextColor(textColor);
            title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        TextView message = dialog.findViewById(android.R.id.message);
        if (message != null) {
            message.setTextColor(secondaryTextColor);
            message.setTextSize(14);
        }
        int[] buttonIds = {android.R.id.button1, android.R.id.button2, android.R.id.button3};
        for (int id : buttonIds) {
            Button button = dialog.findViewById(id);
            if (button != null) {
                button.setTextColor(accentColor);
                button.setAllCaps(false);
                // AlertDialog button rows can be narrow when several actions are shown together.
                // Keep labels inside their own hit target instead of crossing into adjacent actions.
                button.setMinWidth(0);
                button.setMaxLines(2);
                button.setEllipsize(android.text.TextUtils.TruncateAt.END);
                button.setHorizontallyScrolling(false);
                button.setIncludeFontPadding(false);
                button.setPadding(dp(8), dp(3), dp(8), dp(3));
                if (Build.VERSION.SDK_INT >= 26) {
                    button.setAutoSizeTextTypeUniformWithConfiguration(
                            Math.max(9, Math.round(10 * pageScale)),
                            Math.max(11, Math.round(13 * pageScale)),
                            1,
                            android.util.TypedValue.COMPLEX_UNIT_SP);
                }
                button.setOnTouchListener((view, event) -> {
                    if (preferences != null && preferences.getBoolean(KEY_HAPTIC_FEEDBACK, true)
                            && event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                    return false;
                });
            }
        }
    }

    private boolean isSelectorLiquidGlassEnabled() {
        // Selection dialogs follow the main theme; the independent second switch controls the bottom page bar.
        return isLiquidGlassEnabled();
    }

    private boolean isBottomNavigationGlassEnabled() {
        return preferences == null || preferences.getBoolean(KEY_LIQUID_GLASS_NAV_BAR, true);
    }

    private int getBottomNavigationTopColor() {
        int base = darkMode ? Color.rgb(48, 52, 60) : Color.rgb(255, 255, 255);
        return blendColors(base, glassTintColor(), darkMode ? 0.08f : 0.045f);
    }

    private int getBottomNavigationBottomColor() {
        int base = darkMode ? Color.rgb(29, 32, 38) : Color.rgb(239, 242, 247);
        return blendColors(base, glassTintColor(), darkMode ? 0.07f : 0.035f);
    }

    private int getBottomNavigationIdleTextColor() {
        return darkMode ? Color.rgb(190, 197, 207) : Color.rgb(91, 101, 114);
    }

    private int getBottomNavigationSelectedTextColor() {
        return darkMode ? Color.rgb(249, 251, 255) : Color.rgb(29, 39, 52);
    }

    private int getBottomNavigationSelectionFillColor() {
        if (darkMode) {
            return blendColors(accentColor, Color.rgb(65, 72, 83), 0.16f);
        }
        return blendColors(accentColor, Color.WHITE, 0.12f);
    }

    private int getBottomNavigationSelectionStrokeColor() {
        return withAlpha(accentColor, darkMode ? 116 : 92);
    }

    private GradientDrawable createBottomNavigationPillDrawable(int baseColor, int radiusDp) {
        int topBase = getBottomNavigationTopColor();
        int bottomBase = getBottomNavigationBottomColor();
        GradientDrawable material;
        if (!isBottomNavigationGlassEnabled()) {
            int solid = darkMode ? Color.rgb(27, 30, 36) : Color.rgb(248, 249, 252);
            material = new GradientDrawable();
            material.setColor(solid);
            material.setCornerRadius(dp(radiusDp));
            material.setStroke(dp(1), darkMode ? 0xFF373D47 : 0xFFE0E5EC);
            return material;
        }
        // Bottom navigation owns its own light/dark material. It deliberately ignores wallpaper panel opacity.
        material = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{withAlpha(topBase, darkMode ? 238 : 246),
                        withAlpha(bottomBase, darkMode ? 248 : 242)});
        material.setCornerRadius(dp(radiusDp));
        material.setStroke(dp(1), darkMode ? 0x4AFFFFFF : 0xB8FFFFFF);
        return material;
    }

    private GradientDrawable createNavigationSelectionDrawable(int radiusDp) {
        GradientDrawable selected = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{getBottomNavigationSelectionFillColor(),
                        darkMode ? blendColors(getBottomNavigationSelectionFillColor(), Color.BLACK, 0.92f)
                                : blendColors(getBottomNavigationSelectionFillColor(), Color.rgb(235, 239, 245), 0.88f)});
        selected.setCornerRadius(dp(radiusDp));
        selected.setStroke(dp(1), getBottomNavigationSelectionStrokeColor());
        return selected;
    }

    private void applyBottomNavigationPillBackground() {
        if (selectedNavPill == null) return;
        glassSurfaceSpecs.remove(selectedNavPill);
        selectedNavPill.setBackground(createNavigationSelectionDrawable(28));
        selectedNavPill.setElevation(0f);
    }

    private void applyBottomNavigationGlassSetting() {
        if (navigationShell != null && glassBarShell == null) {
            navigationShell.setBackground(createBottomNavigationPillDrawable(backgroundColor, 0));
        } else if (glassBarShell != null) {
            glassBarShell.setBackgroundColor(Color.TRANSPARENT);
        }
        applyBottomNavigationPillBackground();
        updateBottomNavigation();
        if (glassBarShell != null) {
            glassBarShell.invalidate();
            glassBarShell.refreshBackdrop();
        }
        if (root != null) root.invalidate();
    }

    /** 主题表面使用主题开关；选择框/弹窗调用带 glassEnabled 参数的重载，避免互相串联。 */
    private GradientDrawable createGlassDrawable(int baseColor, int radiusDp) {
        return createGlassDrawable(baseColor, radiusDp, isLiquidGlassEnabled());
    }

    /** 轻薄液态材质：卡片只保留薄雾与细边；底部栏透出系统实时合成的页面内容。 */
    private GradientDrawable createGlassDrawable(int baseColor, int radiusDp, boolean glassEnabled) {
        GradientDrawable drawable;
        if (!glassEnabled) {
            drawable = new GradientDrawable();
            drawable.setColor(applyWallpaperPanelOpacity(baseColor));
            drawable.setCornerRadius(dp(radiusDp));
            // 描边、文字和图标不随面板透明度变淡，避免透明度较低时界面失去辨识度。
            drawable.setStroke(dp(1), dividerColor);
            return drawable;
        }
        int glassTint = glassTintColor();
        String glassTintMode = preferences == null ? "clear" : preferences.getString(KEY_GLASS_TINT, "clear");
        int tinted = blendColors(baseColor, glassTint, "clear".equals(glassTintMode) ? 0.38f : 0.48f);
        int opacitySetting = Math.max(35, Math.min(95, preferences == null ? 62
                : preferences.getInt(KEY_GLASS_OPACITY, 62)));
        float opacity = 0.72f + ((opacitySetting - 35) / 60f) * 0.45f;
        int radiusScale = Math.max(65, Math.min(145, preferences == null ? 100
                : preferences.getInt(KEY_GLASS_RADIUS, 100)));
        int topAlpha;
        int bottomAlpha;
        if (isSystemWallpaperEnabled()) {
            float panelOpacity = getWallpaperPanelOpacity() / 100f;
            float materialOpacity = 0.88f + Math.min(1.0f, Math.max(0f, (opacity - 0.72f) / 0.45f)) * 0.12f;
            topAlpha = Math.round(238f * panelOpacity * materialOpacity);
            bottomAlpha = Math.round(248f * panelOpacity * materialOpacity);
        } else {
            topAlpha = scaleAlpha(darkMode ? 48 : 44, opacity);
            bottomAlpha = scaleAlpha(darkMode ? 34 : 30, opacity);
        }
        int top = withAlpha(blendColors(tinted, Color.WHITE, darkMode ? 0.12f : 0.08f), topAlpha);
        int bottom = withAlpha(blendColors(tinted, backgroundColor, darkMode ? 0.18f : 0.24f), bottomAlpha);
        drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{top, bottom});
        drawable.setCornerRadius(dp(Math.max(4, Math.round(radiusDp * radiusScale / 100f))));
        float highlight = Math.max(0, Math.min(100, preferences == null ? 58
                : preferences.getInt(KEY_GLASS_HIGHLIGHT, 58))) / 70f;
        int edgeAlpha = scaleAlpha(darkMode ? 58 : 64, highlight);
        drawable.setStroke(dp(1), withAlpha(Color.WHITE, edgeAlpha));
        return drawable;
    }

    /** 选择弹窗的玻璃背景跟随主题开关；底部切换栏由独立开关控制。 */
    private void applySelectorGlassBackground(View view, int baseColor, int radiusDp) {
        if (view == null) return;
        GradientDrawable drawable = createGlassDrawable(baseColor, radiusDp, isSelectorLiquidGlassEnabled());
        view.setBackground(drawable);
        if (Build.VERSION.SDK_INT >= 21) {
            view.setElevation(dp(isSelectorLiquidGlassEnabled() ? 2 : 0));
        }
    }

    private void setGlassBackground(View view, int baseColor, int radiusDp) {
        setGlassBackground(view, baseColor, radiusDp, dividerColor);
    }

    private void setGlassBackground(View view, int baseColor, int radiusDp, int disabledStrokeColor) {
        if (view == null) return;
        glassSurfaceSpecs.put(view, new GlassSurfaceSpec(baseColor, radiusDp, disabledStrokeColor));
        GradientDrawable drawable = createGlassDrawable(baseColor, radiusDp);
        if (!isLiquidGlassEnabled()) drawable.setStroke(dp(1), disabledStrokeColor);
        view.setBackground(drawable);
    }

    /** 原位刷新所有登记过的玻璃表面，拖动/切换液态玻璃参数时不重建内容树。 */
    private void refreshGlobalGlassAppearance() {
        for (Map.Entry<View, GlassSurfaceSpec> entry : new ArrayList<>(glassSurfaceSpecs.entrySet())) {
            View surface = entry.getKey();
            GlassSurfaceSpec spec = entry.getValue();
            if (surface == null || spec == null) continue;
            GradientDrawable drawable = createGlassDrawable(spec.baseColor, spec.radiusDp);
            if (!isLiquidGlassEnabled()) drawable.setStroke(dp(1), spec.disabledStrokeColor);
            surface.setBackground(drawable);
        }
        if (navigationShell != null && glassBarShell == null) {
            navigationShell.setBackground(createBottomNavigationPillDrawable(backgroundColor, 0));
        }
        updateBottomNavigation();
        if (glassBarShell != null) {
            glassBarShell.invalidate();
            if (Build.VERSION.SDK_INT >= 21) glassBarShell.invalidateOutline();
            glassBarShell.refreshBackdrop();
        }
        if (root != null) root.invalidate();
        
    }

    /** 仅更新某个设置行右侧的值，不重建整页。 */
    private void updateSettingRowValue(String settingTitle, String value) {
        if (contentContainer == null || settingTitle == null) return;
        updateSettingRowValueRecursive(contentContainer, settingTitle, value);
    }

    private void updateSettingRowValueRecursive(ViewGroup parent, String settingTitle, String value) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (!(child instanceof ViewGroup)) continue;
            ViewGroup group = (ViewGroup) child;
            if (group.getChildCount() > 0 && group.getChildAt(0) instanceof ViewGroup) {
                ViewGroup labels = (ViewGroup) group.getChildAt(0);
                if (labels.getChildCount() > 0 && labels.getChildAt(0) instanceof TextView) {
                    CharSequence text = ((TextView) labels.getChildAt(0)).getText();
                    if (settingTitle.contentEquals(text) && group.getChildCount() > 1) {
                        View trailing = group.getChildAt(group.getChildCount() - 1);
                        if (trailing instanceof TextView) {
                            ((TextView) trailing).setText(value + "  ›");
                            return;
                        }
                    }
                }
            }
            updateSettingRowValueRecursive(group, settingTitle, value);
        }
    }

    private GradientDrawable makeSurfaceDrawable(int color, int stroke, int radiusDp) {
        GradientDrawable d = createGlassDrawable(color, radiusDp);
        if (!isLiquidGlassEnabled()) d.setStroke(dp(1), stroke);
        return d;
    }

    private void copyContact(String value, String message) {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText("contact", value));
            toast(message);
        } catch (Exception ignored) {
            toast(value);
        }
    }

    private TextView createText(
            String text,
            float size,
            int typeface
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(size * pageScale);
        view.setTextColor(textColor);
        view.setTypeface(typeface == Typeface.BOLD
                ? Typeface.create("sans-serif-medium", Typeface.NORMAL)
                : Typeface.create("sans-serif", Typeface.NORMAL));
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setIncludeFontPadding(false);
        return view;
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        // Keep children and long button labels inside the rounded card instead of drawing over siblings.
        card.setClipChildren(true);
        card.setClipToPadding(true);
        setGlassBackground(card, cardColor, 18);
        card.setClipToOutline(true);
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(0));
        return card;
    }

    private Button createButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(13 * pageScale);
        button.setGravity(Gravity.CENTER);
        button.setTextColor(textColor);
        button.setAllCaps(false);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        // Many pages place several controls side-by-side. Allow two compact lines and shrink
        // long labels before they spill into the next button or outside the card.
        button.setMinWidth(0);
        button.setMaxLines(2);
        button.setEllipsize(android.text.TextUtils.TruncateAt.END);
        button.setHorizontallyScrolling(false);
        button.setIncludeFontPadding(false);
        button.setPadding(dp(10), dp(5), dp(10), dp(5));
        button.setMinHeight(dp(48));
        if (Build.VERSION.SDK_INT >= 26) {
            button.setAutoSizeTextTypeUniformWithConfiguration(11, 14, 1,
                    android.util.TypedValue.COMPLEX_UNIT_SP);
        }
        button.setElevation(0);
        if (Build.VERSION.SDK_INT >= 21) {
            button.setStateListAnimator(null);
        }
        setGlassBackground(button, darkMode ? Color.rgb(35, 36, 40) : Color.rgb(244, 245, 248), 14);
        button.setOnTouchListener((view, event) -> {
            boolean scaleEnabled = preferences == null || preferences.getBoolean(KEY_TOUCH_FEEDBACK, true);
            boolean hapticEnabled = preferences != null && preferences.getBoolean(KEY_HAPTIC_FEEDBACK, true);
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (scaleEnabled) {
                    view.animate().cancel();
                    view.animate().scaleX(0.975f).scaleY(0.975f).setDuration(75L).start();
                }
                if (hapticEnabled) view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                view.animate().cancel();
                view.animate().scaleX(1f).scaleY(1f).setDuration(scaleEnabled ? 125L : 0L)
                        .setInterpolator(new DecelerateInterpolator()).start();
            }
            return false;
        });
        return button;
    }

    private TextView addInfoRow(
            LinearLayout parent,
            String title,
            String value
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView left =
                createText(
                        title,
                        14,
                        Typeface.NORMAL
                );

        left.setTextColor(
                secondaryTextColor
        );

        TextView right =
                createText(
                        value,
                        14,
                        Typeface.BOLD
                );

        right.setGravity(
                Gravity.RIGHT |
                        Gravity.CENTER_VERTICAL
        );

        row.addView(
                left,
                new LinearLayout.LayoutParams(
                        0,
                        dp(40),
                        1
                )
        );

        row.addView(
                right,
                new LinearLayout.LayoutParams(
                        0,
                        dp(40),
                        1
                )
        );

        parent.addView(row);

        return right;
    }

    private TextView addStat(
            LinearLayout parent,
            String title,
            String value
    ) {

        LinearLayout card =
                createCard();

        card.setGravity(
                Gravity.CENTER
        );

        TextView valueView =
                createText(
                        value,
                        25,
                        Typeface.BOLD
                );

        valueView.setGravity(
                Gravity.CENTER
        );

        TextView titleView =
                createText(
                        title,
                        13,
                        Typeface.NORMAL
                );

        titleView.setTextColor(
                secondaryTextColor
        );

        titleView.setGravity(
                Gravity.CENTER
        );

        card.addView(valueView);
        card.addView(titleView);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                );

        params.rightMargin =
                dp(6);

        parent.addView(
                card,
                params
        );

        return valueView;
    }

    private LinearLayout.LayoutParams marginParams(
            int width,
            int height,
            int left,
            int top,
            int right,
            int bottom
    ) {

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        width,
                        height
                );

        p.leftMargin = dp(left);
        p.topMargin = dp(top);
        p.rightMargin = dp(right);
        p.bottomMargin = dp(bottom);

        return p;
    }

    // =========================================================
    // 权限
    // =========================================================

    private boolean hasBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private boolean hasBluetoothPermission() {

        if (Build.VERSION.SDK_INT >= 31) {
            return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRequiredPermissions() {

        ArrayList<String> permissions =
                new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 31) {

            if (checkSelfPermission(
                    Manifest.permission
                            .BLUETOOTH_SCAN
            ) !=
                    PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission
                                .BLUETOOTH_SCAN
                );
            }

            if (checkSelfPermission(
                    Manifest.permission
                            .BLUETOOTH_CONNECT
            ) !=
                    PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission
                                .BLUETOOTH_CONNECT
                );
            }

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

        } else {

            if (checkSelfPermission(
                    Manifest.permission
                            .ACCESS_FINE_LOCATION
            ) !=
                    PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission
                                .ACCESS_FINE_LOCATION
                );
            }
        }

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    Manifest.permission
                            .POST_NOTIFICATIONS
            ) !=
                    PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission
                                .POST_NOTIFICATIONS
                );
            }
        }

        if (!permissions.isEmpty()) {

            requestPermissions(
                    permissions.toArray(
                            new String[0]
                    ),
                    1001
            );
        }
    }

    // =========================================================
    // 数据
    // =========================================================

    private int getTodayCount() {

        HeartDatabase db =
                new HeartDatabase(this);

        return db.getTodayCount();
    }

    private String getLastTime() {

        HeartDatabase db =
                new HeartDatabase(this);

        long timestamp =
                db.getLastTimestamp();

        if (timestamp <= 0) {
            return "--";
        }

        return formatTime(timestamp);
    }

    private String formatTime(
            long timestamp
    ) {

        return new SimpleDateFormat(
                "MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(
                new Date(timestamp)
        );
    }

    private String bytesToString(
            byte[] bytes
    ) {

        if (bytes == null ||
                bytes.length == 0) {

            return "未知";
        }

        return new String(bytes).trim();
    }

    private int dp(float value) {
        return Math.round(value * pageScale * getResources().getDisplayMetrics().density);
    }

    private int dp(int value) {
        return (int) (
                value * pageScale *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    private void disconnectDevice() {
        userRequestedDisconnect = true;
        monitoring = false;
        stopMonitoringForegroundService();
        handler.removeCallbacks(reconnectRunnable);
        handler.removeCallbacks(batteryPollRunnable);
        handler.removeCallbacks(connectTimeoutRunnable);
        handler.removeCallbacks(serviceDiscoveryTimeoutRunnable);
        servicesDiscovered = false;
        preferences.edit().putBoolean(KEY_MONITOR, false).apply();

        BluetoothGatt oldGatt = bluetoothGatt;
        bluetoothGatt = null;
        connected = false;
        lastBluetoothDiagnostic = "用户主动断开 BLE 直连";
        resetGattOperationQueue();
        if (oldGatt != null) {
            try { if (hasBluetoothConnectPermission()) oldGatt.disconnect(); } catch (Exception ignored) { }
            try { oldGatt.close(); } catch (Exception ignored) { }
        }
        clearEcgCharts();
        updateStatusViews();
        toast("已断开蓝牙直连");
    }

    /** 合并短时间内重复的轻提示，角色元素只放在少量有上下文的弹窗文案中。 */
    private void toast(String message) {
        if (message == null || message.trim().isEmpty() || isFinishing()) return;
        String clean = message.trim();
        long now = SystemClock.uptimeMillis();
        if (clean.equals(lastToastMessage) && now - lastToastUptime < 1400L) return;
        lastToastMessage = clean;
        lastToastUptime = now;
        Toast.makeText(this, clean, Toast.LENGTH_SHORT).show();
    }

    // =========================================================
    // 心率曲线
    // =========================================================

    /**
     * 心电图（波形取自 LuckyEcgDemo）。颜色全部取自当前主题设置，
     * 每收到一条新心率就跳一次，波峰高度对应心率数值。
     */
    public class EcgChart extends LuckyEcgView {
        public EcgChart(Context context) {
            super(context);
            setColors(accentColor, dividerColor, secondaryTextColor);
            ecgCharts.add(this);
        }

        void onHeartRateSample(int bpm) {
            if (bpm <= 0) return;
            pushBpm(bpm);
        }

        @Override protected void onDetachedFromWindow() {
            ecgCharts.remove(this);
            super.onDetachedFromWindow();
        }
    }

    public class HeartRateChart
            extends View {

        private final Paint linePaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint fillPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint gridPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint textPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint bigTextPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint dotPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final ArrayList<Integer> values =
                new ArrayList<>();
        private int maxValues = 80;

        public void setMaxValues(int maxValues) {
            this.maxValues = Math.max(80, maxValues);
            trimValues();
        }

        private void trimValues() {
            while (values.size() > maxValues) values.remove(0);
        }

        private final Path linePath =
                new Path();

        private final Path fillPath =
                new Path();

        public HeartRateChart(
                Context context
        ) {

            super(context);

            setLayerType(
                    View.LAYER_TYPE_SOFTWARE,
                    null
            );

            linePaint.setColor(
                    accentColor
            );

            linePaint.setStyle(
                    Paint.Style.STROKE
            );

            linePaint.setStrokeWidth(
                    dp(3)
            );

            linePaint.setStrokeCap(
                    Paint.Cap.ROUND
            );

            linePaint.setStrokeJoin(
                    Paint.Join.ROUND
            );

            linePaint.setShadowLayer(
                    dp(5),
                    0,
                    dp(2),
                    Color.argb(
                            70,
                            235,
                            61,
                            76
                    )
            );

            gridPaint.setColor(
                    dividerColor
            );

            gridPaint.setStrokeWidth(
                    dp(1)
            );

            fillPaint.setStyle(
                    Paint.Style.FILL
            );

            textPaint.setColor(
                    secondaryTextColor
            );

            textPaint.setTextSize(
                    dp(11)
            );

            bigTextPaint.setColor(
                    textColor
            );

            bigTextPaint.setTypeface(
                    Typeface.DEFAULT_BOLD
            );

            dotPaint.setColor(
                    accentColor
            );

            // 首页趋势图也纳入全局液态玻璃参数，调整浓度/高光/圆角时原位刷新。
            setGlassBackground(this, cardColor, 22);
        }

        public void addValue(
                int value
        ) {

            if (value <= 0) {
                return;
            }

            values.add(value);

            trimValues();
            invalidate();
        }

        public void setValues(
                ArrayList<Integer> list
        ) {

            values.clear();

            if (list != null) {

                for (Integer value : list) {

                    if (value != null &&
                            value > 0) {

                        values.add(value);
                    }
                }
            }

            trimValues();
            invalidate();
        }

        @Override
        protected void onDraw(
                Canvas canvas
        ) {

            super.onDraw(canvas);

            float width =
                    getWidth();

            float height =
                    getHeight();

            if (width <= 0 ||
                    height <= 0) {

                return;
            }

            int current =
                    values.isEmpty()
                            ? currentHeartRate
                            : values.get(
                                    values.size() - 1
                            );

            int min =
                    getMin();

            int max =
                    getMax();

            int average =
                    getAverage();

            bigTextPaint.setTextSize(
                    dp(28)
            );

            String currentText =
                    current > 0
                            ? String.valueOf(current)
                            : "--";

            canvas.drawText(
                    currentText,
                    dp(18),
                    dp(38),
                    bigTextPaint
            );

            textPaint.setTextSize(
                    dp(11)
            );

            canvas.drawText(
                    "BPM",
                    dp(18),
                    dp(55),
                    textPaint
            );

            String summary =
                    values.size() > 0
                            ? "最低 " +
                              min +
                              "   平均 " +
                              average +
                              "   最高 " +
                              max
                            : "等待设备发送心率数据";

            canvas.drawText(
                    summary,
                    dp(78),
                    dp(42),
                    textPaint
            );

            float left =
                    dp(18);

            float right =
                    width - dp(18);

            float top =
                    dp(76);

            float bottom =
                    height - dp(25);

            float chartWidth =
                    right - left;

            float chartHeight =
                    bottom - top;

            if (chartWidth <= 0 ||
                    chartHeight <= 0) {

                return;
            }

            if (values.size() < 2) {

                gridPaint.setColor(
                        dividerColor
                );

                for (int i = 0;
                     i < 4;
                     i++) {

                    float y =
                            top +
                                    chartHeight *
                                            i /
                                            3f;

                    canvas.drawLine(
                            left,
                            y,
                            right,
                            y,
                            gridPaint
                    );
                }

                textPaint.setTextSize(
                        dp(13)
                );

                String empty =
                        "等待心率数据…";

                float tw =
                        textPaint.measureText(
                                empty
                        );

                canvas.drawText(
                        empty,
                        (width - tw) / 2f,
                        top +
                                chartHeight / 2f,
                        textPaint
                );

                return;
            }

            int dataMin =
                    Integer.MAX_VALUE;

            int dataMax =
                    Integer.MIN_VALUE;

            for (int value : values) {

                dataMin =
                        Math.min(
                                dataMin,
                                value
                        );

                dataMax =
                        Math.max(
                                dataMax,
                                value
                        );
            }

            int range =
                    dataMax - dataMin;

            int padding;

            if (range < 20) {

                padding = 15;

            } else if (range < 40) {

                padding = 12;

            } else {

                padding = 8;
            }

            int axisMin =
                    Math.max(
                            30,
                            dataMin - padding
                    );

            int axisMax =
                    Math.min(
                            220,
                            dataMax + padding
                    );

            if (axisMax - axisMin < 30) {

                int center =
                        (axisMax + axisMin) / 2;

                axisMin =
                        Math.max(
                                30,
                                center - 15
                        );

                axisMax =
                        Math.min(
                                220,
                                center + 15
                        );
            }

            gridPaint.setColor(
                    dividerColor
            );

            for (int i = 0;
                 i < 4;
                 i++) {

                float ratio =
                        i / 3f;

                float y =
                        top +
                                chartHeight *
                                        ratio;

                canvas.drawLine(
                        left,
                        y,
                        right,
                        y,
                        gridPaint
                );

                int label =
                        Math.round(
                                axisMax -
                                        (
                                                axisMax -
                                                        axisMin
                                        ) *
                                        ratio
                        );

                textPaint.setTextSize(
                        dp(10)
                );

                String labelText =
                        String.valueOf(label);

                canvas.drawText(
                        labelText,
                        right -
                                dp(30),
                        y -
                                dp(4),
                        textPaint
                );
            }

            int count =
                    values.size();

            float step =
                    count <= 1
                            ? chartWidth
                            : chartWidth /
                                    (count - 1);

            float[] xs =
                    new float[count];

            float[] ys =
                    new float[count];

            for (int i = 0;
                 i < count;
                 i++) {

                float x =
                        left +
                                i * step;

                float normalized =
                        (
                                values.get(i)
                                        - axisMin
                        )
                                /
                                (float)
                                        (
                                                axisMax -
                                                        axisMin
                                        );

                normalized =
                        Math.max(
                                0f,
                                Math.min(
                                        1f,
                                        normalized
                                )
                        );

                float y =
                        bottom -
                                normalized *
                                        chartHeight;

                xs[i] = x;
                ys[i] = y;
            }

            linePath.reset();

            linePath.moveTo(
                    xs[0],
                    ys[0]
            );

            for (int i = 1;
                 i < count;
                 i++) {

                float midX =
                        (
                                xs[i - 1] +
                                        xs[i]
                        ) / 2f;

                float midY =
                        (
                                ys[i - 1] +
                                        ys[i]
                        ) / 2f;

                linePath.quadTo(
                        xs[i - 1],
                        ys[i - 1],
                        midX,
                        midY
                );
            }

            linePath.quadTo(
                    xs[count - 2],
                    ys[count - 2],
                    xs[count - 1],
                    ys[count - 1]
            );

            fillPath.reset();

            fillPath.moveTo(
                    xs[0],
                    bottom
            );

            fillPath.lineTo(
                    xs[0],
                    ys[0]
            );

            for (int i = 1;
                 i < count;
                 i++) {

                float midX =
                        (
                                xs[i - 1] +
                                        xs[i]
                        ) / 2f;

                float midY =
                        (
                                ys[i - 1] +
                                        ys[i]
                        ) / 2f;

                fillPath.quadTo(
                        xs[i - 1],
                        ys[i - 1],
                        midX,
                        midY
                );
            }

            fillPath.quadTo(
                    xs[count - 2],
                    ys[count - 2],
                    xs[count - 1],
                    ys[count - 1]
            );

            fillPath.lineTo(
                    xs[count - 1],
                    bottom
            );

            fillPath.close();

            fillPaint.setShader(
                    new LinearGradient(
                            0,
                            top,
                            0,
                            bottom,
                            Color.argb(
                                    65,
                                    235,
                                    61,
                                    76
                            ),
                            Color.argb(
                                    0,
                                    235,
                                    61,
                                    76
                            ),
                            Shader.TileMode.CLAMP
                    )
            );

            canvas.drawPath(
                    fillPath,
                    fillPaint
            );

            fillPaint.setShader(null);

            canvas.drawPath(
                    linePath,
                    linePaint
            );

            float latestX =
                    xs[count - 1];

            float latestY =
                    ys[count - 1];

            dotPaint.setColor(
                    accentColor
            );

            canvas.drawCircle(
                    latestX,
                    latestY,
                    dp(6),
                    dotPaint
            );

            dotPaint.setColor(
                    cardColor
            );

            canvas.drawCircle(
                    latestX,
                    latestY,
                    dp(3),
                    dotPaint
            );

            textPaint.setTextSize(
                    dp(11)
            );

            textPaint.setColor(
                    accentColor
            );

            String latestText =
                    current +
                            " BPM";

            float latestWidth =
                    textPaint.measureText(
                            latestText
                    );

            float labelX =
                    latestX -
                            latestWidth -
                            dp(10);

            if (labelX < left) {

                labelX =
                        latestX +
                                dp(10);
            }

            float labelY =
                    latestY -
                            dp(10);

            if (labelY < top + dp(15)) {

                labelY =
                        latestY +
                                dp(20);
            }

            canvas.drawText(
                    latestText,
                    labelX,
                    labelY,
                    textPaint
            );

            textPaint.setColor(
                    secondaryTextColor
            );

            canvas.drawText(
                    "较早",
                    left,
                    height - dp(7),
                    textPaint
            );

            String newest =
                    "最新";

            float newestWidth =
                    textPaint.measureText(
                            newest
                    );

            canvas.drawText(
                    newest,
                    right - newestWidth,
                    height - dp(7),
                    textPaint
            );
        }

        private int getMin() {

            if (values.isEmpty()) {
                return 0;
            }

            int min =
                    Integer.MAX_VALUE;

            for (int value : values) {

                min =
                        Math.min(
                                min,
                                value
                        );
            }

            return min;
        }

        private int getMax() {

            if (values.isEmpty()) {
                return 0;
            }

            int max =
                    Integer.MIN_VALUE;

            for (int value : values) {

                max =
                        Math.max(
                                max,
                                value
                        );
            }

            return max;
        }

        private int getAverage() {

            if (values.isEmpty()) {
                return 0;
            }

            long total = 0;

            for (int value : values) {

                total += value;
            }

            return (int)
                    (
                            total /
                                    values.size()
                    );
        }
    }
}