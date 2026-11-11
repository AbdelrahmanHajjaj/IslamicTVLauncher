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
import java.util.LinkedHashMap;
import java.util.Collections;
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
            Map<String, String[]> choiceGaps = new LinkedHashMap<>();
            try (InputStream input = test.getContext().getAssets().open("ui-gaps.json");
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024]; int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                JSONArray json = new JSONArray(output.toString("UTF-8"));
                gaps = new String[json.length()];
                for (int i = 0; i < gaps.length; i++) gaps[i] = json.getJSONObject(i).getString("id");
            }
            try (InputStream input = test.getContext().getAssets().open("ui-choice-gaps.json");
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024]; int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                JSONArray json = new JSONArray(output.toString("UTF-8"));
                for (int i = 0; i < json.length(); i++) {
                    JSONArray values = json.getJSONObject(i).getJSONArray("values");
                    String[] keys = new String[values.length()];
                    for (int j = 0; j < keys.length; j++) keys[j] = values.getString(j);
                    choiceGaps.put(json.getJSONObject(i).getString("id"), keys);
                }
            }
            for (String language : new String[] {"ar", "en"}) {
                check(prefs.edit().putString("locale", language).putString("theme", "mihrab").remove("theme_json").commit(), "Set test prerequisites");
                active[0] = (SettingsActivity) test.startActivitySync(new Intent(test.getTargetContext(), SettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                test.waitForIdleSync();
                long deadline = SystemClock.uptimeMillis() + 5000;
                while (!active[0].hasWindowFocus() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(50);
                check(active[0].hasWindowFocus(), "Settings has window focus");
                Set<String> bound = new LinkedHashSet<>();
                Map<String, Set<String>> offered = new LinkedHashMap<>();
                String[] sections = {"prayer", "reminders", "radio", "display", "appearance", "network", "device"};
                for (int section = 0; section < 7; section++) {
                    collect(test, active[0], bound, sections[section], offered);
                    trace.append(language).append(':').append(section).append(' ').append(bound).append('\n');
                    if (section == 0) {
                        // Enter Prayer, reach adjustments and open its real six-stepper editor.
                        key(test, active[0], KeyEvent.KEYCODE_DPAD_CENTER);
                        for (int row = 0; row < 3; row++) key(test, active[0], KeyEvent.KEYCODE_DPAD_DOWN);
                        key(test, active[0], KeyEvent.KEYCODE_DPAD_CENTER);
                        collect(test, active[0], bound, "offsets", offered);
                        key(test, active[0], KeyEvent.KEYCODE_BACK);
                        key(test, active[0], KeyEvent.KEYCODE_BACK);
                    }
                    if (section < 6) key(test, active[0], KeyEvent.KEYCODE_DPAD_DOWN);
                }
                // Rebuild Appearance with a valid imported-theme prerequisite, then visit all position modes.
                check(prefs.edit().putString("theme_json", "{\"version\":1}").commit(), "Set custom-theme prerequisite");
                key(test, active[0], KeyEvent.KEYCODE_DPAD_UP);
                key(test, active[0], KeyEvent.KEYCODE_DPAD_UP);
                key(test, active[0], KeyEvent.KEYCODE_DPAD_CENTER);
                int towardStart = language.equals("ar") ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT;
                int towardEnd = language.equals("ar") ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
                key(test, active[0], towardStart); key(test, active[0], towardStart);
                for (int mode = 0; mode < 3; mode++) {
                    collect(test, active[0], bound, "appearance", offered);
                    if (mode < 2) key(test, active[0], towardEnd);
                }
                key(test, active[0], KeyEvent.KEYCODE_BACK);
                bound.addAll(PrayerUiDeviceCheck.run(test, active[0]));
                UiContract.INSTANCE.validateCoverage(bound.toArray(new String[0]), gaps);
                for (Map.Entry<String, Set<String>> entry : offered.entrySet()) {
                    UiContract.INSTANCE.validateChoiceCoverage(entry.getKey(), entry.getValue().toArray(new String[0]),
                        choiceGaps.getOrDefault(entry.getKey(), new String[0]));
                }
                test.runOnMainSync(() -> active[0].finish());
                test.waitForIdleSync(); active[0] = null;
            }
            result.putString("stream", "Native UI contract passed: Arabic/English settings, six offsets, five prayer switches, room direction save/cancel/return, semantic kinds, canonical labels and full binding/gap coverage.\n");
            code = Activity.RESULT_OK;
        } catch (Throwable error) {
            result.putString("stream", trace + android.util.Log.getStackTraceString(error));
        } finally {
            if (active[0] != null) test.runOnMainSync(() -> active[0].finish());
            SharedPreferences.Editor restore = prefs.edit();
            for (String key : new String[] {"locale", "theme", "theme_json"}) {
                if (saved.containsKey(key)) restore.putString(key, (String) saved.get(key));
                else restore.remove(key);
            }
            if (!restore.commit() || !prefs.getAll().equals(saved)) {
                code = Activity.RESULT_CANCELED;
                result.putString("stream", "UI check did not preserve preferences.\n");
            }
        }
        test.finish(code, result);
    }

    static void key(Instrumentation test, Activity settings, int key) {
        onMain(test, () -> {
            settings.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, key));
            settings.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, key));
        });
        test.waitForIdleSync();
    }

    static void onMain(Instrumentation test, Runnable action) {
        Throwable[] failure = new Throwable[1];
        test.runOnMainSync(() -> { try { action.run(); } catch (Throwable error) { failure[0] = error; } });
        if (failure[0] != null) throw new AssertionError("Native UI interaction", failure[0]);
    }

    private static void collect(Instrumentation test, SettingsActivity settings, Set<String> all, String section, Map<String, Set<String>> offered) {
        Throwable[] failure = new Throwable[1];
        test.runOnMainSync(() -> {
            try {
                Set<String> found = new LinkedHashSet<>();
                walk(settings.root, settings.t, found, section, offered);
                check(!found.isEmpty(), "Actual section has bound controls");
                all.addAll(found);
            } catch (Throwable error) { failure[0] = error; }
        });
        if (failure[0] != null) throw new AssertionError("Rendered native contract", failure[0]);
    }

    private static void walk(View view, Text text, Set<String> found, String section, Map<String, Set<String>> offered) throws Exception {
        if (view.getTag() instanceof SettingsActivity.Row) {
            SettingsActivity.Row row = (SettingsActivity.Row) view.getTag();
            UiControl control = row.contract;
            check(found.add(control.getId()), "Duplicate real binding: " + control.getId());
            check(control.getSection().equals(section), "Wrong control section: " + control.getId());
            check(view.isFocusable(), "Contract row must be focusable: " + control.getId());
            if (control.getKind().equals("choice")) {
                Collections.addAll(offered.computeIfAbsent(control.getId(), id -> new LinkedHashSet<>()), row.keys());
            }
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
            for (int i = 0; i < group.getChildCount(); i++) walk(group.getChildAt(i), text, found, section, offered);
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
