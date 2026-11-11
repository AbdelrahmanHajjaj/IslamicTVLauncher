package tv.afnon.home;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import tv.afnon.core.PrayerRules;
import tv.afnon.core.UiContract;
import tv.afnon.core.UiControl;

/** Exercises the actual prayer screen without scheduling or playing alerts. */
final class PrayerUiDeviceCheck {
    static Set<String> run(Instrumentation test, SettingsActivity parent) {
        SharedPreferences prefs = Prefs.of(test.getTargetContext());
        Map<String, ?> before = prefs.getAll();
        Set<String> bound = new LinkedHashSet<>();
        check(prefs.edit().putBoolean("bell_1", true).commit(), "Seed ignored legacy sunrise flag");
        PrayerDayActivity day = (PrayerDayActivity) test.startActivitySync(new Intent(test.getTargetContext(), PrayerDayActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        test.waitForIdleSync();
        PrayerDayActivity[] editor = new PrayerDayActivity[1];
        try {
            waitFocus(day);
            test.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
            test.waitForIdleSync();
            UiContractDeviceCheck.onMain(test, () -> {
                FocusTile[] rows = (FocusTile[]) field(day, "row");
                Map<String, ?> infoBefore = prefs.getAll();
                rows[PrayerDay.SUNRISE].performClick();
                check(prefs.getAll().equals(infoBefore), "Sunrise information must not write an adhan setting");
                check(!rows[PrayerDay.SUNRISE].isFocusable() && !rows[PrayerDay.SUNRISE].isClickable(), "Sunrise must be informational");
                View[] bells = (View[]) field(day, "bell");
                check(bells[PrayerDay.SUNRISE].getVisibility() != View.VISIBLE, "No sunrise adhan affordance");
                check(rows[PrayerDay.FAJR].requestFocus(), "Fajr focuses after remote input; touch=" + rows[PrayerDay.FAJR].isInTouchMode());
            });
            UiContractDeviceCheck.key(test, day, KeyEvent.KEYCODE_DPAD_DOWN);
            UiContractDeviceCheck.onMain(test, () -> check(day.getCurrentFocus() == ((View[]) field(day, "row"))[PrayerDay.DHUHR], "Down skips sunrise"));
            UiContractDeviceCheck.key(test, day, KeyEvent.KEYCODE_DPAD_UP);
            UiContractDeviceCheck.onMain(test, () -> check(day.getCurrentFocus() == ((View[]) field(day, "row"))[PrayerDay.FAJR], "Up skips sunrise"));
            for (int i = 0; i < 6; i++) {
                if (!PrayerRules.INSTANCE.isPrayer(i)) continue;
                final int prayer = i;
                boolean original = prefs.getBoolean("bell_" + i, true);
                UiContractDeviceCheck.onMain(test, () -> {
                    FocusTile row = ((FocusTile[]) field(day, "row"))[prayer];
                    UiControl control = (UiControl) row.getTag();
                    check(control.getId().equals(UiContract.INSTANCE.prayerAdhanId(prayer)), "Actual per-prayer binding");
                    check(bound.add(control.getId()), "Unique per-prayer binding");
                    row.requestFocus();
                });
                UiContractDeviceCheck.key(test, day, KeyEvent.KEYCODE_DPAD_CENTER);
                check(prefs.getBoolean("bell_" + i, true) == !original, "OK saves the corresponding prayer switch");
                UiContractDeviceCheck.onMain(test, () -> check(((View[]) field(day, "bell"))[prayer].getContentDescription().equals(day.t.bell(!original)), "Rendered switch matches saved state"));
            }
            UiContractDeviceCheck.onMain(test, () -> {
                View room = (View) field(day, "roomButton");
                UiControl control = (UiControl) room.getTag();
                check(control.getId().equals("prayer.facing") && control.getKind().equals("number"), "Bound numeric room editor");
                bound.add(control.getId());
                day.setFacing(350);
                room.requestFocus();
            });
            UiContractDeviceCheck.key(test, day, KeyEvent.KEYCODE_DPAD_CENTER);
            facing(test, day, 350);
            UiContractDeviceCheck.key(test, day, KeyEvent.KEYCODE_DPAD_RIGHT); facing(test, day, 355);
            UiContractDeviceCheck.key(test, day, KeyEvent.KEYCODE_DPAD_RIGHT); facing(test, day, 0);
            UiContractDeviceCheck.key(test, day, KeyEvent.KEYCODE_DPAD_UP); facing(test, day, 45);
            UiContractDeviceCheck.key(test, day, KeyEvent.KEYCODE_DPAD_DOWN); facing(test, day, 0);
            UiContractDeviceCheck.key(test, day, KeyEvent.KEYCODE_DPAD_LEFT); facing(test, day, 355);
            UiContractDeviceCheck.key(test, day, KeyEvent.KEYCODE_BACK);
            UiContractDeviceCheck.onMain(test, () -> {
                check(field(day, "picker") == null && day.getCurrentFocus() == field(day, "roomButton"), "Cancel restores origin focus");
                check(day.facing() == 350, "Cancel retains saved facing");
            });
            test.runOnMainSync(day::finish);
            test.waitForIdleSync(); waitFocus(parent);
            test.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
            test.waitForIdleSync();
            if (!(Boolean) field(parent, "onNav")) UiContractDeviceCheck.key(test, parent, KeyEvent.KEYCODE_BACK);
            int section = (Integer) field(parent, "section");
            for (int i = 0; i < section; i++) UiContractDeviceCheck.key(test, parent, KeyEvent.KEYCODE_DPAD_UP);
            UiContractDeviceCheck.key(test, parent, KeyEvent.KEYCODE_DPAD_CENTER);
            for (int i = 0; i < 5; i++) UiContractDeviceCheck.key(test, parent, KeyEvent.KEYCODE_DPAD_DOWN);
            UiContractDeviceCheck.onMain(test, () -> {
                check(parent.getCurrentFocus() != null, "Room setting has focus; touch=" + parent.root.isInTouchMode());
                check(((SettingsActivity.Row) parent.getCurrentFocus().getTag()).contract.getId().equals("prayer.facing"), "Room setting focused");
            });
            Instrumentation.ActivityMonitor monitor = test.addMonitor(PrayerDayActivity.class.getName(), null, false);
            try {
                UiContractDeviceCheck.key(test, parent, KeyEvent.KEYCODE_DPAD_CENTER);
                editor[0] = (PrayerDayActivity) test.waitForMonitorWithTimeout(monitor, 5000);
                check(editor[0] != null, "Setting opens its actual room editor");
            } finally { test.removeMonitor(monitor); }
            test.waitForIdleSync(); waitFocus(editor[0]); facing(test, editor[0], 350);
            UiContractDeviceCheck.key(test, editor[0], KeyEvent.KEYCODE_DPAD_RIGHT); facing(test, editor[0], 355);
            UiContractDeviceCheck.key(test, editor[0], KeyEvent.KEYCODE_DPAD_CENTER);
            test.waitForIdleSync(); waitFocus(parent);
            check(prefs.getInt("facing", -1) == 355, "OK commits bearing");
            UiContractDeviceCheck.onMain(test, () -> {
                SettingsActivity.Row row = (SettingsActivity.Row) parent.getCurrentFocus().getTag();
                check(row.contract.getId().equals("prayer.facing"), "Save returns focus to originating setting");
                check(((TextView) row.control.getChildAt(0)).getText().toString().equals(parent.t.bearingLabel(355)), "Saved bearing is visible in Settings");
            });
            return bound;
        } finally {
            test.runOnMainSync(day::finish);
            if (editor[0] != null) test.runOnMainSync(editor[0]::finish);
            SharedPreferences.Editor restore = prefs.edit();
            for (int i = 0; i < 6; i++) {
                String key = "bell_" + i;
                if (before.containsKey(key)) restore.putBoolean(key, (Boolean) before.get(key)); else restore.remove(key);
            }
            if (before.containsKey("facing")) restore.putInt("facing", (Integer) before.get("facing")); else restore.remove("facing");
            check(restore.commit() && prefs.getAll().equals(before), "Restore every prayer/facing preference exactly");
        }
    }

    private static void waitFocus(android.app.Activity activity) {
        long until = SystemClock.uptimeMillis() + 5000;
        while (!activity.hasWindowFocus() && SystemClock.uptimeMillis() < until) SystemClock.sleep(25);
        check(activity.hasWindowFocus(), "Activity has window focus");
    }

    private static void facing(Instrumentation test, PrayerDayActivity day, int expected) {
        UiContractDeviceCheck.onMain(test, () -> {
            check(field(day, "picker") != null, "Room editor owns keys");
            check((Integer) field(day, "pickerFacing") == expected, "Bearing input including wrap and RTL");
            check(((TextView) field(day, "pickerValue")).getText().toString().equals(day.t.bearingLabel(expected)), "Rendered bearing value");
        });
    }

    static Object field(Object target, String name) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
