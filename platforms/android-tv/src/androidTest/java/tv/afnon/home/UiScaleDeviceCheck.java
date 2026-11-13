package tv.afnon.home;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

/** Real Android view/window sizing; media stays disabled and all original preferences are restored. */
final class UiScaleDeviceCheck {
    static void run(Instrumentation test) {
        SharedPreferences prefs = Prefs.of(test.getTargetContext());
        Map<String, ?> original = prefs.getAll();
        Activity[] active = new Activity[1];
        Bundle result = new Bundle(); int code = Activity.RESULT_CANCELED;
        try {
            test.setInTouchMode(false);
            prefs.edit().remove("ui_scale").commit();
            check(Prefs.uiScale(test.getTargetContext()) == 100, "Missing size keeps the original UI");
            prefs.edit().putString("ui_scale", "90").commit();
            check(Prefs.uiScale(test.getTargetContext()) == 100, "Type-confused size uses the safe default");
            for (int invalid : new int[] {0, 85, 111, 155, 160}) {
                prefs.edit().putInt("ui_scale", invalid).commit();
                check(Prefs.uiScale(test.getTargetContext()) == 100, "Unknown saved size uses the default");
                boolean rejected = false;
                try { Prefs.setUiScale(test.getTargetContext(), invalid); } catch (IllegalArgumentException expected) { rejected = true; }
                check(rejected, "Invalid size must not be saved");
            }
            for (String language : new String[] {"en", "ar"}) for (int size : new int[] {150, 80, 90, 100, 110, 120, 130, 140}) {
                prefs.edit().putString("locale", language).putString("clock", "12").putString("digits", "")
                    .putInt("ui_scale", 100).putInt("appearance_cycle", 0).putBoolean("reduce_motion", false)
                    .putBoolean("adhan", false).putBoolean("announce", false).putBoolean("remind_voice", false).putInt("remind", 0).commit();
                SettingsActivity settings = launch(test, SettingsActivity.class); active[0] = settings;
                for (int i = 0; i < 3; i++) key(test, settings, KeyEvent.KEYCODE_DPAD_DOWN);
                key(test, settings, KeyEvent.KEYCODE_DPAD_CENTER);
                View bound = binding(settings.root, "display.size");
                check(bound != null, "Actual display.size binding exists");
                for (int i = 0; i < 12 && settings.getCurrentFocus() != bound; i++) key(test, settings, KeyEvent.KEYCODE_DPAD_DOWN);
                check(settings.getCurrentFocus() == bound, "D-pad reaches the actual global size row");
                Map<String, ?> before = prefs.getAll();
                key(test, settings, KeyEvent.KEYCODE_DPAD_CENTER);
                key(test, settings, language.equals("ar") ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
                key(test, settings, KeyEvent.KEYCODE_BACK);
                check(prefs.getAll().equals(before), "Back cancels the size draft");
                check(settings.getCurrentFocus() == binding(settings.root, "display.size"), "Cancel retains the size row");
                key(test, settings, KeyEvent.KEYCODE_DPAD_CENTER);
                int delta = size / 10 - 10;
                for (int i = 0; i < Math.abs(delta); i++) key(test, settings,
                    (delta > 0) != language.equals("ar") ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT);
                key(test, settings, KeyEvent.KEYCODE_DPAD_CENTER);
                SystemClock.sleep(200); test.waitForIdleSync();
                Map<String, Object> expected = new HashMap<>(before); expected.put("ui_scale", size);
                check(prefs.getAll().equals(expected), "Only global size is saved");
                check(settings.getCurrentFocus() == binding(settings.root, "display.size"), "Saving restores the new size row");
                scene(test, settings, language, size);
                key(test, settings, KeyEvent.KEYCODE_BACK);
                for (int i = 0; i < 3; i++) key(test, settings, KeyEvent.KEYCODE_DPAD_UP);
                for (int section = 0; section < 7; section++) {
                    key(test, settings, KeyEvent.KEYCODE_DPAD_CENTER);
                    if (section == 0) {
                        key(test, settings, KeyEvent.KEYCODE_DPAD_CENTER);
                        modal(test, settings, "chooser"); key(test, settings, KeyEvent.KEYCODE_BACK);
                    }
                    int rows = ((java.util.List<?>) UiContractDeviceCheck.field(settings, "rows")).size();
                    for (int i = 1; i < rows; i++) key(test, settings, KeyEvent.KEYCODE_DPAD_DOWN);
                    SystemClock.sleep(200); test.waitForIdleSync();
                    UiContractDeviceCheck.onMain(test, () -> pageBounds(settings, size));
                    key(test, settings, KeyEvent.KEYCODE_BACK);
                    if (section < 6) key(test, settings, KeyEvent.KEYCODE_DPAD_DOWN);
                }
                finish(test, settings); active[0] = null;
                for (Class<? extends Screen> type : Arrays.asList(MainActivity.class, AllAppsActivity.class, PrayerDayActivity.class, NowActivity.class, OnlinePrayerActivity.class)) {
                    Screen screen = launch(test, type); active[0] = screen;
                    if (screen instanceof AllAppsActivity) {
                        long deadline = SystemClock.uptimeMillis() + 7000;
                        while (((java.util.List<?>) UiContractDeviceCheck.field(screen, "tiles")).isEmpty() && SystemClock.uptimeMillis() < deadline) {
                            SystemClock.sleep(50); test.waitForIdleSync();
                        }
                        UiContractDeviceCheck.onMain(test, () -> {
                            java.util.List<?> tiles = (java.util.List<?>) UiContractDeviceCheck.field(screen, "tiles");
                            check(!tiles.isEmpty() && tiles.get(0) != null, "Actual installed-app row is loaded");
                            ((View) tiles.get(0)).requestFocus();
                        });
                    }
                    if (screen instanceof OnlinePrayerActivity) UiContractDeviceCheck.onMain(test, () -> {
                        OnlinePrayerActivity online = (OnlinePrayerActivity) screen;
                        OnlinePrayer old = (OnlinePrayer) UiContractDeviceCheck.field(online, "request");
                        if (old != null) old.cancel();
                        OnlinePrayer source = OnlinePrayerDeviceCheck.request();
                        OnlinePrayerDeviceCheck.set(online, "request", source);
                        try { OnlinePrayerDeviceCheck.deliver(online, source, source.parse(OnlinePrayerDeviceCheck.fixture().toString())); }
                        catch (Exception error) { throw new AssertionError(error); }
                    });
                    SystemClock.sleep(200); test.waitForIdleSync();
                    scene(test, screen, language, size);
                    if (screen instanceof PrayerDayActivity) {
                        UiContractDeviceCheck.onMain(test, () -> ((View) UiContractDeviceCheck.field(screen, "roomButton")).requestFocus());
                        key(test, screen, KeyEvent.KEYCODE_DPAD_CENTER); modal(test, screen, "picker"); key(test, screen, KeyEvent.KEYCODE_BACK);
                    }
                    if (screen instanceof AllAppsActivity) {
                        View focused = screen.getCurrentFocus();
                        UiContractDeviceCheck.onMain(test, () -> {
                            long now = SystemClock.uptimeMillis();
                            screen.dispatchKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, 0));
                            screen.dispatchKeyEvent(KeyEvent.changeFlags(new KeyEvent(now, now + 600, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, 1), KeyEvent.FLAG_LONG_PRESS));
                            screen.dispatchKeyEvent(new KeyEvent(now, now + 610, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER, 0));
                        });
                        modal(test, screen, "menu"); key(test, screen, KeyEvent.KEYCODE_BACK);
                        check(screen.getCurrentFocus() == focused, "App menu returns to its origin");
                    }
                    finish(test, screen); active[0] = null;
                    if (type == MainActivity.class && size == 150) {
                        Map<String, ?> beforeHome = prefs.getAll();
                        for (int[] variant : new int[][] {{0,1,0},{0,2,0},{1,0,0},{1,0,1},{2,0,0}}) {
                            prefs.edit().putBoolean("quick_labels", true).putInt("home_countdown", variant[0])
                                .putInt("home_side", variant[1]).putInt("home_bar", variant[2]).putString("clock", "12").commit();
                            MainActivity sample = launch(test, MainActivity.class); active[0] = sample;
                            SystemClock.sleep(200); test.waitForIdleSync(); scene(test, sample, language, size);
                            finish(test, sample); active[0] = null;
                        }
                        restore(prefs, beforeHome);
                    }
                }
                check(Prefs.uiScale(test.getTargetContext()) == size, "Size survives activity reopening");
                Bundle progress = new Bundle(); progress.putString("stream", "PASS: native pages " + language + " " + size + "% save/cancel/focus/geometry.\n"); test.sendStatus(0, progress);
            }
            result.putString("stream", "Native global-size pages passed, including settings preservation and real view transforms.\n"); code = Activity.RESULT_OK;
        } catch (Throwable error) { result.putString("stream", android.util.Log.getStackTraceString(error)); }
        finally {
            if (active[0] != null) test.runOnMainSync(active[0]::finish);
            restore(prefs, original);
            if (!prefs.getAll().equals(original)) { code = Activity.RESULT_CANCELED; result.putString("stream", "Preference restoration failed"); }
        }
        test.finish(code, result);
    }

    private static <T extends Activity> T launch(Instrumentation test, Class<T> type) {
        T activity = type.cast(test.startActivitySync(new Intent(test.getTargetContext(), type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        test.waitForIdleSync(); UiContractDeviceCheck.waitFocus(activity); return activity;
    }
    private static void finish(Instrumentation test, Activity activity) { test.runOnMainSync(activity::finish); test.waitForIdleSync(); }
    private static void key(Instrumentation test, Activity activity, int key) { UiContractDeviceCheck.key(test, activity, key); }

    private static View binding(View view, String id) {
        if (view.getTag() instanceof SettingsActivity.Row && id.equals(((SettingsActivity.Row) view.getTag()).contract.getId())) return view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            View found = binding(((ViewGroup) view).getChildAt(i), id); if (found != null) return found;
        }
        return null;
    }

    private static void scene(Instrumentation test, Screen screen, String language, int size) throws Exception {
        capture(test, language, size, screen.getClass().getSimpleName());
        UiContractDeviceCheck.onMain(test, () -> {
            pageBounds(screen, size);
        });
        if (screen instanceof MainActivity && size > 110) {
            View initial = screen.getCurrentFocus();
            View[] nodes = (View[]) UiContractDeviceCheck.field(screen, "nodes");
            key(test, screen, KeyEvent.KEYCODE_DPAD_UP);
            check(Arrays.asList(FavouriteChoice.QIBLA, FavouriteChoice.WIFI, FavouriteChoice.SETTINGS, FavouriteChoice.RADIO, FavouriteChoice.INPUTS)
                .contains(Arrays.asList(nodes).indexOf(screen.getCurrentFocus())), "Large Home Up reaches top controls");
            SystemClock.sleep(200); test.waitForIdleSync();
            UiContractDeviceCheck.onMain(test, () -> pageBounds(screen, size));
            capture(test, language, size, "home-header");
            key(test, screen, KeyEvent.KEYCODE_DPAD_DOWN);
            check(screen.getCurrentFocus() == initial, "Large Home returns to its app");
            for (int i = 0; i < 20 && screen.getCurrentFocus() != nodes[FavouriteChoice.ALL]; i++) key(test, screen, KeyEvent.KEYCODE_DPAD_DOWN);
            check(screen.getCurrentFocus() == nodes[FavouriteChoice.ALL], "Large Home Down reaches All apps");
            UiContractDeviceCheck.onMain(test, () -> pageBounds(screen, size));
        }
    }

    private static void capture(Instrumentation test, String language, int size, String name) throws Exception {
        if (size != 90) {
            Bitmap image = test.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).takeScreenshot();
            check(image != null, "Screenshot is available");
            try (FileOutputStream output = new FileOutputStream(test.getTargetContext().getCacheDir() + "/size-native-" + language + "-" + size + "-" + name + ".png")) {
                image.compress(Bitmap.CompressFormat.PNG, 100, output);
            } finally { image.recycle(); }
        }
    }

    private static void pageBounds(Screen screen, int size) {
        check(Math.abs(screen.root.getScaleX() - size / 100f) < 0.001, "Per-screen scale matches the shared preference");
        View background = size == 100 ? screen.root : (View) screen.root.getParent();
        check(background.getBackground() != null && background.getBackground().getCallback() == background, "Unscaled background retains its viewport owner");
        int[] at = new int[2]; background.getLocationOnScreen(at);
        bounds(screen.root, false, new Rect(at[0], at[1], at[0] + background.getWidth(), at[1] + background.getHeight()));
        if (screen instanceof MainActivity && size > 110) {
            View[] nodes = (View[]) UiContractDeviceCheck.field(screen, "nodes");
            ViewGroup controls = (ViewGroup) nodes[FavouriteChoice.QIBLA].getParent();
            ViewGroup header = (ViewGroup) controls.getParent();
            View metadata = header.getChildAt(0);
            if (Prefs.quickLabels(screen)) for (int id : new int[] {FavouriteChoice.QIBLA, FavouriteChoice.INPUTS, FavouriteChoice.RADIO}) {
                ViewGroup words = (ViewGroup) ((ViewGroup) nodes[id]).getChildAt(0);
                android.widget.TextView label = (android.widget.TextView) words.getChildAt(1);
                check(label.getBottom() <= words.getHeight() - words.getPaddingBottom(), "Canonical toolbar labels fit vertically");
                for (int line = 0; line < label.getLineCount(); line++) check(label.getLayout().getEllipsisCount(line) == 0, "Canonical toolbar labels remain complete");
            }
            check(!Rect.intersects(new Rect(metadata.getLeft(), metadata.getTop(), metadata.getRight(), metadata.getBottom()),
                new Rect(controls.getLeft(), controls.getTop(), controls.getRight(), controls.getBottom())), "Home toolbar uses the space beside the clock");
            ViewGroup content = (ViewGroup) header.getParent();
            check(content.getChildAt(1).getTop() > header.getBottom(), "Home body clears its measured header");
        }
        if (screen instanceof MainActivity) {
            for (String field : new String[] {"cellName", "cellTime"}) for (android.widget.TextView label : (android.widget.TextView[]) UiContractDeviceCheck.field(screen, field)) {
                if (label == null) continue;
                check(label.getLayout() != null && label.getLayout().getEllipsisCount(0) == 0, "Large Home prayer labels and times must fit");
                check(label.getLayout().getLineWidth(0) <= label.getWidth() + 1, "Home prayer text must fit: " + label.getText() + " width " + label.getLayout().getLineWidth(0) + "/" + label.getWidth());
            }
        }
        if (screen instanceof SettingsActivity && size > 110) {
            View pane = (View) UiContractDeviceCheck.field(screen, "rowScroll");
            Rect content = new Rect();
            check(pane != null && pane.getGlobalVisibleRect(content), "Large Settings rows are visible");
            Rect previous = null;
            for (View nav : (View[]) UiContractDeviceCheck.field(screen, "nav")) {
                Rect section = new Rect();
                check(nav.getGlobalVisibleRect(section), "Large Settings navigation remains visible");
                // FocusTile reserves REACH around its painted card; adjacent hit bounds may overlap.
                int reach = Math.round(Ui.px(FocusTile.REACH) * size / 100f);
                section.inset(reach, reach);
                check(!Rect.intersects(content, section), "Large Settings columns must not overlap");
                check(previous == null || !Rect.intersects(previous, section), "Large Settings navigation rows must not overlap");
                previous = section;
            }
        }
    }

    private static void modal(Instrumentation test, Screen screen, String field) {
        SystemClock.sleep(200); test.waitForIdleSync();
        UiContractDeviceCheck.onMain(test, () -> {
            View overlay = (View) UiContractDeviceCheck.field(screen, field);
            check(overlay != null && overlay.isShown(), "Actual modal is open: " + field);
            View viewport = Prefs.uiScale(screen) == 100 ? screen.root : (View) screen.root.getParent();
            int[] at = new int[2], edge = new int[2]; overlay.getLocationOnScreen(at); viewport.getLocationOnScreen(edge);
            float factor = Ui.factor(screen);
            check(Math.abs(at[0] - edge[0]) <= 1 && Math.abs(at[1] - edge[1]) <= 1
                && Math.abs(overlay.getWidth() * factor - viewport.getWidth()) <= 1
                && Math.abs(overlay.getHeight() * factor - viewport.getHeight()) <= 1, "Modal backdrop covers the viewport: " + field);
            pageBounds(screen, Prefs.uiScale(screen));
        });
    }

    static void bounds(View view, boolean scroll, Rect viewport) {
        if (!view.isShown() || view.getAlpha() == 0) return;
        scroll |= view instanceof ScrollView;
        if (view instanceof FocusTile && (!scroll || view.isFocused())) {
            float sx = 1, sy = 1;
            for (View node = view; node != null; node = node.getParent() instanceof View ? (View) node.getParent() : null) { sx *= node.getScaleX(); sy *= node.getScaleY(); }
            int[] at = new int[2]; view.getLocationOnScreen(at);
            Rect visible = new Rect();
            check(view.getGlobalVisibleRect(visible) && at[0] >= viewport.left - 1 && at[1] >= viewport.top - 1
                && at[0] + view.getWidth() * sx <= viewport.right + 1
                && at[1] + view.getHeight() * sy <= viewport.bottom + 1,
                "Control clips: " + (view.getTag() instanceof SettingsActivity.Row ? ((SettingsActivity.Row) view.getTag()).contract.getId() : view.getContentDescription()) + " at " + at[0] + "," + at[1] + " size " + view.getWidth() * sx + "x" + view.getHeight() * sy + " viewport " + viewport + " visible " + visible + " parents " + parents(view));
        }
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) bounds(((ViewGroup) view).getChildAt(i), scroll, viewport);
    }

    private static String parents(View view) {
        StringBuilder out = new StringBuilder();
        for (View node = view; node != null; node = node.getParent() instanceof View ? (View) node.getParent() : null)
            out.append(node.getClass().getSimpleName()).append('[').append(node.getTop()).append(',').append(node.getHeight()).append(',').append(node.getScrollY()).append(']');
        return out.toString();
    }

    @SuppressWarnings("unchecked")
    static void restore(SharedPreferences prefs, Map<String, ?> saved) {
        SharedPreferences.Editor edit = prefs.edit().clear();
        for (Map.Entry<String, ?> entry : saved.entrySet()) {
            String key = entry.getKey(); Object value = entry.getValue();
            if (value instanceof String) edit.putString(key, (String) value);
            else if (value instanceof Boolean) edit.putBoolean(key, (Boolean) value);
            else if (value instanceof Integer) edit.putInt(key, (Integer) value);
            else if (value instanceof Long) edit.putLong(key, (Long) value);
            else if (value instanceof Float) edit.putFloat(key, (Float) value);
            else if (value instanceof Set) edit.putStringSet(key, new HashSet<>((Set<String>) value));
        }
        check(edit.commit(), "Restore original preferences");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
