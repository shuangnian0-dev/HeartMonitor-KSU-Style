package com.shuangnian;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.LinearGradient;
import android.graphics.Color;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Single 2x3-style heart-rate widget, matched to the app theme (including light mode). */
public class HeartControlWidgetProvider extends AppWidgetProvider {
    public static final String PREFS = "app_preferences";
    public static final String ACTION_TOGGLE = "com.shuangnian.widget.TOGGLE_MASTER";
    public static final String ACTION_CONTROL = "com.shuangnian.WIDGET_MASTER_CONTROL";
    public static final String KEY_WIDGET_MASTER = "widget_master_enabled";
    public static final String KEY_WIDGET_PENDING_START = "widget_pending_start";
    public static final String KEY_WIDGET_CONNECTED = "widget_connected";
    public static final String KEY_OVERLAY_READY = "overlay_view_ready";
    public static final String KEY_LAST_HEART_AT = "monitor_last_hr_at";
    public static final String KEY_LAST_HEART = "monitor_last_hr";
    public static final String KEY_HEART_HISTORY = "monitor_hr_history_widget";
    public static final String KEY_WIDGET_COLOR_MODE = "widget_color_mode";
    public static final String KEY_MONITOR = "monitor";
    public static final String KEY_OVERLAY = "overlay";

    private static final int HISTORY_LIMIT = 32;
    private static final int GRAPH_WIDTH = 320;
    private static final int GRAPH_HEIGHT = 72;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent != null && Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
            refreshWidgets(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateWidget(context, manager, id);
    }

    @Override
    public void onEnabled(Context context) { refreshWidgets(context); }

    public static void refreshWidgets(Context context) {
        if (context == null) return;
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName component = new ComponentName(context, HeartControlWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(component);
            for (int id : ids) updateWidget(context, manager, id);
        } catch (Exception ignored) {
            // Widget refresh must never interfere with heart-rate collection.
        }
    }

