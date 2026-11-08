package com.shuangnian;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HeartOverlayService extends Service {

    private static final String CHANNEL_ID = "heart_overlay_channel";
    private static final int NOTIFICATION_ID = 1001;

    public static final String ACTION_UPDATE =
            "com.shuangnian.UPDATE_OVERLAY";

    public static final String ACTION_REFRESH =
            "com.shuangnian.REFRESH_OVERLAY";

    public static final String ACTION_SETTINGS =
            "com.shuangnian.UPDATE_OVERLAY_SETTINGS";

    private static final String PREFS = "app_preferences";
    private static final String KEY_OVERLAY = "overlay";

    private static final String KEY_OVERLAY_SIZE = "overlay_size";
    private static final String KEY_OVERLAY_ALPHA = "overlay_alpha";
    private static final String KEY_OVERLAY_LOCKED = "overlay_locked";
    private static final String KEY_OVERLAY_X = "overlay_x";
    private static final String KEY_OVERLAY_Y = "overlay_y";

    private static final float MIN_SCALE = 0.50f;
    private static final float MAX_SCALE = 1.30f;

    private static final float DEFAULT_SCALE = 0.90f;

    private static final float MIN_ALPHA = 0.30f;
    private static final float MAX_ALPHA = 1.00f;
    private static final float DEFAULT_ALPHA = 0.92f;

    // 紧凑型悬浮胶囊：减小外边距、图标和数字槽空隙。
    private static final int HORIZONTAL_PADDING = 3;
    private static final int VERTICAL_PADDING = 1;

    private static final int ICON_SIZE = 34;
    private static final int TEXT_HEIGHT = 36;
    // 保持三位心率数字稳定，不再预留过宽空槽。
    private static final int TEXT_SLOT_WIDTH = 52;
    private static final int ICON_TEXT_GAP = 0;

    private WindowManager windowManager;

    private LinearLayout overlayView;
    private LinearLayout contentLayout;
    private GradientDrawable overlayBackground;

    private TextView heartIcon;
    private TextView heartText;

    private WindowManager.LayoutParams params;

    private SharedPreferences preferences;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private volatile int heartRate = 0;

    private float downX;
    private float downY;

    private int startX;
    private int startY;

    private boolean moved = false;

    private float appliedScale = DEFAULT_SCALE;

    private boolean rebuilding = false;

    private boolean receiverRegistered = false;

    private final BroadcastReceiver receiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(
                        Context context,
                        Intent intent
                ) {

                    if (intent == null) {
                        return;
                    }

                    String action = intent.getAction();

                    if (ACTION_UPDATE.equals(action)) {

                        if (intent.hasExtra("heart_rate")) {

                            heartRate =
                                    intent.getIntExtra(
                                            "heart_rate",
                                            heartRate
                                    );

                            mainHandler.post(
                                    () -> refreshHeartRateInternal()
                            );
                        }

                        return;
                    }

                    if (ACTION_REFRESH.equals(action)) {

                        mainHandler.post(() -> {

                            if (preferences == null || !preferences.getBoolean(KEY_OVERLAY, false)) {
                                stopSelf();
                                return;
                            }
                            if (!hasOverlayPermission()) {
                                return;
                            }

                            if (overlayView == null) {
                                createOverlay();
                            }

                            applyOverlaySettings();
                            refreshHeartRateInternal();
                            syncWidgetOverlayReady();
                        });

                        return;
                    }

                    if (ACTION_SETTINGS.equals(action)) {

                        applySettingsFromIntent(intent);
                    }
                }
            };

    @Override
    public void onCreate() {

        super.onCreate();

        preferences =
                getSharedPreferences(
                        PREFS,
                        MODE_PRIVATE
                );

        // START_STICKY 可能在进程回收后重建服务；只有设置仍开启时才允许重建悬浮窗。
        if (!preferences.getBoolean(KEY_OVERLAY, false)) {
            stopSelf();
            return;
        }

        createNotificationChannel();

        startAsForegroundService();

        registerOverlayReceiver();

        mainHandler.post(() -> {

            if (!hasOverlayPermission()) {
                return;
            }

            if (overlayView == null) {
                createOverlay();
            }

            refreshHeartRateInternal();
            syncWidgetOverlayReady();
        });
    }

    private boolean hasOverlayPermission() {

        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        return Settings.canDrawOverlays(this);
    }

    private void startAsForegroundService() {

        Notification notification =
                createNotification();

        try {

            if (Build.VERSION.SDK_INT >= 29) {

                startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo
                                .FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                );

            } else {

                startForeground(
                        NOTIFICATION_ID,
                        notification
                );
            }

        } catch (Exception e) {

            try {

                startForeground(
                        NOTIFICATION_ID,
                        notification
                );

            } catch (Exception ignored) {
            }
        }
    }

    private void registerOverlayReceiver() {

        if (receiverRegistered) {
            return;
        }

        IntentFilter filter =
                new IntentFilter();

        filter.addAction(ACTION_UPDATE);
        filter.addAction(ACTION_REFRESH);
        filter.addAction(ACTION_SETTINGS);

        try {

            if (Build.VERSION.SDK_INT >= 33) {

                registerReceiver(
                        receiver,
                        filter,
                        Context.RECEIVER_NOT_EXPORTED
                );

            } else {

                registerReceiver(
                        receiver,
                        filter
                );
            }

            receiverRegistered = true;

        } catch (Exception ignored) {
        }
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {

        if (preferences == null) {
            preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        }
        if (!preferences.getBoolean(KEY_OVERLAY, false)) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        if (intent != null && intent.hasExtra("heart_rate")) {
            heartRate = intent.getIntExtra("heart_rate", heartRate);
        }
        // A newly started overlay should immediately show the latest real sample saved by the app.
        // Subsequent heart-rate samples are pushed through ACTION_UPDATE by MainActivity.
        if (heartRate <= 0) {
            heartRate = preferences.getInt(HeartControlWidgetProvider.KEY_LAST_HEART, 0);
        }

        mainHandler.post(() -> {

            if (!hasOverlayPermission()) {
                return;
            }

            if (overlayView == null) {

                createOverlay();

            } else {

                applyOverlaySettings();
            }

            refreshHeartRateInternal();
            syncWidgetOverlayReady();
        });

        // Don't leave the launcher widget showing the old state while the window is attaching.
        HeartControlWidgetProvider.refreshWidgets(this);
        return START_STICKY;
    }

    private Notification createNotification() {

        Intent intent =
                new Intent(
                        this,
                        MainActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= 26) {

            builder =
                    new Notification.Builder(
                            this,
                            CHANNEL_ID
                    );

        } else {

            builder =
                    new Notification.Builder(this);
        }

        builder.setContentTitle("心率监测");

        builder.setContentText(
                "悬浮窗正在显示实时心率"
        );

        builder.setSmallIcon(
                android.R.drawable.ic_dialog_info
        );

        builder.setContentIntent(
                pendingIntent
        );

        builder.setOngoing(true);
        builder.setShowWhen(false);

        return builder.build();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        NotificationManager manager =
                getSystemService(
                        NotificationManager.class
                );

        if (manager == null) {
            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        "心率悬浮窗",
                        NotificationManager.IMPORTANCE_LOW
                );

        channel.setDescription(
                "显示实时心率悬浮窗"
        );

        channel.setShowBadge(false);

        manager.createNotificationChannel(channel);
    }

    private void createOverlay() {

        if (!hasOverlayPermission()) {
            return;
        }

        if (overlayView != null) {
            return;
        }

        windowManager =
                (WindowManager)
                        getSystemService(
                                WINDOW_SERVICE
                        );

        if (windowManager == null) {
            return;
        }

        float scale =
                getOverlayScale();

        appliedScale = scale;

        overlayView =
                new LinearLayout(this);

        overlayView.setOrientation(
                LinearLayout.HORIZONTAL
        );

        overlayView.setGravity(
                Gravity.CENTER
        );

        overlayView.setPadding(
                scaledDp(
                        HORIZONTAL_PADDING,
                        scale
                ),
                scaledDp(
                        VERTICAL_PADDING,
                        scale
                ),
                scaledDp(
                        HORIZONTAL_PADDING,
                        scale
                ),
                scaledDp(
                        VERTICAL_PADDING,
                        scale
                )
        );

        // 悬浮窗不是液态玻璃：只让胶囊底色和边缘描线按透明度设置变化。
        // 爱心和心率文字在独立子 View 中绘制，始终保持不透明。
        overlayBackground = new GradientDrawable();
        overlayBackground.setCornerRadius(scaledDp(50, scale));
        updateOverlayBackground(getOverlayAlpha());
        overlayView.setBackground(overlayBackground);

        contentLayout =
                new LinearLayout(this);

        contentLayout.setOrientation(
                LinearLayout.HORIZONTAL
        );

        contentLayout.setGravity(
                Gravity.CENTER
        );
        contentLayout.setMinimumWidth(scaledDp(ICON_SIZE + TEXT_SLOT_WIDTH, scale));

        LinearLayout.LayoutParams contentParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );

        overlayView.addView(
                contentLayout,
                contentParams
        );

        /*
         * 爱心。
         */
        heartIcon =
                new TextView(this);

        heartIcon.setText("♥");

        heartIcon.setTextColor(
                Color.rgb(
                        255,
                        65,
                        75
                )
        );

        heartIcon.setTextSize(
                22 * scale
        );

        heartIcon.setGravity(
                Gravity.CENTER
        );

        heartIcon.setIncludeFontPadding(false);

        heartIcon.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(
                        scaledDp(
                                ICON_SIZE,
                                scale
                        ),
                        scaledDp(
                                ICON_SIZE,
                                scale
                        )
                );

        contentLayout.addView(
                heartIcon,
                iconParams
        );

        /*
         * 心率数字。
         */
        heartText =
                new TextView(this);

        heartText.setText(
                heartRate > 0
                        ? String.valueOf(heartRate)
                        : "--"
        );

        heartText.setTextColor(
                Color.WHITE
        );

        heartText.setTextSize(
                24 * scale
        );

        heartText.setGravity(
                Gravity.CENTER
        );

        heartText.setIncludeFontPadding(false);

        heartText.setTypeface(
                Typeface.MONOSPACE,
                Typeface.NORMAL
        );

        heartText.setSingleLine(true);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        scaledDp(TEXT_SLOT_WIDTH, scale),
                        scaledDp(
                                TEXT_HEIGHT,
                                scale
                        )
                );

        /*
         * 不留额外间距，让数字贴近爱心。
         */
        textParams.leftMargin =
                scaledDp(
                        ICON_TEXT_GAP,
                        scale
                );

        contentLayout.addView(
                heartText,
                textParams
        );

        int windowType;

        if (Build.VERSION.SDK_INT >= 26) {

            windowType =
                    WindowManager.LayoutParams
                            .TYPE_APPLICATION_OVERLAY;

        } else {

            windowType =
                    WindowManager.LayoutParams
                            .TYPE_PHONE;
        }

        int flags =
                WindowManager.LayoutParams
                        .FLAG_NOT_FOCUSABLE;

        int overlayHeight =
                scaledDp(
                        40,
                        scale
                );

        params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        overlayHeight,
                        windowType,
                        flags,
                        PixelFormat.TRANSLUCENT
                );

        params.gravity =
                Gravity.TOP |
                        Gravity.START;

        params.x =
                preferences.getInt(
                        KEY_OVERLAY_X,
                        80
                );

        params.y =
                preferences.getInt(
                        KEY_OVERLAY_Y,
                        180
                );

        // Window alpha 会连文字和爱心一起淡掉，因此固定为 1。
        params.alpha = 1.0f;

        try {

            windowManager.addView(
                    overlayView,
                    params
            );

        } catch (Exception e) {

            overlayView = null;
            contentLayout = null;
            overlayBackground = null;
            heartIcon = null;
            heartText = null;
            params = null;

            return;
        }

        setupTouchListener();

        updateLockState();

        mainHandler.post(
                this::refreshHeartRateInternal
        );
    }

    private void setupTouchListener() {

        if (overlayView == null) {
            return;
        }

        overlayView.setOnTouchListener(
                (view, event) -> {

                    if (getOverlayLocked()) {
                        return true;
                    }

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:

                            downX =
                                    event.getRawX();

                            downY =
                                    event.getRawY();

                            if (params != null) {

                                startX =
                                        params.x;

                                startY =
                                        params.y;
                            }

                            moved = false;

                            return true;

                        case MotionEvent.ACTION_MOVE:

                            if (params == null ||
                                    windowManager == null ||
                                    overlayView == null) {

                                return true;
                            }

                            float dx =
                                    event.getRawX()
                                            - downX;

                            float dy =
                                    event.getRawY()
                                            - downY;

                            if (!moved) {
                                if (Math.abs(dx) < dp(9) && Math.abs(dy) < dp(9)) {
                                    return true;
                                }
                                moved = true;
                            }

                            params.x =
                                    startX +
                                            (int) dx;

                            params.y =
                                    startY +
                                            (int) dy;

                            try {

                                windowManager.updateViewLayout(
                                        overlayView,
                                        params
                                );

                            } catch (Exception ignored) {
                            }

                            return true;

                        case MotionEvent.ACTION_UP:

                            if (params != null) {

                                preferences.edit()
                                        .putInt(
                                                KEY_OVERLAY_X,
                                                params.x
                                        )
                                        .putInt(
                                                KEY_OVERLAY_Y,
                                                params.y
                                        )
                                        .apply();
                            }

                            return true;
                    }

                    return true;
                }
        );
    }

    private void syncWidgetOverlayReady() {
        boolean ready = overlayView != null && hasOverlayPermission()
                && preferences != null
                && preferences.getBoolean(KEY_OVERLAY, false);
        try {
            if (preferences != null) {
                preferences.edit().putBoolean(HeartControlWidgetProvider.KEY_OVERLAY_READY, ready).apply();
            }
            HeartControlWidgetProvider.refreshWidgets(this);
        } catch (Exception ignored) { }
    }

    private void refreshHeartRateInternal() {

        if (heartText == null ||
                overlayView == null ||
                params == null) {

            return;
        }

        String value =
                heartRate > 0
                        ? String.valueOf(heartRate)
                        : "--";

        heartText.setText(value);

        heartText.measure(
                View.MeasureSpec.makeMeasureSpec(
                        0,
                        View.MeasureSpec.UNSPECIFIED
                ),
                View.MeasureSpec.makeMeasureSpec(
                        scaledDp(
                                TEXT_HEIGHT,
                                appliedScale
                        ),
                        View.MeasureSpec.EXACTLY
                )
        );

        contentLayout.requestLayout();
        overlayView.requestLayout();

        overlayView.measure(
                View.MeasureSpec.makeMeasureSpec(
                        0,
                        View.MeasureSpec.UNSPECIFIED
                ),
                View.MeasureSpec.makeMeasureSpec(
                        params.height,
                        View.MeasureSpec.EXACTLY
                )
        );

        int width =
                overlayView.getMeasuredWidth();

        if (width <= 0) {
            return;
        }

        params.width = width;

        try {

            windowManager.updateViewLayout(
                    overlayView,
                    params
            );

        } catch (Exception ignored) {
        }

        heartText.invalidate();
        heartIcon.invalidate();
        overlayView.invalidate();
    }

    private void applySettingsFromIntent(
            Intent intent
    ) {

        if (intent == null) {
            return;
        }

        if (intent.hasExtra(KEY_OVERLAY_SIZE)) {

            float scale =
                    intent.getFloatExtra(
                            KEY_OVERLAY_SIZE,
                            DEFAULT_SCALE
                    );

            scale =
                    clamp(
                            scale,
                            MIN_SCALE,
                            MAX_SCALE
                    );

            preferences.edit()
                    .putFloat(
                            KEY_OVERLAY_SIZE,
                            scale
                    )
                    .apply();
        }

        if (intent.hasExtra(KEY_OVERLAY_ALPHA)) {

            float alpha =
                    intent.getFloatExtra(
                            KEY_OVERLAY_ALPHA,
                            DEFAULT_ALPHA
                    );

            alpha =
                    clamp(
                            alpha,
                            MIN_ALPHA,
                            MAX_ALPHA
                    );

            preferences.edit()
                    .putFloat(
                            KEY_OVERLAY_ALPHA,
                            alpha
                    )
                    .apply();
        }

        if (intent.hasExtra(KEY_OVERLAY_LOCKED)) {

            boolean locked =
                    intent.getBooleanExtra(
                            KEY_OVERLAY_LOCKED,
                            false
                    );

            preferences.edit()
                    .putBoolean(
                            KEY_OVERLAY_LOCKED,
                            locked
                    )
                    .apply();
        }

        mainHandler.post(
                this::applyOverlaySettings
        );
    }

    private void applyOverlaySettings() {

        if (overlayView == null ||
                params == null ||
                windowManager == null) {

            return;
        }

        if (rebuilding) {
            return;
        }

        float newScale =
                getOverlayScale();

        float newAlpha =
                getOverlayAlpha();

        boolean scaleChanged =
                Math.abs(
                        newScale -
                                appliedScale
                ) > 0.001f;

        if (scaleChanged) {

            rebuildOverlay(
                    newScale,
                    newAlpha
            );

            return;
        }

        // 只改胶囊底色透明度，心形和数字本身不受影响。
        updateOverlayBackground(newAlpha);
        params.alpha = 1.0f;

        try {

            windowManager.updateViewLayout(
                    overlayView,
                    params
            );

        } catch (Exception ignored) {
        }

        updateLockState();

        refreshHeartRateInternal();
    }

    private void rebuildOverlay(
            float newScale,
            float newAlpha
    ) {

        if (windowManager == null ||
                rebuilding) {

            return;
        }

        rebuilding = true;

        int oldX =
                params != null
                        ? params.x
                        : preferences.getInt(
                                KEY_OVERLAY_X,
                                80
                        );

        int oldY =
                params != null
                        ? params.y
                        : preferences.getInt(
                                KEY_OVERLAY_Y,
                                180
                        );

        int oldHeartRate =
                heartRate;

        if (overlayView != null) {

            try {

                windowManager.removeView(
                        overlayView
                );

            } catch (Exception ignored) {
            }
        }

        overlayView = null;
        contentLayout = null;
        overlayBackground = null;
        heartIcon = null;
        heartText = null;
        params = null;

        preferences.edit()
                .putInt(
                        KEY_OVERLAY_X,
                        oldX
                )
                .putInt(
                        KEY_OVERLAY_Y,
                        oldY
                )
                .apply();

        appliedScale = newScale;
        heartRate = oldHeartRate;

        createOverlay();

        if (params != null) {

            params.x = oldX;
            params.y = oldY;
            updateOverlayBackground(newAlpha);
            params.alpha = 1.0f;

            try {

                windowManager.updateViewLayout(
                        overlayView,
                        params
                );

            } catch (Exception ignored) {
            }

            updateLockState();

            refreshHeartRateInternal();
        }

        rebuilding = false;
    }

    private void updateOverlayBackground(float alpha) {
        if (overlayBackground == null) return;
        float safe = clamp(alpha, MIN_ALPHA, MAX_ALPHA);
        // 仅保留圆角底色，不绘制任何边框线；文字与图标保持不透明。
        int fillAlpha = Math.max(36, Math.min(238, Math.round(42f + 196f * safe)));
        overlayBackground.setColor(Color.argb(fillAlpha, 25, 27, 32));
        overlayBackground.setCornerRadius(scaledDp(50, appliedScale));
        if (overlayView != null) overlayView.invalidate();
    }

    private void updateLockState() {

        if (overlayView == null) {
            return;
        }

        boolean locked =
                getOverlayLocked();

        overlayView.setTag(
                locked
                        ? "locked"
                        : "unlocked"
        );
    }

    private boolean getOverlayLocked() {

        return preferences.getBoolean(
                KEY_OVERLAY_LOCKED,
                false
        );
    }

    private float getOverlayScale() {

        float scale =
                preferences.getFloat(
                        KEY_OVERLAY_SIZE,
                        DEFAULT_SCALE
                );

        return clamp(
                scale,
                MIN_SCALE,
                MAX_SCALE
        );
    }

    private float getOverlayAlpha() {

        float alpha =
                preferences.getFloat(
                        KEY_OVERLAY_ALPHA,
                        DEFAULT_ALPHA
                );

        return clamp(
                alpha,
                MIN_ALPHA,
                MAX_ALPHA
        );
    }

    private float clamp(
            float value,
            float min,
            float max
    ) {

        if (value < min) {
            return min;
        }

        if (value > max) {
            return max;
        }

        return value;
    }

    private int scaledDp(
            int value,
            float scale
    ) {

        return Math.max(
                1,
                (int) (
                        dp(value) * scale
                )
        );
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    @Override
    public void onDestroy() {

        mainHandler.removeCallbacksAndMessages(
                null
        );

        if (receiverRegistered) {

            try {

                unregisterReceiver(receiver);

            } catch (Exception ignored) {
            }

            receiverRegistered = false;
        }

        if (overlayView != null &&
                windowManager != null) {

            try {

                windowManager.removeView(
                        overlayView
                );

            } catch (Exception ignored) {
            }
        }

        overlayView = null;
        contentLayout = null;
        overlayBackground = null;
        heartIcon = null;
        heartText = null;
        params = null;

        try {
            if (preferences != null) {
                preferences.edit().putBoolean(HeartControlWidgetProvider.KEY_OVERLAY_READY, false).apply();
            }
            HeartControlWidgetProvider.refreshWidgets(this);
        } catch (Exception ignored) { }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}