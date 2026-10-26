package tv.afnon.home;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Remote buttons that only the stock launcher answered, from any app: the app buttons, Settings, Input and All
 * apps. Every other key passes through untouched. Taken over from the cleanyourtv launcher's InputKeyService and
 * HomeKeyService so that package can be disabled. Keys injected over ADB never reach this; test with the remote.
 * Being always on, it also hosts {@link PrayerWatch}: the reminders and the call to prayer.
 *
 * On TCL firmwares the buttons are handed to TCL's own key remapping ({@link TclKeys}) and this service stops
 * filtering keys: filtered keys lose their auto-repeat, so holding Volume moved it only one step. Firmwares that
 * answer the buttons themselves (CVTE's hotkey service, see {@link FirmwareKeys}) are left alone too. Elsewhere it
 * filters keys and opens the targets itself, as below; a button whose target is missing passes through.
 */
public class RemoteKeys extends AccessibilityService {

    /**
     * Key name (TCL names resolve on this firmware only) or keycode -> the app it opens. The default; the phone
     * installer replaces it through {@link SetupProvider} (pref "keys": "KEY=package,KEY=package").
     */
    static final String[][] APP_KEYS = {
        {"KEYCODE_BUTTON_3", "com.google.android.youtube.tv"},
        {"KEYCODE_TCL_YOUTUBE", "com.google.android.youtube.tv"},
        {"KEYCODE_TCL_NETFLIX", "net.mbc.shahidTV"},
        {"KEYCODE_INFO", "dev.ctv.tv"},
        // The Web button: TCL's own keycode with no Android name, sent over IR (scancode 651).
        {"4159", "com.tcl.browser"},
    };
    /** TCL remotes send Settings as 83 (KEYCODE_NOTIFICATION), which the stock home turned into its panel. */
    private static final int SETTINGS_KEY = KeyEvent.KEYCODE_NOTIFICATION;
    private static final String ACTION_VIEW_INPUTS = "com.android.tv.action.VIEW_INPUTS";

    private final Map<Integer, String> appKeys = new HashMap<>();
    /** The running watch, for Home's test hook; null while the service is off. */
    static PrayerWatch watch;
    /** The running service, so a new button table applies at once; null while the service is off. */
    private static RemoteKeys running;
    private PrayerDock dock;

    static boolean toggleDock() { if (running == null || running.dock == null) return false; running.dock.toggle(); return true; }
    static boolean showInputs() { if (running == null || running.dock == null) return false; running.dock.inputs(); return true; }
    static void hideDock() { if (running != null && running.dock != null) running.dock.hide(); }
    static void tickDock(java.util.Date now, boolean asleep) { if (running != null && running.dock != null) running.dock.update(now, asleep); }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        running = this;
        if (dock != null) dock.stop();
        dock = new PrayerDock(this);
        ServiceRepair.connected(this);
        applyKeys();
        // The radio may have left the TV's sleep timer off (a crash, a reboot mid-listen): put it back.
        Radio.restoreSleep(this);
        if (watch != null) watch.stop();
        watch = new PrayerWatch(this);
        watch.start();
    }

    /** The button table: the saved one, else {@link #APP_KEYS}. */
    static String[][] keys(android.content.Context c) {
        String saved = Prefs.of(c).getString("keys", null);
        if (saved == null) return APP_KEYS;
        java.util.List<String[]> out = new java.util.ArrayList<>();
        for (String pair : saved.split(",")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && eq < pair.length() - 1) out.add(new String[] {pair.substring(0, eq).trim(), pair.substring(eq + 1).trim()});
        }
        return out.toArray(new String[0][]);
    }

    /** A key's code from its name ("KEYCODE_TCL_NETFLIX" or "TCL_NETFLIX") or its number ("4159"); 0 if unknown. */
    static int code(String name) {
        if (name.matches("\\d+")) {
            try { return Integer.parseInt(name); } catch (NumberFormatException ignored) { return 0; }
        }
        int code = KeyEvent.keyCodeFromString(name.startsWith("KEYCODE_") ? name : "KEYCODE_" + name);
        return Math.max(code, 0);
    }

    /** Re-reads the button table and hands it to the firmware again. Main thread. */
    static void reload() {
        if (running != null) running.applyKeys();
    }

    private void applyKeys() {
        appKeys.clear();
        if (!Prefs.of(this).getBoolean("remote_enabled", true)) {
            TclKeys.restore(this);
            filterKeys(false);
            return;
        }
        for (String[] k : keys(this)) {
            int code = code(k[0]);
            if (allowedCode(code)) appKeys.put(code, k[1]);
        }
        boolean tcl = TclKeys.install(this, targets());
        filterKeys(!tcl && FirmwareKeys.shouldFilter(this));
    }

    @Override public boolean onUnbind(Intent intent) {
        ServiceRepair.disconnected(this);
        return super.onUnbind(intent);
    }

    static Intent target(android.content.Context c, String value) {
        switch (value) {
            case "@dock": return new Intent(c, DockActivity.class);
            case "@home": return new Intent(c, MainActivity.class);
            case "@apps": return new Intent(c, AllAppsActivity.class);
            case "@inputs": return new Intent(c, InputsActivity.class);
            case "@settings": return new Intent(c, SettingsActivity.class);
            case "@default": return null;
            default: return Apps.launchIntent(c, value);
        }
    }

    static String defaultAction(int code) {
        if (code == SETTINGS_KEY || code == KeyEvent.KEYCODE_SETTINGS) return "@settings";
        if (code == KeyEvent.KEYCODE_TV || code == KeyEvent.KEYCODE_TV_INPUT) return "@inputs";
        if (code == KeyEvent.KEYCODE_ALL_APPS) return "@apps";
        return "@default";
    }

    static boolean allowedCode(int code) {
        return code > 0 && code < 100000 && code != KeyEvent.KEYCODE_HOME && code != KeyEvent.KEYCODE_POWER
            && code != KeyEvent.KEYCODE_SLEEP && code != KeyEvent.KEYCODE_WAKEUP && code != KeyEvent.KEYCODE_TV_POWER
            && code != KeyEvent.KEYCODE_STB_POWER && code != KeyEvent.KEYCODE_AVR_POWER
            && code != KeyEvent.KEYCODE_BACK && code != KeyEvent.KEYCODE_ESCAPE
            && code != KeyEvent.KEYCODE_ENTER && code != KeyEvent.KEYCODE_NUMPAD_ENTER
            && code != KeyEvent.KEYCODE_DPAD_UP_LEFT && code != KeyEvent.KEYCODE_DPAD_UP_RIGHT
            && code != KeyEvent.KEYCODE_DPAD_DOWN_LEFT && code != KeyEvent.KEYCODE_DPAD_DOWN_RIGHT
            && code != KeyEvent.KEYCODE_VOLUME_UP && code != KeyEvent.KEYCODE_VOLUME_DOWN
            && code != KeyEvent.KEYCODE_VOLUME_MUTE && !(code >= KeyEvent.KEYCODE_DPAD_UP && code <= KeyEvent.KEYCODE_DPAD_CENTER);
    }

    static void validate(String value) {
        if (value.length() > 8192) throw new IllegalArgumentException("Too many button mappings");
        for (String pair : value.split(",")) {
            if (pair.isEmpty()) continue;
            String[] parts = pair.split("=", -1);
            if (parts.length != 2 || !allowedCode(code(parts[0])) ||
                    !(parts[1].matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+")
                    || java.util.Arrays.asList("@home", "@apps", "@inputs", "@settings", "@dock", "@default").contains(parts[1])))
                throw new IllegalArgumentException("Invalid button mapping");
        }
    }

    @Override
    public void onDestroy() {
        if (dock != null) { dock.stop(); dock = null; }
        if (running == this) running = null;
        if (watch != null) watch.stop();
        watch = null;
        super.onDestroy();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.isFromSource(android.view.InputDevice.SOURCE_GAMEPAD)
                || event.isFromSource(android.view.InputDevice.SOURCE_JOYSTICK)) return false;
        if (RemoteButtonsActivity.captureKey(event)) return true;
        int code = event.getKeyCode();
        String app = appKeys.get(code);
        Intent open;
        if (app != null) open = null;
        // Afnon Settings, which lists the TV's own under Device settings.
        else if (code == SETTINGS_KEY || code == KeyEvent.KEYCODE_SETTINGS) open = new Intent(this, SettingsActivity.class);
        else if (code == KeyEvent.KEYCODE_TV || code == KeyEvent.KEYCODE_TV_INPUT) open = inputs();
        else if (code == KeyEvent.KEYCODE_ALL_APPS) open = new Intent(this, AllAppsActivity.class);
        else return false;
        // Nothing here answers it (the app is gone, or the firmware has no Android TV inputs screen): let the key
        // through so the firmware's own handling (a CVTE source menu, say) still gets it.
        if (app != null ? target(this, app) == null : open == null || explicit(new Intent(open)) == null) return false;
        // Act on release only, and swallow the press too so the app underneath never sees half a key.
        if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
            if ("@dock".equals(app)) { toggleDock(); return true; }
            if ("@inputs".equals(app) || (app == null && (code == KeyEvent.KEYCODE_TV || code == KeyEvent.KEYCODE_TV_INPUT))) {
                if (showInputs()) return true;
            }
            if (app != null) {
                try { startActivity(target(this, app).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); Apps.remember(this, app); }
                catch (RuntimeException ignored) { }
            } else {
                try {
                    startActivity(open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (ActivityNotFoundException ignored) {
                    // No input switcher or settings on this build; the key just does nothing.
                }
            }
        }
        return true;
    }

    /** Every button this service answers, with what it opens; buttons whose target is missing are left out. */
    private Map<Integer, Intent> targets() {
        Map<Integer, Intent> out = new HashMap<>();
        for (Map.Entry<Integer, String> e : appKeys.entrySet()) {
            Intent i = target(this, e.getValue());
            if (i != null) out.put(e.getKey(), i);
        }
        Intent settings = new Intent(this, SettingsActivity.class);
        if (!appKeys.containsKey(SETTINGS_KEY)) out.put(SETTINGS_KEY, settings);
        if (!appKeys.containsKey(KeyEvent.KEYCODE_SETTINGS)) out.put(KeyEvent.KEYCODE_SETTINGS, settings);
        Intent inputs = inputs();
        if (inputs != null) {
            if (!appKeys.containsKey(KeyEvent.KEYCODE_TV)) out.put(KeyEvent.KEYCODE_TV, inputs);
            if (!appKeys.containsKey(KeyEvent.KEYCODE_TV_INPUT)) out.put(KeyEvent.KEYCODE_TV_INPUT, inputs);
        }
        if (android.os.Build.VERSION.SDK_INT >= 28 && !appKeys.containsKey(KeyEvent.KEYCODE_ALL_APPS)) out.put(KeyEvent.KEYCODE_ALL_APPS, new Intent(this, AllAppsActivity.class));
        for (Intent i : out.values()) i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return out;
    }

    /** The Android TV inputs screen, else the firmware's own source menu; null if neither exists. */
    private Intent inputs() {
        return new Intent(this, InputsActivity.class);
    }

    /** The intent pinned to the activity that answers it now, or null if none does. */
    private Intent explicit(Intent i) {
        ResolveInfo r = getPackageManager().resolveActivity(i, 0);
        if (r == null || r.activityInfo == null) return null;
        ActivityInfo a = r.activityInfo;
        return i.setComponent(new ComponentName(a.packageName, a.name));
    }

    private void filterKeys(boolean on) {
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) return;
        int flag = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        info.flags = on ? info.flags | flag : info.flags & ~flag;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (dock != null) dock.onWindow(event);
        Redirects.onWindowChanged(this, event);
    }

    @Override
    public void onInterrupt() {
    }
}
