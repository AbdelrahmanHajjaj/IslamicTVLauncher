package tv.afnon.home;

import com.batoulapps.adhan.*;
import com.batoulapps.adhan.data.CalendarUtil;
import com.batoulapps.adhan.data.DateComponents;
import java.util.Date;
import java.util.TimeZone;

/** Real library boundary: seconds must survive until the shared downward minute rule. */
public final class PrayerRoundingCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static Date[] times(PrayerTimes day) {
        return new Date[] {day.fajr, day.sunrise, day.dhuhr, day.asr, day.maghrib, day.isha};
    }

    public static void main(String[] args) {
        for (long[] vector : new long[][] {{30000,0},{59999,0},{0,0},{60000,60000},{-1,-60000},{-30000,-60000},{-60000,-60000}}) {
            check(CalendarUtil.roundedMinute(new Date(vector[0])).getTime() == vector[1], "Library must round down, including :30 and milliseconds: " + vector[0]);
        }
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Africa/Cairo"));
            Coordinates where = new Coordinates(30.0444, 31.2357);
            DateComponents date = new DateComponents(2026, 11, 12);
            int[] offsets = {1,-1,30,-30,0,2};
            for (PrayerMethod method : PrayerMethod.values()) {
                CalculationParameters params = method.getParameters();
                Date[] base = times(new PrayerTimes(where, date, params));
                params.adjustments = new PrayerAdjustments(offsets[0],offsets[1],offsets[2],offsets[3],offsets[4],offsets[5]);
                Date[] shifted = times(new PrayerTimes(where, date, params));
                for (int i = 0; i < 6; i++) {
                    check(base[i].getTime() % 60000 == 0, "Whole minute for " + method + ":" + i);
                    check(shifted[i].getTime() - base[i].getTime() == offsets[i] * 60000L, "Offset applied once for " + method + ":" + i);
                }
            }
            Date[] cairo = times(new PrayerTimes(where,date,CalculationMethod.EGYPTIAN.getParameters()));
            int[] expected = {288,377,700,879,1020,1100};
            java.util.Calendar clock = java.util.Calendar.getInstance();
            for (int i = 0; i < 6; i++) {
                clock.setTime(cairo[i]);
                check(clock.get(java.util.Calendar.HOUR_OF_DAY) * 60 + clock.get(java.util.Calendar.MINUTE) == expected[i], "Cairo floor vector: " + i);
            }
        } finally { TimeZone.setDefault(original); }
        System.out.println("PASS: actual library floors half-minutes/epochs; all 19 methods keep offsets and Cairo matches the downward-rounding vector.");
    }
}
