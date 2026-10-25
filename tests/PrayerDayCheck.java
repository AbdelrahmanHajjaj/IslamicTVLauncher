package tv.afnon.home;

import com.batoulapps.adhan.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

public final class PrayerDayCheck {
    private static PrayerDay day(Date now, int[] offsets) {
        return new PrayerDay(now, new Coordinates(52.2913, -1.5364),
            PrayerMethod.MUSLIM_WORLD_LEAGUE, Madhab.SHAFI, 0, offsets);
    }
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
        check(PrayerMethod.values().length == 19, "All supported presets are exposed");
        Coordinates here = new Coordinates(52.2913, -1.5364);
        for (String date : new String[] {"2026-03-29", "2026-06-21", "2026-10-25", "2026-12-21"}) {
            Date now = Date.from(LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant());
            for (PrayerMethod method : PrayerMethod.values()) {
                PrayerDay result = new PrayerDay(now, here, method, Madhab.SHAFI, 0, new int[6]);
                for (Date time : result.times) check(time != null && Math.abs(time.getTime() - now.getTime()) < 36 * 3_600_000L,
                    method + " returns finite nearby times on " + date);
                int[] adjust = {1, -2, 3, 1, 2, 4};
                PrayerDay shifted = new PrayerDay(now, here, method, Madhab.SHAFI, 0, adjust);
                for (int i = 0; i < 6; i++) check(shifted.times[i].getTime() - result.times[i].getTime() == adjust[i] * 60_000L,
                    method + " preserves per-prayer offsets");
            }
            for (CalculationMethod old : CalculationMethod.values()) {
                if (old == CalculationMethod.OTHER) continue;
                PrayerTimes expected = new PrayerTimes(here, com.batoulapps.adhan.data.DateComponents.from(now), old.getParameters());
                PrayerDay actual = new PrayerDay(now, here, PrayerMethod.valueOf(old.name()), Madhab.SHAFI, 0, new int[6]);
                Date[] times = {expected.fajr, expected.sunrise, expected.dhuhr, expected.asr, expected.maghrib, expected.isha};
                for (int i = 0; i < 6; i++) check(actual.times[i].getTime() == times[i].getTime() / 60_000 * 60_000,
                    old + " existing times must not change");
            }
        }
        Date winter = Date.from(LocalDate.parse("2026-12-21").atStartOfDay(ZoneId.systemDefault()).toInstant());
        PrayerDay france = new PrayerDay(winter, here, PrayerMethod.FRANCE, Madhab.SHAFI, 0, new int[6]);
        PrayerDay mwl = day(winter, new int[6]);
        check(france.times[0].after(mwl.times[0]) && france.times[5].before(mwl.times[5]), "France's 12-degree twilight changes both times");
        PrayerDay gulf = new PrayerDay(winter, here, PrayerMethod.GULF, Madhab.SHAFI, 0, new int[6]);
        check(gulf.times[5].getTime() - gulf.times[4].getTime() == 90 * 60_000, "Gulf Isha follows Maghrib by 90 minutes");
        PrayerDay turkey = new PrayerDay(winter, here, PrayerMethod.TURKEY, Madhab.SHAFI, 0, new int[6]);
        check(turkey.times[1].getTime() - mwl.times[1].getTime() == -7 * 60_000, "Turkey sunrise adjustment");
        check(turkey.times[4].getTime() - mwl.times[4].getTime() == 7 * 60_000, "Turkey Maghrib adjustment");
        int[] offsets = {3, -1, 2, 4, -2, 5};
        for (String date : new String[] {"2026-03-29", "2026-06-21", "2026-10-25", "2026-12-21"}) {
            Date midnight = Date.from(LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant());
            PrayerDay plain = day(midnight, new int[6]), shifted = day(midnight, offsets);
            for (int i = 0; i < 6; i++) {
                check(shifted.times[i].getTime() - plain.times[i].getTime() == offsets[i] * 60_000L,
                    date + " offset for prayer " + i);
            }
            Date nearFajr = new Date(plain.times[0].getTime() + 60_000);
            check(day(nearFajr, new int[6]).nextIndex != PrayerDay.FAJR, "Original Fajr has passed");
            check(day(nearFajr, offsets).nextIndex == PrayerDay.FAJR, "Offset must move the countdown boundary");
            Date afterIsha = new Date(shifted.times[PrayerDay.ISHA].getTime() + 60_000);
            plain = day(afterIsha, new int[6]);
            shifted = day(afterIsha, offsets);
            // At UK midsummer this method's Isha is after midnight; Fajr may already have passed too.
            check(shifted.nextIndex == plain.nextIndex, "Same next prayer after both adjusted times");
            check(shifted.nextTime.getTime() - plain.nextTime.getTime() == offsets[plain.nextIndex] * 60_000L, "Next prayer offset");
            check(shifted.suhoorEnd.getTime() - plain.suhoorEnd.getTime() == 3 * 60_000L, "Suhoor uses adjusted Fajr");
            if (!date.equals("2026-06-21")) check(shifted.nextIndex == PrayerDay.FAJR, "Tomorrow Fajr is next");
        }
        try { day(new Date(), new int[5]); throw new AssertionError("Bad length accepted"); }
        catch (IllegalArgumentException expected) {}
        try { day(new Date(), new int[] {31,0,0,0,0,0}); throw new AssertionError("Bad range accepted"); }
        catch (IllegalArgumentException expected) {}
    }
}
