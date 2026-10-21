package tv.afnon.home;

import android.accessibilityservice.AccessibilityService;
import android.content.ActivityNotFoundException;
import android.content.Intent;
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
 */
public class RemoteKeys extends AccessibilityService {

    /** Key name (TCL names resolve on this firmware only) or keycode -> the app it opens. */
    private static final String[][] APP_KEYS = {
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

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        appKeys.clear();
        for (String[] k : APP_KEYS) {
            int code = KeyEvent.keyCodeFromString(k[0]);
            if (code > 0) appKeys.put(code, k[1]);
        }
        // The radio may have left the TV's sleep timer off (a crash, a reboot mid-listen): put it back.
        Radio.restoreSleep(this);
        if (watch != null) watch.stop();
        watch = new PrayerWatch(this);
        watch.start();
    }

    @Override
    public void onDestroy() {
        if (watch != null) watch.stop();
        watch = null;
        super.onDestroy();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        String app = appKeys.get(code);
        Intent open;
        if (app != null) open = null;
        // Afnon Settings, which lists the TV's own under Device settings.
        else if (code == SETTINGS_KEY || code == KeyEvent.KEYCODE_SETTINGS) open = new Intent(this, SettingsActivity.class);
        else if (code == KeyEvent.KEYCODE_TV || code == KeyEvent.KEYCODE_TV_INPUT) open = new Intent(ACTION_VIEW_INPUTS);
        else if (code == KeyEvent.KEYCODE_ALL_APPS) open = new Intent(this, AllAppsActivity.class);
        else return false;
        // Act on release only, and swallow the press too so the app underneath never sees half a key.
        if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
            if (app != null) {
                Apps.launch(this, app);
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

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
