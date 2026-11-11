package tv.afnon.home;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Map;

/** Device-only deadline tests, with no test dependency or shortcut in the production app. */
public final class AppearanceCycleDeviceCheck extends Instrumentation {
    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); start(); }

    @Override public void onStart() {
        SharedPreferences prefs = Prefs.of(getTargetContext());
        Map<String, ?> saved = prefs.getAll();
        Bundle result = new Bundle();
        int code = Activity.RESULT_CANCELED;
        try {
            prefs.edit().putBoolean("quick_labels", true).commit();
            MainActivity home = (MainActivity) startActivitySync(new Intent(getTargetContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            waitForIdleSync();
            long until = SystemClock.uptimeMillis() + 5000;
            while (!home.hasWindowFocus() && SystemClock.uptimeMillis() < until) SystemClock.sleep(50);
            check(home.hasWindowFocus(), "Home needs window focus");
            onMain(() -> {
                String theme = prefs.getString("theme", "mihrab");
                String background = Appearance.style(home, theme, prefs.getString("background", "theme"), -1).mode;
                String focus = label(home.getCurrentFocus());
                String wifi = wifiText(home.root);
                check(!wifi.isEmpty(), "Wi-Fi label is populated before rotation");
                prefs.edit().putInt("appearance_cycle", 15).putLong("appearance_cycle_at", System.currentTimeMillis() - 1).commit();
                home.onTick();
                check(!theme.equals(prefs.getString("theme", "mihrab")), "Due rotation changes theme");
                check(!background.equals(prefs.getString("background", "theme")), "Due rotation changes background");
                check(focus.equals(label(home.getCurrentFocus())), "Rotation preserves focus");
                check(wifi.equals(wifiText(home.root)), "Wi-Fi label survives rotation");
                String rotated = prefs.getString("theme", "mihrab");
                home.onTick();
                check(rotated.equals(prefs.getString("theme", "mihrab")), "No repeated rotation on next tick");
            });
            Activity settings = startActivitySync(new Intent(getTargetContext(), SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            waitForIdleSync();
            final String theme = prefs.getString("theme", "mihrab");
            onMain(() -> {
                prefs.edit().putInt("appearance_cycle", 30).putLong("appearance_cycle_at", System.currentTimeMillis() - 1).commit();
                home.onTick();
                check(theme.equals(prefs.getString("theme", "mihrab")), "Settings must defer rotation");
                settings.finish();
            });
            until = SystemClock.uptimeMillis() + 5000;
            while (theme.equals(prefs.getString("theme", "mihrab")) && SystemClock.uptimeMillis() < until) SystemClock.sleep(50);
            check(!theme.equals(prefs.getString("theme", "mihrab")), "Returning Home runs one overdue rotation");
            long remaining = prefs.getLong("appearance_cycle_at", 0) - System.currentTimeMillis();
            check(remaining > 29 * 60_000L && remaining <= 30 * 60_000L, "30-minute interval rearmed");
            onMain(() -> {
                String current = prefs.getString("theme", "mihrab");
                prefs.edit().remove("appearance_cycle").putLong("appearance_cycle_at", 1).commit();
                home.onTick();
                check(current.equals(prefs.getString("theme", "mihrab")), "Off retains current appearance");
            });
            result.putString("stream", "Appearance rotation passed: due tick, focus, Wi-Fi label, no repeat, Settings deferral, Home resume, 30-minute rearm, Off.\n");
            code = Activity.RESULT_OK;
        } catch (Throwable error) {
            result.putString("stream", android.util.Log.getStackTraceString(error));
        } finally {
            SharedPreferences.Editor restore = prefs.edit();
            for (String key : new String[]{"theme", "background", "appearance_cycle", "appearance_cycle_at", "quick_labels"}) {
                Object value = saved.get(key);
                if (value instanceof Boolean) restore.putBoolean(key, (Boolean)value);
                else if (value instanceof String) restore.putString(key, (String)value);
                else if (value instanceof Integer) restore.putInt(key, (Integer)value);
                else if (value instanceof Long) restore.putLong(key, (Long)value);
                else restore.remove(key);
            }
            if (!restore.commit()) { code = Activity.RESULT_CANCELED; result.putString("stream", "Preference restoration failed"); }
        }
        finish(code, result);
    }

    private void onMain(Runnable action) {
        Throwable[] failure = new Throwable[1];
        runOnMainSync(() -> { try { action.run(); } catch (Throwable error) { failure[0] = error; } });
        if (failure[0] != null) throw new AssertionError("Device check failed", failure[0]);
    }

    private static String wifiText(View view) {
        if ("Wi-Fi".contentEquals(view.getContentDescription() == null ? "" : view.getContentDescription())) {
            TextView label = firstText(view);
            return label == null ? "" : label.getText().toString();
        }
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
            String text = wifiText(((ViewGroup)view).getChildAt(i));
            if (!text.isEmpty()) return text;
        }
        return "";
    }
    private static TextView firstText(View view) {
        if (view instanceof TextView) return (TextView)view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
            TextView text = firstText(((ViewGroup)view).getChildAt(i));
            if (text != null) return text;
        }
        return null;
    }
    private static String label(View view) {
        if (view instanceof TextView) return ((TextView)view).getText().toString();
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
            String label = label(((ViewGroup)view).getChildAt(i));
            if (!label.isEmpty()) return label;
        }
        return view == null || view.getContentDescription() == null ? "" : view.getContentDescription().toString();
    }
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
