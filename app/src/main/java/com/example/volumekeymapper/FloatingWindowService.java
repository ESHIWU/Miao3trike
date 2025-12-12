package com.ark3trike.matches;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FloatingWindowService extends Service {

    private static final String TAG = "Miao3trikeFloat";
    private static final String CHANNEL_ID = "miao3trike_foreground";

    private WindowManager windowManager;
    private View floatingView;
    private ImageView floatingButton;
    private WindowManager.LayoutParams params;

    private static volatile boolean isRunning = false;
    private static FloatingWindowService instance;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        isRunning = true;

        createNotificationChannelIfNeeded();
        startForeground(1, buildForegroundNotification());

        if (!Settings.canDrawOverlays(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            stopSelf();
            return;
        }

        createFloatingWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (windowManager != null && floatingView != null) {
                windowManager.removeView(floatingView);
            }
        } catch (Exception e) {
            Log.e(TAG, "removeView error", e);
        }
        floatingView = null;
        floatingButton = null;
        windowManager = null;
        params = null;

        isRunning = false;
        if (instance == this) {
            instance = null;
        }
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Miao3trike 前台服务",
                            NotificationManager.IMPORTANCE_MIN
                    );
            channel.setDescription("保持 Miao3trike 功能运行");
            channel.setShowBadge(false);
            channel.setSound(null, null);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Miao3trike 正在运行")
                        .setContentText("点击进入应用可调整设置")
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_MIN);

        return builder.build();
    }

    private void createFloatingWindow() {
        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            if (windowManager == null) return;

            LayoutInflater inflater = LayoutInflater.from(this);
            floatingView = inflater.inflate(R.layout.floating_button, null);
            floatingButton = floatingView.findViewById(R.id.floating_button);

            int layoutFlag;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
            }

            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );

            // 修改点1：改为左上角对齐，这是实现自由拖拽的关键
            params.gravity = Gravity.TOP | Gravity.START;

            // 修改点2：手动计算初始位置（放在屏幕右侧）
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            params.x = metrics.widthPixels - dp2px(80); // 屏幕宽度减去一些边距，初始靠右
            params.y = metrics.heightPixels / 4;        // 初始高度在屏幕 1/4 处

            setupTouchAndClick();
            updateButtonAppearance(VolumeKeyAccessibilityService.isFunctionEnabled());

            windowManager.addView(floatingView, params);
        } catch (Exception e) {
            Log.e(TAG, "createFloatingWindow error", e);
            stopSelf();
        }
    }

    private void setupTouchAndClick() {
        if (floatingView == null) return;

        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private long touchStartTime;
            private static final int CLICK_DURATION = 200;
            private static final int CLICK_DRIFT = 10;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (params == null || windowManager == null) return false;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchStartTime = System.currentTimeMillis();
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // 修改点3：标准的拖拽计算公式
                        // 因为是 Gravity.TOP | Gravity.START，x/y 就是绝对坐标
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        long clickDuration = System.currentTimeMillis() - touchStartTime;
                        float dx = Math.abs(event.getRawX() - initialTouchX);
                        float dy = Math.abs(event.getRawY() - initialTouchY);

                        if (clickDuration < CLICK_DURATION && dx < CLICK_DRIFT && dy < CLICK_DRIFT) {
                            toggleFunction();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void toggleFunction() {
        try {
            boolean current = VolumeKeyAccessibilityService.isFunctionEnabled();
            boolean newState = !current;
            VolumeKeyAccessibilityService.setFunctionEnabled(newState);
            updateButtonAppearance(newState);

            // 静默执行，无 Toast
        } catch (Exception e) {
            Log.e(TAG, "toggleFunction error", e);
        }
    }

    private void updateButtonAppearance(boolean enabled) {
        if (floatingButton == null) return;

        floatingButton.clearColorFilter();
        floatingButton.setBackground(null);

        if (enabled) {
            floatingButton.setImageResource(R.drawable.ic_float_on);
        } else {
            floatingButton.setImageResource(R.drawable.ic_float_off);
        }

        floatingButton.setAlpha(0.7f);
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    public static boolean isRunning() {
        return isRunning && instance != null;
    }

    public static void stopService(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, FloatingWindowService.class);
        context.stopService(intent);
    }
}