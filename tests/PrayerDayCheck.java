package tv.afnon.home;

import com.batoulapps.adhan.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

public final class PrayerDayCheck {
    private static PrayerDay day(Date now, int[] offsets) {
        return new PrayerDay(now, new Coordinates(52.2913, -1.5364),
            CalculationMethod.MUSLIM_WORLD_LEAGUE, Madhab.SHAFI, 0, offsets);
    }
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
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