    /** Persist each real sample so both widget rendering and the app share one recent history. */
    public static void recordHeartRate(Context context, int heartRate) {
        if (context == null || heartRate <= 0 || heartRate > 250) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        List<Integer> history = parseHistory(prefs.getString(KEY_HEART_HISTORY, ""));
        history.add(heartRate);
        while (history.size() > HISTORY_LIMIT) history.remove(0);
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            if (i > 0) encoded.append(',');
            encoded.append(history.get(i));
        }
        prefs.edit()
                .putInt(KEY_LAST_HEART, heartRate)
                .putLong(KEY_LAST_HEART_AT, System.currentTimeMillis())
                .putString(KEY_HEART_HISTORY, encoded.toString())
                .apply();
        refreshWidgets(context);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int id) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.heart_control_widget);
        String widgetMode = prefs.getString(KEY_WIDGET_COLOR_MODE, "follow");
        boolean darkTheme = "black".equals(widgetMode)
                || (!"white".equals(widgetMode) && isDarkTheme(context, prefs));
        applyWidgetTheme(views, darkTheme);
        boolean monitoring = prefs.getBoolean(KEY_MONITOR, false);
        boolean overlay = prefs.getBoolean(KEY_OVERLAY, false);
        boolean overlayReady = prefs.getBoolean(KEY_OVERLAY_READY, false);
        boolean active = monitoring || overlay;
        boolean connected = prefs.getBoolean(KEY_WIDGET_CONNECTED, false);
        int heartRate = prefs.getInt(KEY_LAST_HEART, 0);
        long timestamp = prefs.getLong(KEY_LAST_HEART_AT, 0L);

        views.setTextViewText(R.id.widget_power, active ? "⏻  一键关闭" : "⏻  一键开启");
        int powerBackground = active ? R.drawable.widget_power_on
                : (darkTheme ? R.drawable.widget_power_off_black : R.drawable.widget_power_off_light);
        views.setInt(R.id.widget_power, "setBackgroundResource", powerBackground);
        views.setTextColor(R.id.widget_power, active ? 0xFF05242D
                : (darkTheme ? 0xFFF2FBFF : 0xFF254158));

        String monitorText = monitoring ? "监测开" : "监测关";
        String overlayText = overlayReady ? "悬浮窗已显示" : (overlay ? "悬浮窗启动中" : "悬浮窗关闭");
        String deviceText = connected ? "● 设备已连接" : (active ? "◷ 等待设备连接" : "○ 设备未连接");
        views.setTextViewText(R.id.widget_status, monitorText + " · " + overlayText + "\n" + deviceText);
        views.setTextColor(R.id.widget_status, connected
                ? (darkTheme ? 0xFF28D8C9 : 0xFF087F78)
                : (darkTheme ? 0xFF8EB3C9 : 0xFF617789));

        views.setTextViewText(R.id.widget_hr, heartRate > 0 ? String.valueOf(heartRate) : "--");
        String lastTime = timestamp > 0
                ? new SimpleDateFormat("HH:mm · MM-dd", Locale.getDefault()).format(new Date(timestamp))
                : "等待真实数据";
        views.setTextViewText(R.id.widget_last_time, lastTime);
        // Keep the original colored illustration and accent curve in both black and white modes.
        views.setImageViewResource(R.id.widget_mascot, R.drawable.widget_mascot);
        views.setImageViewBitmap(R.id.widget_trend, createTrendBitmap(
                parseHistory(prefs.getString(KEY_HEART_HISTORY, "")), darkTheme));

        Intent action = new Intent(context, WidgetActionReceiver.class).setAction(ACTION_TOGGLE);
        PendingIntent pending = PendingIntent.getBroadcast(context, 48320, action,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_power, pending);
        manager.updateAppWidget(id, views);
    }

    /** Follow the theme selected inside the app; "system" follows Android's light/dark mode. */
    private static boolean isDarkTheme(Context context, SharedPreferences prefs) {
        String theme = prefs.getString("theme", "system");
        if ("dark".equals(theme) || "amoled".equals(theme) || "midnight".equals(theme)) return true;
        if ("light".equals(theme)) return false;
        int night = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return night == Configuration.UI_MODE_NIGHT_YES;
    }

    private static void applyWidgetTheme(RemoteViews views, boolean dark) {
        if (dark) {
            views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_card_bg_black);
            views.setInt(R.id.widget_hr_panel, "setBackgroundResource", R.drawable.widget_hr_bg_black);
            views.setTextColor(R.id.widget_title, 0xFFF1F7FC);
            views.setTextColor(R.id.widget_brand, 0xFF66869A);
            views.setTextColor(R.id.widget_status, 0xFF8EB3C9);
            views.setTextColor(R.id.widget_heart_icon, 0xFF21DDD4);
            views.setTextColor(R.id.widget_hr, 0xFFDDF5FF);
            views.setTextColor(R.id.widget_bpm_label, 0xFF23D7CE);
            views.setTextColor(R.id.widget_last_label, 0xFF7DA7C0);
            views.setTextColor(R.id.widget_last_time, 0xFF6F94AE);
        } else {
            views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_card_bg_white);
            views.setInt(R.id.widget_hr_panel, "setBackgroundResource", R.drawable.widget_hr_bg_white);
            views.setTextColor(R.id.widget_title, 0xFF172B3D);
            views.setTextColor(R.id.widget_brand, 0xFFA4B8C8);
            views.setTextColor(R.id.widget_status, 0xFF617789);
            views.setTextColor(R.id.widget_heart_icon, 0xFF008F91);
            views.setTextColor(R.id.widget_hr, 0xFF19364A);
            views.setTextColor(R.id.widget_bpm_label, 0xFF008F91);
            views.setTextColor(R.id.widget_last_label, 0xFF486B81);
            views.setTextColor(R.id.widget_last_time, 0xFF627F94);
        }
    }

    private static List<Integer> parseHistory(String raw) {
        ArrayList<Integer> values = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return values;
        String[] parts = raw.split(",");
        int from = Math.max(0, parts.length - HISTORY_LIMIT);
        for (int i = from; i < parts.length; i++) {
            try {
                int value = Integer.parseInt(parts[i].trim());
                if (value > 0 && value <= 250) values.add(value);
            } catch (Exception ignored) { }
        }
        return values;
    }

    /** Draw a restrained sparkline. Every pixel is clipped to the graph's safe inset. */
    private static Bitmap createTrendBitmap(List<Integer> values, boolean dark) {
        Bitmap bitmap = Bitmap.createBitmap(GRAPH_WIDTH, GRAPH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float left = 10f, right = GRAPH_WIDTH - 10f, top = 8f, bottom = GRAPH_HEIGHT - 9f;
        int save = canvas.save();
        canvas.clipRect(left, top, right, bottom);

        // One quiet baseline only. No glow or filled area, so the trend stays visually inside its card.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.1f);
        paint.setColor(dark ? 0x443F778B : 0x55748FA4);
        float baseline = bottom - (bottom - top) * 0.18f;
        canvas.drawLine(left, baseline, right, baseline, paint);

        if (values == null || values.isEmpty()) {
            paint.setColor(dark ? 0xFF477589 : 0xFF9AAEBE);
            paint.setStrokeWidth(1.8f);
            canvas.drawLine(left, (top + bottom) * 0.5f, right, (top + bottom) * 0.5f, paint);
            canvas.restoreToCount(save);
            return bitmap;
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        if (max - min < 10) {
            int center = (max + min) / 2;
            min = Math.max(25, center - 5);
            max = Math.min(240, center + 5);
        } else {
            int pad = Math.max(2, (max - min) / 8);
            min = Math.max(25, min - pad);
            max = Math.min(240, max + pad);
        }
        float range = Math.max(1f, max - min);
        Path line = new Path();
        float lastX = right;
        float lastY = (top + bottom) * 0.5f;
        for (int i = 0; i < values.size(); i++) {
            float x = values.size() == 1 ? right : left + (right - left) * i / (values.size() - 1f);
            float normalized = Math.max(0f, Math.min(1f, (values.get(i) - min) / range));
            float y = bottom - normalized * (bottom - top);
            y = Math.max(top + 4f, Math.min(bottom - 4f, y));
            if (i == 0) line.moveTo(x, y); else line.lineTo(x, y);
            lastX = x;
            lastY = y;
        }

        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        paint.setColor(dark ? 0xFF27D9D0 : 0xFF008F91);
        canvas.drawPath(line, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(dark ? 0xFFB4FFFA : 0xFF176A71);
        canvas.drawCircle(lastX, lastY, 2.7f, paint);
        canvas.restoreToCount(save);
        return bitmap;
    }
}
