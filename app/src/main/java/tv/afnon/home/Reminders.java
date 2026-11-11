package tv.afnon.home;

import android.content.Context;
import tv.afnon.core.ReminderRules;

import java.util.Date;

/**
 * When the reminder and the call to prayer are due. The remote buttons service checks every few seconds and
 * shows them; Home shows its own reminder card while it is in front, and the screensaver steps aside for the call.
 */
final class Reminders {
    private Reminders() {}

    /** How long after a prayer time the call screen may still open (the check runs every 15 s). */
    static final long CALL_WINDOW_MS = 120_000;

    /** Set by Home while it is in front: the reminder becomes Home's own card instead of the banner. */
    interface HomeCard {
        void showReminder();
    }

    static HomeCard home;

    /** The prayer whose time began within the last two minutes and has its adhan on; -1 if none. */
    static int dueCall(Context c, PrayerDay day, Date now) {
        return ReminderRules.INSTANCE.dueCall(times(day), now.getTime(), Prefs.adhan(c), bellMask(c), CALL_WINDOW_MS);
    }

    /**
     * The prayer whose time began within the last two minutes with its adhan off (all of them, or that prayer's
     * bell), for the spoken «حان الآن موعد صلاة …»; -1 if none, or if that announcement is turned off.
     */
    static int dueAnnounce(Context c, PrayerDay day, Date now) {
        return ReminderRules.INSTANCE.dueAnnounce(times(day), now.getTime(), Prefs.adhan(c), Prefs.announce(c), bellMask(c), CALL_WINDOW_MS);
    }

    /** Whether the next prayer is inside the reminder window (Settings: 5 to 30 minutes before, or off). */
    static boolean dueReminder(Context c, PrayerDay day, Date now) {
        return ReminderRules.INSTANCE.dueReminder(day.nextTime.getTime(), now.getTime(), Prefs.remindMinutes(c));
    }

    /** Minutes left, rounded up, as the reminder words it. */
    static long minutesLeft(PrayerDay day, Date now) {
        return (long) ReminderRules.INSTANCE.minutesLeft(day.nextTime.getTime(), now.getTime());
    }

    private static double[] times(PrayerDay day) {
        double[] times = new double[6];
        for (int i = 0; i < times.length; i++) times[i] = day.times[i].getTime();
        return times;
    }

    private static int bellMask(Context c) {
        int mask = 0;
        for (int i = 0; i < 6; i++) if (Prefs.bell(c, i)) mask |= 1 << i;
        return mask;
    }
}
