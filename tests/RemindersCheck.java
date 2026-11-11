package tv.afnon.home;

import android.content.Context;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Madhab;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

public final class RemindersCheck {
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        PrefsCheck.Memory saved = new PrefsCheck.Memory();
        Context c = PrefsCheck.context(saved);
        PrayerDay day = new PrayerDay(Date.from(Instant.parse("2026-11-10T00:00:00Z")),
            new Coordinates(30.0444, 31.2357), PrayerMethod.EGYPTIAN, Madhab.SHAFI, 0, new int[6]);
        check(Prefs.adhan(c) && Prefs.announce(c) && Prefs.remindVoice(c), "Native audio defaults remain enabled");
        check(!Prefs.bell(c, 1) && Prefs.remindMinutes(c) == 15, "Native bell/lead defaults");
        for (int mask = 0; mask < 64; mask++) {
            for (int i = 0; i < 6; i++) saved.values.put("bell_" + i, (mask & 1 << i) != 0);
            for (boolean adhan : new boolean[] {false, true}) for (boolean announce : new boolean[] {false, true}) {
                saved.values.put("adhan", adhan); saved.values.put("announce", announce);
                for (int i = 0; i < 6; i++) for (long delta : new long[] {-1, 0, 119999, 120000}) {
                    Date now = new Date(day.times[i].getTime() + delta);
                    boolean due = i != 1 && delta >= 0 && delta < 120000;
                    boolean bell = (mask & 1 << i) != 0;
                    check(Reminders.dueCall(c, day, now) == (due && adhan && bell ? i : -1), "Call mask/window boundary");
                    check(Reminders.dueAnnounce(c, day, now) == (due && announce && !(adhan && bell) ? i : -1), "Announcement replaces disabled adhan");
                }
            }
        }
        saved.values.put("remind", 5);
        for (long left : new long[] {-1, 0, 1, 999, 1000, 299999, 300000, 300001}) {
            Date now = new Date(day.nextTime.getTime() - left);
            check(Reminders.dueReminder(c, day, now) == (left > 0 && left <= 300000), "Reminder rounded-second boundary");
        }
        for (int disabled : new int[] {-5, 0}) {
            saved.values.put("remind", disabled);
            check(!Reminders.dueReminder(c, day, new Date(day.nextTime.getTime() - 1)), "Disabled reminders");
        }
        for (long[] sample : new long[][] {{-1,0}, {0,0}, {1,1}, {60000,1}, {60001,2}, {120000,2}}) {
            check(Reminders.minutesLeft(day, new Date(day.nextTime.getTime() - sample[0])) == sample[1], "Ceiling minutes");
        }
    }
}
