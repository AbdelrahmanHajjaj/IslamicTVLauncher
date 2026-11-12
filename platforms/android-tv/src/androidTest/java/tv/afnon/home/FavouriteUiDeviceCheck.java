package tv.afnon.home;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import tv.afnon.core.UiControl;
import static tv.afnon.home.UiContractDeviceCheck.field;
import static tv.afnon.home.UiContractDeviceCheck.key;
import static tv.afnon.home.UiContractDeviceCheck.onMain;
import static tv.afnon.home.UiContractDeviceCheck.waitFocus;

/** Exercises actual arrange menus and replacement mode without launching or uninstalling an app. */
final class FavouriteUiDeviceCheck {
    static Set<String> run(Instrumentation test) {
        SharedPreferences prefs = Prefs.of(test.getTargetContext());
        Map<String, ?> before = prefs.getAll();
        Set<String> bound = new LinkedHashSet<>();
        AllAppsActivity[] apps = new AllAppsActivity[1];
        MainActivity[] home = new MainActivity[1];
        String fixture = test.getContext().getPackageName();
        Apps.invalidate();
        List<String> installed = Apps.installed(test.getTargetContext());
        check(installed.contains(fixture) && Apps.canUninstall(test.getTargetContext(), fixture), "Launchable removable test fixture is installed");
        List<String> original = new ArrayList<>(installed);
        original.remove(fixture);
        check(original.size() >= 4, "Four real apps are needed for the minimum favourite slots");
        original = new ArrayList<>(original.subList(0, 4));
        try {
            check(prefs.edit().putBoolean("hide_system_apps", false).putBoolean("repair_service", false).putInt("appearance_cycle", 0).commit(), "Set bounded UI prerequisites");
            Apps.saveFavourites(test.getTargetContext(), original);
            apps[0] = start(test);

            headerNavigation(test, apps[0]);

            Set<String> menu = open(test, apps[0], original.get(0), bound);
            check(!menu.contains("favourites.remove") && !menu.contains("favourites.move-earlier"), "Minimum and first-slot boundaries");
            key(test, apps[0], KeyEvent.KEYCODE_BACK);
            verifyOrder(test, original, apps[0], original.get(0));

            String last = original.get(3);
            menu = open(test, apps[0], last, bound);
            check(!menu.contains("favourites.move-later"), "Last slot has no later action");
            choose(test, apps[0], "favourites.move-earlier");
            List<String> earlier = new ArrayList<>(original);
            Collections.swap(earlier, 2, 3);
            verifyOrder(test, earlier, apps[0], last);
            open(test, apps[0], last, bound);
            choose(test, apps[0], "favourites.move-later");
            verifyOrder(test, original, apps[0], last);

            menu = open(test, apps[0], fixture, bound);
            check(menu.contains("apps.uninstall") && menu.contains("favourites.add") && menu.contains("favourites.replace"), "Actual removable non-favourite actions");
            choose(test, apps[0], "favourites.cancel");
            verifyOrder(test, original, apps[0], fixture);
            open(test, apps[0], fixture, bound);
            choose(test, apps[0], "favourites.add");
            List<String> added = new ArrayList<>(original); added.add(fixture);
            verifyOrder(test, added, apps[0], fixture);
            menu = open(test, apps[0], fixture, bound);
            check(menu.contains("favourites.remove") && !menu.contains("favourites.add"), "Added favourite changes its actual actions");
            choose(test, apps[0], "favourites.remove");
            verifyOrder(test, original, apps[0], fixture);

            home[0] = replace(test, apps[0], fixture, bound); apps[0] = null;
            key(test, home[0], KeyEvent.KEYCODE_DPAD_UP);
            checkFocusedSlot(test, home[0], original.get(0));
            key(test, home[0], home[0].rtl ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
            checkFocusedSlot(test, home[0], original.get(1));
            key(test, home[0], KeyEvent.KEYCODE_BACK);
            check(Apps.favourites(test.getTargetContext()).equals(original), "Back cancels replacement without saving");
            onMain(test, () -> check(field(home[0], "replacementPackage") == null, "Cancel leaves replacement mode"));
            checkFocusedSlot(test, home[0], original.get(1));
            test.runOnMainSync(home[0]::finish); test.waitForIdleSync(); home[0] = null;

            apps[0] = start(test);
            home[0] = replace(test, apps[0], fixture, bound); apps[0] = null;
            key(test, home[0], home[0].rtl ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
            checkFocusedSlot(test, home[0], original.get(1));
            key(test, home[0], KeyEvent.KEYCODE_DPAD_CENTER);
            List<String> replaced = new ArrayList<>(original); replaced.set(1, fixture);
            check(Apps.favourites(test.getTargetContext()).equals(replaced), "OK replaces exactly the chosen slot");
            onMain(test, () -> {
                check(field(home[0], "replacementPackage") == null, "Save leaves replacement mode");
                View[] nodes = (View[]) field(home[0], "nodes");
                check(home[0].getCurrentFocus() == nodes[FavouriteChoice.F0 + 1], "Replacement returns focus to the chosen slot");
                check(((String[]) field(home[0], "nodePackage"))[FavouriteChoice.F0 + 1].equals(fixture), "Focused slot contains the replacement");
            });
            return bound;
        } finally {
            if (apps[0] != null) test.runOnMainSync(apps[0]::finish);
            if (home[0] != null) test.runOnMainSync(home[0]::finish);
            test.waitForIdleSync();
            SharedPreferences.Editor restore = prefs.edit();
            for (String name : new String[] {"favourites", "hide_system_apps", "repair_service", "appearance_cycle"}) {
                Object value = before.get(name);
                if (!before.containsKey(name)) restore.remove(name);
                else if (value instanceof Boolean) restore.putBoolean(name, (Boolean) value);
                else if (value instanceof Integer) restore.putInt(name, (Integer) value);
                else restore.putString(name, (String) value);
            }
            check(restore.commit() && prefs.getAll().equals(before), "Restore every original favourite/UI preference exactly");
        }
    }

    private static AllAppsActivity start(Instrumentation test) {
        AllAppsActivity activity = (AllAppsActivity) test.startActivitySync(new Intent(test.getTargetContext(), AllAppsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        test.waitForIdleSync(); waitFocus(activity);
        test.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP); test.waitForIdleSync();
        until(test, () -> !packages(activity).isEmpty(), "Actual app catalog loaded");
        return activity;
    }

    private static void headerNavigation(Instrumentation test, AllAppsActivity apps) {
        View back = (View) field(apps, "backButton");
        View prayer = (View) field(apps, "chip");
        View settings = (View) field(apps, "settingsButton");
        List<?> tiles = (List<?>) field(apps, "tiles");
        for (int i = 0; i < Math.min(5, tiles.size()); i++) {
            View tile = (View) tiles.get(i);
            onMain(test, tile::requestFocus);
            key(test, apps, KeyEvent.KEYCODE_DPAD_UP);
            onMain(test, () -> check(apps.getCurrentFocus() == back, "Every first-row column moves Up to Back"));
        }
        int end = apps.rtl ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
        int start = apps.rtl ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT;
        for (View target : new View[] {prayer, settings, back}) {
            key(test, apps, end);
            onMain(test, () -> check(apps.getCurrentFocus() == target, "Logical end stays within the Apps header"));
        }
        for (View target : new View[] {settings, prayer, back}) {
            key(test, apps, start);
            onMain(test, () -> check(apps.getCurrentFocus() == target, "Logical start stays within the Apps header"));
        }
        key(test, apps, KeyEvent.KEYCODE_DPAD_DOWN);
        onMain(test, () -> check(apps.getCurrentFocus() == tiles.get(0), "Back Down returns to the first app"));
        if (tiles.size() > 5) {
            onMain(test, () -> ((View) tiles.get(5)).requestFocus());
            key(test, apps, KeyEvent.KEYCODE_DPAD_UP);
            onMain(test, () -> check(apps.getCurrentFocus() == tiles.get(0), "Lower-row Up stays in app content"));
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> packages(AllAppsActivity apps) { return (List<String>) field(apps, "apps"); }

    private static String focusedPackage(AllAppsActivity apps) {
        int index = ((List<?>) field(apps, "tiles")).indexOf(apps.getCurrentFocus());
        return index < 0 ? null : packages(apps).get(index);
    }

    private static Set<String> open(Instrumentation test, AllAppsActivity apps, String pkg, Set<String> bound) {
        until(test, () -> packages(apps).contains(pkg), "Requested app is loaded");
        for (int guard = 0; guard < packages(apps).size() + 5; guard++) {
            int[] position = new int[2];
            onMain(test, () -> {
                position[0] = ((List<?>) field(apps, "tiles")).indexOf(apps.getCurrentFocus());
                position[1] = packages(apps).indexOf(pkg);
            });
            if (position[0] == position[1]) break;
            int key = position[0] < 0 || position[0] / 5 < position[1] / 5 ? KeyEvent.KEYCODE_DPAD_DOWN
                : position[0] / 5 > position[1] / 5 ? KeyEvent.KEYCODE_DPAD_UP
                : (position[0] < position[1]) != apps.rtl ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT;
            key(test, apps, key);
        }
        Map<String, ?> before = Prefs.of(apps).getAll();
        onMain(test, () -> {
            check(pkg.equals(focusedPackage(apps)), "Remote focuses requested app");
            long time = SystemClock.uptimeMillis();
            apps.dispatchKeyEvent(new KeyEvent(time, time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, 0));
            apps.dispatchKeyEvent(KeyEvent.changeFlags(new KeyEvent(time, time + 600, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, 1), KeyEvent.FLAG_LONG_PRESS));
            apps.dispatchKeyEvent(new KeyEvent(time, time + 610, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER, 0));
            check(field(apps, "menu") != null, "Long OK opens the actual menu");
        });
        check(Prefs.of(apps).getAll().equals(before), "Opening OK release does not choose an action");
        Set<String> found = new LinkedHashSet<>();
        onMain(test, () -> {
            for (Object item : (List<?>) field(apps, "menuItems")) {
                FocusTile tile = (FocusTile) item;
                check(tile.getTag() instanceof UiControl, "Actual menu item has a shared binding");
                UiControl control = (UiControl) tile.getTag();
                check(control.getKind().equals("action") && found.add(control.getId()), "Unique action binding in actual menu");
                try {
                    java.lang.reflect.Method method = Text.class.getDeclaredMethod(control.getLabelKey()); method.setAccessible(true);
                    check(((TextView) tile.getChildAt(0)).getText().toString().equals(method.invoke(apps.t)), "Canonical action label");
                } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
            }
        });
        bound.addAll(found);
        return found;
    }

    private static void choose(Instrumentation test, AllAppsActivity apps, String id) {
        int[] index = {-1};
        onMain(test, () -> {
            List<?> items = (List<?>) field(apps, "menuItems");
            for (int i = 0; i < items.size(); i++) if (((UiControl) ((View) items.get(i)).getTag()).getId().equals(id)) index[0] = i;
        });
        check(index[0] >= 0, "Requested action exists: " + id);
        for (int i = 0; i < index[0]; i++) key(test, apps, KeyEvent.KEYCODE_DPAD_DOWN);
        key(test, apps, KeyEvent.KEYCODE_DPAD_CENTER);
    }

    private static void verifyOrder(Instrumentation test, List<String> expected, AllAppsActivity apps, String focused) {
        check(Apps.favourites(apps).equals(expected), "Exact stored favourite order");
        until(test, () -> field(apps, "menu") == null && expected.equals(field(apps, "favs")) && focused.equals(focusedPackage(apps)), "Rebuild restores app focus and favourite markers");
        onMain(test, () -> {
            FocusTile tile = (FocusTile) apps.getCurrentFocus();
            boolean star = false;
            for (int i = 0; i < tile.getChildCount(); i++) star |= apps.t.favourite().equals(tile.getChildAt(i).getContentDescription());
            check(star == expected.contains(focused), "Actual tile's favourite marker matches saved state");
        });
    }

    private static MainActivity replace(Instrumentation test, AllAppsActivity apps, String pkg, Set<String> bound) {
        open(test, apps, pkg, bound);
        Instrumentation.ActivityMonitor monitor = test.addMonitor(MainActivity.class.getName(), null, false);
        MainActivity home;
        try {
            choose(test, apps, "favourites.replace");
            home = (MainActivity) test.waitForMonitorWithTimeout(monitor, 5000);
            check(home != null, "Replace opens actual Home selection");
        } finally { test.removeMonitor(monitor); }
        test.waitForIdleSync(); waitFocus(home);
        onMain(test, () -> {
            check(pkg.equals(field(home, "replacementPackage")), "Replacement retains selected package");
            View[] nodes = (View[]) field(home, "nodes");
            for (int i = 0; i < nodes.length; i++) if (nodes[i] != null) {
                boolean candidate = i >= FavouriteChoice.F0 && i < FavouriteChoice.M0;
                check(nodes[i].isFocusable() == candidate && nodes[i].isEnabled() == candidate, "Replacement confines focus to real favourite slots");
                if (candidate) check(((UiControl) nodes[i].getTag()).getId().equals("favourites.replace"), "Actual replacement target binding");
            }
        });
        return home;
    }

    private static void checkFocusedSlot(Instrumentation test, MainActivity home, String pkg) {
        onMain(test, () -> {
            View[] nodes = (View[]) field(home, "nodes");
            int index = java.util.Arrays.asList(nodes).indexOf(home.getCurrentFocus());
            check(index >= 0 && pkg.equals(((String[]) field(home, "nodePackage"))[index]), "Remote input/save/cancel preserves the expected favourite focus");
        });
    }

    private static void until(Instrumentation test, BooleanSupplier condition, String message) {
        long until = SystemClock.uptimeMillis() + 5000;
        boolean[] ready = {false};
        do {
            onMain(test, () -> ready[0] = condition.getAsBoolean());
            if (ready[0]) return;
            SystemClock.sleep(25);
        } while (SystemClock.uptimeMillis() < until);
        throw new AssertionError(message);
    }

    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
