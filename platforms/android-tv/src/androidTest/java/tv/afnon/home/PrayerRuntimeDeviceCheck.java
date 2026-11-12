package tv.afnon.home;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Build;
import android.os.Bundle;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Madhab;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import tv.afnon.core.PrayerRules;
import tv.afnon.core.PrayerSelection;
import tv.afnon.core.ReminderEvent;
import tv.afnon.core.ReminderRules;

/** Runs the existing prayer regression against ART and real Android ICU, without settings/UI changes. */
final class PrayerRuntimeDeviceCheck {
    static void run(Instrumentation instrumentation) {
        TimeZone originalJava = TimeZone.getDefault();
        Bundle result = new Bundle();
        int code = Activity.RESULT_CANCELED;
        try {
            PrayerDayCheck.main(new String[0]);
            core();
            int cases = calendars(instrumentation);
            result.putString("stream", "Native prayer regression, core/reminder linkage and " + cases
                + " real ICU calendar cases passed on API " + Build.VERSION.SDK_INT
                + " / ICU " + android.icu.util.VersionInfo.ICU_VERSION + ".\n");
            code = Activity.RESULT_OK;
        } catch (Throwable error) {
            result.putString("stream", android.util.Log.getStackTraceString(error));
        } finally {
            TimeZone.setDefault(originalJava);
        }
        instrumentation.finish(code, result);
    }

    private static void core() {
        check(PrayerRules.INSTANCE.floorToMinute(30_000) == 0, "Shared downward minute");
        check(com.batoulapps.adhan.data.CalendarUtil.roundedMinute(new Date(30_000)).getTime() == 0,
            "Actual Android library uses shared downward minute");
        check(com.batoulapps.adhan.data.CalendarUtil.roundedMinute(new Date(-1)).getTime() == -60_000,
            "Actual Android library floors before epoch");
        check(PrayerRules.INSTANCE.normalizeOffset(Double.NaN) == 0, "Nonfinite offset");
        check(PrayerRules.INSTANCE.normalizeOffset(100) == 30, "Offset clamp");
        double[] times = {1_000_000, 2_000_000, 3_000_000, 4_000_000, 5_000_000, 6_000_000};
        PrayerSelection selected = PrayerRules.INSTANCE.select(times, 7_000_000, 2_000_000);
        check(selected.getCurrent() == -1 && selected.getNext() == 2, "Sunrise is a marker");
        check(ReminderRules.INSTANCE.dueCall(times, 1_000_000, true, 63, 120_000) == 0, "Native due boundary");
        check(ReminderRules.INSTANCE.dueCall(times, 1_120_000, true, 63, 120_000) == -1, "Native expiry boundary");
        check(ReminderRules.INSTANCE.dueReminder(1_000_000, 940_001, 1), "Reminder rounding");
        ReminderEvent event = ReminderRules.INSTANCE.nextScheduledEvent(new double[][] {times, times, times},
            999_999, 0, 63, true, new String[0]);
        check(event != null && event.getId().equals("call:0:1000000"), "Stable event ID");
        check(ReminderRules.INSTANCE.deliveryState(1_000_000, 1_090_000, 90_000).equals("due"), "Inclusive delivery");
        check(ReminderRules.INSTANCE.deliveryState(1_000_000, 1_090_001, 90_000).equals("expired"), "Delivery expiry");
    }

    private static int calendars(Instrumentation instrumentation) throws Exception {
        JSONArray cases;
        try (InputStream input = instrumentation.getContext().getAssets().open("calendar-cases.json");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            cases = new JSONArray(output.toString("UTF-8"));
        }
        check(cases.length() == 14, "Frozen calendar case count");
        for (int i = 0; i < cases.length(); i++) {
            JSONObject fixture = cases.getJSONObject(i);
            String zone = fixture.getString("zone");
            TimeZone.setDefault(TimeZone.getTimeZone(zone));
            Date now = Date.from(Instant.parse(fixture.getString("at")));
            PrayerDay day = new PrayerDay(now, new Coordinates(30.0444, 31.2357), PrayerMethod.EGYPTIAN,
                Madhab.SHAFI, fixture.getInt("offset"), new int[6]);
            JSONArray expected = fixture.getJSONArray("hijri");
            String label = zone + " " + fixture.getString("at") + " offset " + fixture.getInt("offset");
            check(day.hijriYear == expected.getInt(0) && day.hijriMonth == expected.getInt(1)
                && day.hijriDay == expected.getInt(2), "Umm al-Qura " + label);
            check(day.ramadan == (expected.getInt(1) == 8), "Ramadan " + label);
            check(day.friday == fixture.getBoolean("friday"), "Friday " + label);
        }
        return cases.length();
    }

    private static void check(boolean success, String message) {
        if (!success) throw new AssertionError(message);
    }
}
