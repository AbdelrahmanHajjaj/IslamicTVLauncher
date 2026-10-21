package tv.afnon.home;

import android.content.Context;
import android.icu.util.IslamicCalendar;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.Qibla;
import com.batoulapps.adhan.data.DateComponents;

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
        this(now, Prefs.where(c), Prefs.method(c), Prefs.madhab(c), Prefs.hijriOffset(c));
    }

    PrayerDay(Date now, Coordinates where, CalculationMethod method, Madhab madhab, int hijriOffset) {
        CalculationParameters params = method.getParameters();
        params.madhab = madhab;
        PrayerTimes today = new PrayerTimes(where, DateComponents.from(now), params);
        times[FAJR] = today.fajr;
        times[SUNRISE] = today.sunrise;
        times[DHUHR] = today.dhuhr;
        times[ASR] = today.asr;
        times[MAGHRIB] = today.maghrib;
        times[ISHA] = today.isha;

        int latest = -1;
        for (int i = 0; i < 6; i++) if (!times[i].after(now)) latest = i;
        nowIndex = latest == SUNRISE ? -1 : latest;

        int next = -1;
        for (int i = 0; i < 6; i++) {
            if (i != SUNRISE && times[i].after(now)) { next = i; break; }
        }
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrowFajr = new PrayerTimes(where, DateComponents.from(c.getTime()), params).fajr;
        if (next == -1) {
            next = FAJR;
            times[FAJR] = tomorrowFajr;
        }
        nextIndex = next;
        nextTime = times[next];
        suhoorEnd = today.fajr.after(now) ? today.fajr : tomorrowFajr;

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
}
