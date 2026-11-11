package tv.afnon.home;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import tv.afnon.core.UiContract;
import tv.afnon.core.UiControl;

/** Enumerates real native settings rows; the gap file alone cannot claim a working binding. */
final class UiContractDeviceCheck {
    static void run(Instrumentation test) {
        SharedPreferences prefs = Prefs.of(test.getTargetContext());
        Map<String, ?> saved = prefs.getAll();
        Bundle result = new Bundle();
        int code = Activity.RESULT_CANCELED;
        SettingsActivity[] active = new SettingsActivity[1];
        StringBuilder trace = new StringBuilder();
        try {
            String[] gaps;
            try (InputStream input = test.getContext().getAssets().open("ui-gaps.json");
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024]; int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                JSONArray json = new JSONArray(output.toString("UTF-8"));
                gaps = new String[json.length()];
                for (int i = 0; i < gaps.length; i++) gaps[i] = json.getJSONObject(i).getString("id");
            }
            for (String language : new String[] {"ar", "en"}) {
                check(prefs.edit().putString("locale", language).commit(), "Set test language");
                active[0] = (SettingsActivity) test.startActivitySync(new Intent(test.getTargetContext(), SettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                test.waitForIdleSync();
                long deadline = SystemClock.uptimeMillis() + 5000;
                while (!active[0].hasWindowFocus() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(50);
                check(active[0].hasWindowFocus(), "Settings has window focus");
                Set<String> bound = new LinkedHashSet<>();
                String[] sections = {"prayer", "reminders", "radio", "display", "appearance", "network", "device"};
                for (int section = 0; section < 7; section++) {
                    collect(test, active[0], bound, sections[section]);
                    trace.append(language).append(':').append(section).append(' ').append(bound).append('\n');
                    if (section == 0) {
                        // Enter Prayer, reach adjustments and open its real six-stepper editor.
                        key(test, active[0], KeyEvent.KEYCODE_DPAD_CENTER);
                        for (int row = 0; row < 3; row++) key(test, active[0], KeyEvent.KEYCODE_DPAD_DOWN);
                        key(test, active[0], KeyEvent.KEYCODE_DPAD_CENTER);
                        collect(test, active[0], bound, "offsets");
                        key(test, active[0], KeyEvent.KEYCODE_BACK);
                        key(test, active[0], KeyEvent.KEYCODE_BACK);
                    }
                    if (section < 6) key(test, active[0], KeyEvent.KEYCODE_DPAD_DOWN);
                }
                UiContract.INSTANCE.validateCoverage(bound.toArray(new String[0]), gaps);
                test.runOnMainSync(() -> active[0].finish());
                test.waitForIdleSync(); active[0] = null;
            }
            result.putString("stream", "Native UI contract passed: Arabic/English real settings rows, six offset steppers, semantic kinds, canonical labels and full binding/gap coverage.\n");
            code = Activity.RESULT_OK;
        } catch (Throwable error) {
            result.putString("stream", trace + android.util.Log.getStackTraceString(error));
        } finally {
            if (active[0] != null) test.runOnMainSync(() -> active[0].finish());
            SharedPreferences.Editor restore = prefs.edit();
            if (saved.containsKey("locale")) restore.putString("locale", (String) saved.get("locale"));
            else restore.remove("locale");
            if (!restore.commit() || !prefs.getAll().equals(saved)) {
                code = Activity.RESULT_CANCELED;
                result.putString("stream", "UI check did not preserve preferences.\n");
            }
        }
        test.finish(code, result);
    }

    private static void key(Instrumentation test, Activity settings, int key) {
        test.runOnMainSync(() -> {
            settings.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, key));
            settings.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, key));
        });
        test.waitForIdleSync();
    }

    private static void collect(Instrumentation test, SettingsActivity settings, Set<String> all, String section) {
        Throwable[] failure = new Throwable[1];
        test.runOnMainSync(() -> {
            try {
                Set<String> found = new LinkedHashSet<>();
                walk(settings.root, settings.t, found, section);
                check(!found.isEmpty(), "Actual section has bound controls");
                all.addAll(found);
            } catch (Throwable error) { failure[0] = error; }
        });
        if (failure[0] != null) throw new AssertionError("Rendered native contract", failure[0]);
    }

    private static void walk(View view, Text text, Set<String> found, String section) throws Exception {
        if (view.getTag() instanceof UiControl) {
            UiControl control = (UiControl) view.getTag();
            check(found.add(control.getId()), "Duplicate real binding: " + control.getId());
            check(control.getSection().equals(section), "Wrong control section: " + control.getId());
            check(view.isFocusable(), "Contract row must be focusable: " + control.getId());
            if (!control.getId().equals("radio.playback")) {
                String[] key = control.getLabelKey().split(":");
                java.lang.reflect.Method label = Text.class.getDeclaredMethod(key[0]);
                label.setAccessible(true);
                Object value = label.invoke(text);
                String expected = key.length == 1 ? (String) value : ((String[]) value)[Integer.parseInt(key[1])];
                check(expected.equals(firstText(view)), "Canonical label: " + control.getId());
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) walk(group.getChildAt(i), text, found, section);
        }
    }

    private static String firstText(View view) {
        // PrayerIcon's replacement span occupies one object-replacement character before the label.
        if (view instanceof TextView) return ((TextView) view).getText().toString().replace("\uFFFC", "");
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                String value = firstText(group.getChildAt(i));
                if (value != null) return value;
            }
        }
        return null;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
