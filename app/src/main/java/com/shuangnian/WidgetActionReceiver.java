package com.shuangnian;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

/** Handles widget taps directly; deliberately does not launch MainActivity. */
public class WidgetActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !HeartControlWidgetProvider.ACTION_TOGGLE.equals(intent.getAction())) return;

        SharedPreferences prefs = context.getSharedPreferences(HeartControlWidgetProvider.PREFS, Context.MODE_PRIVATE);
        boolean currentlyActive = prefs.getBoolean(HeartControlWidgetProvider.KEY_MONITOR, false)
                || prefs.getBoolean(HeartControlWidgetProvider.KEY_OVERLAY, false);
        boolean enabled = !currentlyActive;
        boolean canOverlay = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context);

        SharedPreferences.Editor editor = prefs.edit()
                .putBoolean(HeartControlWidgetProvider.KEY_WIDGET_MASTER, enabled)
                .putBoolean(HeartControlWidgetProvider.KEY_WIDGET_PENDING_START, enabled)
                .putBoolean(HeartControlWidgetProvider.KEY_MONITOR, enabled);
        if (enabled) editor.putBoolean(HeartControlWidgetProvider.KEY_OVERLAY_READY, false);

        if (enabled) {
            // The widget may start the floating window only when system permission already exists.
            editor.putBoolean(HeartControlWidgetProvider.KEY_OVERLAY, canOverlay);
        } else {
            editor.putBoolean(HeartControlWidgetProvider.KEY_OVERLAY, false)
                    .putBoolean(HeartControlWidgetProvider.KEY_OVERLAY_READY, false)
                    .putBoolean(HeartControlWidgetProvider.KEY_WIDGET_CONNECTED, false);
        }
        editor.apply();

        Intent command = new Intent(HeartControlWidgetProvider.ACTION_CONTROL)
                .setPackage(context.getPackageName())
                .putExtra("enabled", enabled);
        context.sendBroadcast(command);

        if (enabled) {
            Intent monitor = new Intent(context, HeartMonitoringService.class)
                    .setAction(HeartMonitoringService.ACTION_START);
            try {
                if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(monitor);
                else context.startService(monitor);
            } catch (Exception ignored) {
                // MainActivity can still handle the control command while it remains alive.
            }

            if (canOverlay) {
                Intent overlay = new Intent(context, HeartOverlayService.class);
                overlay.putExtra("heart_rate", prefs.getInt(HeartControlWidgetProvider.KEY_LAST_HEART, 0));
                try {
                    if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(overlay);
                    else context.startService(overlay);
                } catch (Exception ignored) { }
            } else {
                Toast.makeText(context, "悬浮窗未启动：尚未授予系统悬浮窗权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            try { context.stopService(new Intent(context, HeartMonitoringService.class)); } catch (Exception ignored) { }
            try { context.stopService(new Intent(context, HeartOverlayService.class)); } catch (Exception ignored) { }
        }

        // Refresh now for immediate feedback, then again after Android has had time to
        // create/attach the overlay view. This keeps the widget from remaining at a stale state.
        HeartControlWidgetProvider.refreshWidgets(context);
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> HeartControlWidgetProvider.refreshWidgets(context.getApplicationContext()), 250L);
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> HeartControlWidgetProvider.refreshWidgets(context.getApplicationContext()), 900L);
    }
}
