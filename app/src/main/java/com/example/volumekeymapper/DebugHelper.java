package com.ark3trike.matches;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

/**
 * 调试辅助类，用于记录系统信息和权限状态
 */
public class DebugHelper {

    private static final String TAG = "DebugHelper";

    /**
     * 记录系统信息
     */
    public static void logSystemInfo(Context context) {
        Log.d(TAG, "=== SYSTEM INFO ===");
        Log.d(TAG, "Android Version: " + Build.VERSION.RELEASE);
        Log.d(TAG, "API Level: " + Build.VERSION.SDK_INT);
        Log.d(TAG, "Device: " + Build.MANUFACTURER + " " + Build.MODEL);
        Log.d(TAG, "Package: " + context.getPackageName());

        try {
            String versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            Log.d(TAG, "App Version: " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Could not get app version", e);
        }

        Log.d(TAG, "=== END SYSTEM INFO ===");
    }

    /**
     * 记录权限状态
     */
    public static void logPermissionStatus(Context context) {
        Log.d(TAG, "=== PERMISSION STATUS ===");

        // 检查悬浮窗权限
        boolean canDrawOverlays = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canDrawOverlays = Settings.canDrawOverlays(context);
        }
        Log.d(TAG, "Overlay Permission: " + (canDrawOverlays ? "GRANTED" : "DENIED"));

        // 检查无障碍服务
        boolean accessibilityEnabled = isAccessibilityServiceEnabled(context);
        Log.d(TAG, "Accessibility Service: " + (accessibilityEnabled ? "ENABLED" : "DISABLED"));

        Log.d(TAG, "=== END PERMISSION STATUS ===");
    }

    /**
     * 记录内存信息
     */
    public static void logMemoryInfo() {
        Log.d(TAG, "=== MEMORY INFO ===");

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        Log.d(TAG, "Max Memory: " + formatBytes(maxMemory));
        Log.d(TAG, "Total Memory: " + formatBytes(totalMemory));
        Log.d(TAG, "Used Memory: " + formatBytes(usedMemory));
        Log.d(TAG, "Free Memory: " + formatBytes(freeMemory));

        Log.d(TAG, "=== END MEMORY INFO ===");
    }

    /**
     * 检查无障碍服务是否启用
     */
    private static boolean isAccessibilityServiceEnabled(Context context) {
        try {
            String settingValue = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (settingValue != null) {
                String serviceName = context.getPackageName() + "/" +
                        context.getPackageName() + ".VolumeKeyAccessibilityService";
                return settingValue.contains(serviceName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking accessibility service", e);
        }

        return false;
    }

    /**
     * 格式化字节数
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * 记录服务启动信息
     */
    public static void logServiceStart(String serviceName) {
        Log.d(TAG, "=== SERVICE START: " + serviceName + " ===");
        Log.d(TAG, "Thread: " + Thread.currentThread().getName());
        Log.d(TAG, "Time: " + System.currentTimeMillis());
    }

    /**
     * 记录服务停止信息
     */
    public static void logServiceStop(String serviceName) {
        Log.d(TAG, "=== SERVICE STOP: " + serviceName + " ===");
        Log.d(TAG, "Time: " + System.currentTimeMillis());
    }
}