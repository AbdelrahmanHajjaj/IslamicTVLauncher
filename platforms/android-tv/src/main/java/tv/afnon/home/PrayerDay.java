package tv.afnon.home;

import android.content.Context;
import android.icu.util.IslamicCalendar;

import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.PrayerAdjustments;
import com.batoulapps.adhan.Qibla;
import com.batoulapps.adhan.data.DateComponents;
import tv.afnon.core.PrayerRules;
import tv.afnon.core.PrayerSelection;

import java.util.Calendar;
import java.util.Date;

/** One moment's view of the prayer day: the six times, which prayer is current and which is next. */
final class PrayerDay {
    static final int FAJR = 0, SUNRISE = 1, DHUHR = 2, ASR = 3, MAGHRIB = 4, ISHA = 5;

    /** Today's times, except that after Isha the Fajr slot holds tomorrow's Fajr (it is the next prayer). */
    final Date[] times = new Date[6];
    /** Index of the current prayer, or -1 before Fajr and between Sunrise and Dhuhr. */
    final int nowIndex;
    /** Index of the next prayer (never Sunrise, which is a marker, not a prayer). */
    final int nextIndex;
    final Date nextTime;
    /** The Fajr that ends tonight's Suhoor: today's before dawn, otherwise tomorrow's. */
    final Date suhoorEnd;
    final boolean friday;
    final boolean ramadan;
    final int hijriDay, hijriMonth, hijriYear;
    final double qibla;

    /** With the saved location, method, Asr school and Hijri adjustment (Afnon Settings). */
    PrayerDay(Context c, Date now) {
        this(now, Prefs.where(c), Prefs.method(c), Prefs.madhab(c), Prefs.hijriOffset(c), Prefs.prayerOffsets(c));
    }

    PrayerDay(Date now, Coordinates where, PrayerMethod method, Madhab madhab, int hijriOffset, int[] offsets) {
        CalculationParameters params = parameters(method, madhab, offsets);
        // Reuse these parameters for tomorrow too, so Fajr alerts and Suhoor share the displayed offset.
        System.arraycopy(times(new PrayerTimes(where, DateComponents.from(now), params)), 0, times, 0, 6);

        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrowFajr = minute(new PrayerTimes(where, DateComponents.from(c.getTime()), params).fajr);
        double[] epochs = new double[6];
        for (int i = 0; i < epochs.length; i++) epochs[i] = times[i].getTime();
        PrayerSelection selected = PrayerRules.INSTANCE.select(epochs, tomorrowFajr.getTime(), now.getTime());
        nowIndex = selected.getCurrent();
        nextIndex = selected.getNext();
        // Android displays tomorrow's Fajr in this slot only after today's prayers have passed.
        if (nextIndex == FAJR && !times[FAJR].after(now)) times[FAJR] = tomorrowFajr;
        nextTime = times[nextIndex];
        suhoorEnd = times[FAJR].after(now) ? times[FAJR] : tomorrowFajr;

        c.setTime(now);
        friday = c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;

        IslamicCalendar h = new IslamicCalendar();
        h.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC_UMALQURA);
        h.setTime(now);
        if (hijriOffset != 0) h.add(IslamicCalendar.DAY_OF_MONTH, hijriOffset);
        hijriDay = h.get(IslamicCalendar.DAY_OF_MONTH);
        hijriMonth = h.get(IslamicCalendar.MONTH);
        hijriYear = h.get(IslamicCalendar.YEAR);
        ramadan = hijriMonth == IslamicCalendar.RAMADAN;

        qibla = new Qibla(where).direction;
    }

    static Date[] civilTimes(DateComponents date, Coordinates where, PrayerMethod method, Madhab madhab, int[] offsets) {
        return times(new PrayerTimes(where, date, parameters(method, madhab, offsets)));
    }

    private static CalculationParameters parameters(PrayerMethod method, Madhab madhab, int[] offsets) {
        PrayerRules.INSTANCE.validateOffsets(offsets);
        CalculationParameters params = method.getParameters();
        params.madhab = madhab;
        params.adjustments = new PrayerAdjustments(offsets[0], offsets[1], offsets[2], offsets[3], offsets[4], offsets[5]);
        return params;
    }

    private static Date[] times(PrayerTimes day) {
        return new Date[] {minute(day.fajr), minute(day.sunrise), minute(day.dhuhr), minute(day.asr), minute(day.maghrib), minute(day.isha)};
    }

    /**
     * The time on its whole minute. adhan-java keeps the milliseconds of the moment it was asked, so the same
     * prayer came out a little different on every check, and "already shown" never matched: the reminder and the
     * call kept coming back after being closed.
     */
    private static Date minute(Date d) {
        return new Date(d.getTime() / 60_000 * 60_000);
    }
}
