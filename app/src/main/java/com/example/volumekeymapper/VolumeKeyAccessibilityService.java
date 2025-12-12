package com.ark3trike.matches;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class VolumeKeyAccessibilityService extends AccessibilityService {

    private static final String TAG = "VolumeKeyService";
    private static VolumeKeyAccessibilityService instance;
    private static boolean functionEnabled = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 不需要处理
    }

    @Override
    public void onInterrupt() {
        // 不需要处理
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP &&
                event.getAction() == KeyEvent.ACTION_DOWN &&
                functionEnabled) {

            performDoubleBack();
            return true; // 拦截事件
        }
        return super.onKeyEvent(event);
    }

    private void performDoubleBack() {
        // 第一次返回
        performGlobalAction(GLOBAL_ACTION_BACK);

        // 修改点：将延迟从 100ms 增加到 300ms
        // 这样可以避免操作过快导致第二次返回无效
        handler.postDelayed(() -> {
            performGlobalAction(GLOBAL_ACTION_BACK);
        }, 300);
    }

    public static void setFunctionEnabled(boolean enabled) {
        functionEnabled = enabled;
    }

    public static boolean isFunctionEnabled() {
        return functionEnabled;
    }

    public static VolumeKeyAccessibilityService getInstance() {
        return instance;
    }
}