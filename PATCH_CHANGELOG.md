# Patch Changelog

This patch was auto-generated to improve Android 13/14+ compatibility and clean up manifest.

## Changes
1. **AndroidManifest.xml**
   - Removed top-level `<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE"/>` (signature-level; should not be declared at top-level). The service declaration remains with `android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"`.
   - Affected files:
     - /mnt/data/workdir/VolumeKeyMapper_patched/app/src/main/AndroidManifest.xml

2. **Accessibility Services**
   - Ensured `FLAG_REQUEST_FILTER_KEY_EVENTS` is set in `onServiceConnected()` so the service reliably receives `onKeyEvent()` callbacks.
   - Affected files:
     - (no AccessibilityService subclass found or already compliant)

3. **MainActivity notification permission (Android 13+)**
   - If a `MainActivity` was found, added a runtime request for `POST_NOTIFICATIONS` so the foreground service's notification can appear properly on Android 13+.
   - Affected files:
     - /mnt/data/workdir/VolumeKeyMapper_patched/app/src/main/java/com/example/volumekeymapper/MainActivity.java

## Notes
- No Gradle configuration was changed.
- No removal of `WRITE_SETTINGS` or other sensitive permissions was performed automatically.
- Please rebuild with your usual Android Gradle Plugin/SDK configuration.
