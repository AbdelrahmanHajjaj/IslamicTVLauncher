package tv.afnon.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;
import java.util.*;

/** Assign a physical shortcut button to an installed app or a launcher action. */
public final class RemoteButtonsActivity extends Activity {
    private final LinkedHashMap<Integer, String> mappings = new LinkedHashMap<>();
    private static RemoteButtonsActivity capturing;
    private AlertDialog captureDialog;
    private ButtonPress press = new ButtonPress();
    private Text t;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        t = Screen.loadText(this);
        for (String[] item : RemoteKeys.keys(this)) {
            int code = RemoteKeys.code(item[0]);
            if (RemoteKeys.allowedCode(code)) mappings.put(code, item[1]);
        }
        for (int code : new int[]{KeyEvent.KEYCODE_NOTIFICATION, KeyEvent.KEYCODE_SETTINGS,
                KeyEvent.KEYCODE_TV_INPUT, KeyEvent.KEYCODE_TV, KeyEvent.KEYCODE_ALL_APPS})
            mappings.putIfAbsent(code, RemoteKeys.defaultAction(code));
        if (getPackageManager().hasSystemFeature("com.tcl.software.tinput_shortcut_config"))
            for (int code : new int[]{4062, 4065, 4159}) mappings.putIfAbsent(code, "@default");
        showKeys();
    }

    private void showKeys() {
        List<Integer> keys = new ArrayList<>(mappings.keySet());
        List<String> labels = new ArrayList<>();
        labels.add(t.pressButton());
        boolean enabled = Prefs.of(this).getBoolean("remote_enabled", true);
        for (int key : keys) labels.add(buttonName(key) + " · " + label(enabled ? mappings.get(key) : "@default"));
        labels.add(t.restoreButtons());
        if (RemoteKeys.watch == null) labels.add(t.enableButtons());
        show(new AlertDialog.Builder(this).setTitle(t.remoteButtons()).setItems(labels.toArray(new String[0]), (d, index) -> {
            if (index == 0) listen();
            else if (index <= keys.size()) choose(keys.get(index - 1));
            else if (index == keys.size() + 1) {
                LinkedHashMap<Integer, String> originals = new LinkedHashMap<>();
                for (int key : keys) originals.put(key, "@default");
                if (!save(originals, false)) { showKeys(); return; }
                if (!TclKeys.restore(this)) Toast.makeText(this, t.buttonsFailed(), Toast.LENGTH_LONG).show();
                showKeys();
            } else { SystemScreens.open(this, android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS); showKeys(); }
        }).setNegativeButton(t.back(), (d, w) -> finish()).setOnCancelListener(d -> finish()).create());
    }

    private String buttonName(int code) {
        switch (code) {
            case KeyEvent.KEYCODE_NOTIFICATION: return t.buttonNames()[0];
            case KeyEvent.KEYCODE_SETTINGS: return t.buttonNames()[1];
            case KeyEvent.KEYCODE_TV_INPUT: return t.inputsTitle();
            case KeyEvent.KEYCODE_TV: return t.buttonNames()[2];
            case KeyEvent.KEYCODE_ALL_APPS: return t.buttonActions()[2];
            case KeyEvent.KEYCODE_INFO: return t.buttonNames()[3];
            default:
                if (getPackageManager().hasSystemFeature("com.tcl.software.tinput_shortcut_config")) {
                    if (code == 4062) return "Netflix";
                    if (code == 4065) return "YouTube";
                    if (code == 4159) return t.buttonNames()[4];
                }
                String name = KeyEvent.keyCodeToString(code);
                return name.startsWith("KEYCODE_") ? name.substring(8).replace('_', ' ') : t.buttonNumber(code);
        }
    }

    private String label(String value) {
        String[] values = {"@default", "@home", "@apps", "@inputs", "@settings"};
        for (int i = 0; i < values.length; i++) if (values[i].equals(value)) return t.buttonActions()[i];
        return Apps.name(this, t, value);
    }

    private void choose(int key) {
        Apps.invalidate();
        List<String> values = new ArrayList<>(Arrays.asList("@default", "@home", "@apps", "@inputs", "@settings"));
        values.addAll(Apps.sorted(this, t));
        String current = Prefs.of(this).getBoolean("remote_enabled", true) ? mappings.getOrDefault(key, "@default") : "@default";
        String[] labels = new String[values.size()];
        for (int i = 0; i < labels.length; i++) labels[i] = label(values.get(i)) + (values.get(i).equals(current) ? " ✓" : "");
        show(new AlertDialog.Builder(this).setTitle(buttonName(key)).setItems(labels, (d, index) -> {
            String value = values.get(index);
            if (!value.startsWith("@") && Apps.launchIntent(this, value) == null) {
                Toast.makeText(this, t.buttonAppMissing(), Toast.LENGTH_LONG).show(); choose(key); return;
            }
            LinkedHashMap<Integer, String> next = new LinkedHashMap<>(mappings);
            if (!Prefs.of(this).getBoolean("remote_enabled", true)) next.replaceAll((code, target) -> "@default");
            next.put(key, value);
            if (save(next, true)) Toast.makeText(this, t.saved(), Toast.LENGTH_SHORT).show();
            showKeys();
        }).setNegativeButton(t.back(), (d, w) -> showKeys()).setOnCancelListener(d -> showKeys()).create());
    }

    private boolean save(LinkedHashMap<Integer, String> next, boolean enabled) {
        List<String> table = new ArrayList<>();
        for (Map.Entry<Integer, String> item : next.entrySet()) table.add(item.getKey() + "=" + item.getValue());
        String value = android.text.TextUtils.join(",", table);
        try { RemoteKeys.validate(value); }
        catch (IllegalArgumentException e) { Toast.makeText(this, t.buttonsFailed(), Toast.LENGTH_LONG).show(); return false; }
        if (!Prefs.of(this).edit().putString("keys", value).putBoolean("remote_enabled", enabled).commit()) {
            Toast.makeText(this, t.buttonsFailed(), Toast.LENGTH_LONG).show(); return false;
        }
        mappings.clear(); mappings.putAll(next);
        RemoteKeys.reload();
        return true;
    }

    private void show(AlertDialog dialog) {
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().getDecorView().setLayoutDirection(
            t.ar ? android.view.View.LAYOUT_DIRECTION_RTL : android.view.View.LAYOUT_DIRECTION_LTR);
    }

    private void listen() {
        press = new ButtonPress();
        captureDialog = new AlertDialog.Builder(this).setTitle(t.pressButton()).setMessage(t.pressButtonHelp())
            .setNegativeButton(t.back(), (d, w) -> showKeys()).setOnCancelListener(d -> showKeys()).create();
        captureDialog.setOnKeyListener((d, key, event) -> captureKey(event));
        captureDialog.setOnDismissListener(d -> { if (capturing == this) capturing = null; captureDialog = null; });
        show(captureDialog);
        capturing = this;
    }

    /** The service sees keys before dialogs; route a captured press here instead of launching its old app. */
    static boolean captureKey(KeyEvent event) {
        RemoteButtonsActivity a = capturing;
        if (a == null || !RemoteKeys.allowedCode(event.getKeyCode())
                || event.isFromSource(android.view.InputDevice.SOURCE_GAMEPAD)
                || event.isFromSource(android.view.InputDevice.SOURCE_JOYSTICK)) return false;
        int code = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN) return a.press.down(code, event.getRepeatCount());
        if (event.getAction() != KeyEvent.ACTION_UP || !a.press.holds(code)) return false;
        int selected = a.press.up(code, event.isCanceled());
        if (selected != 0) { a.captureDialog.dismiss(); a.choose(selected); }
        return true;
    }

    @Override protected void onResume() {
        super.onResume();
        if (captureDialog != null && captureDialog.isShowing()) { press = new ButtonPress(); capturing = this; }
    }
    @Override protected void onPause() { if (capturing == this) capturing = null; super.onPause(); }
    @Override protected void onDestroy() {
        if (capturing == this) capturing = null;
        if (captureDialog != null) captureDialog.dismiss();
        super.onDestroy();
    }
}
