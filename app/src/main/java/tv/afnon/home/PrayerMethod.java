package tv.afnon.home;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.PrayerAdjustments;

/** Offline presets; names retain saved preferences from the original ten methods. Sources: docs/PRAYER-METHODS.md. */
enum PrayerMethod {
    EGYPTIAN(CalculationMethod.EGYPTIAN),
    UMM_AL_QURA(CalculationMethod.UMM_AL_QURA),
    MUSLIM_WORLD_LEAGUE(CalculationMethod.MUSLIM_WORLD_LEAGUE),
    KARACHI(CalculationMethod.KARACHI),
    DUBAI(CalculationMethod.DUBAI),
    KUWAIT(CalculationMethod.KUWAIT),
    QATAR(CalculationMethod.QATAR),
    NORTH_AMERICA(CalculationMethod.NORTH_AMERICA),
    MOON_SIGHTING_COMMITTEE(CalculationMethod.MOON_SIGHTING_COMMITTEE),
    SINGAPORE(CalculationMethod.SINGAPORE),
    FRANCE(12, 12, 0),
    TURKEY(18, 17, 0),
    RUSSIA(16, 15, 0),
    JAKIM(20, 18, 0),
    KEMENAG(20, 18, 0),
    TUNISIA(18, 18, 0),
    ALGERIA(18, 17, 0),
    MOROCCO(19, 17, 0),
    GULF(19.5, 0, 90);

    private final CalculationMethod original;
    private final double fajr, isha;
    private final int interval;

    PrayerMethod(CalculationMethod original) {
        this.original = original;
        fajr = isha = interval = 0;
    }

    PrayerMethod(double fajr, double isha, int interval) {
        original = null;
        this.fajr = fajr;
        this.isha = isha;
        this.interval = interval;
    }

    CalculationParameters getParameters() {
        if (original != null) return original.getParameters();
        CalculationParameters p = new CalculationParameters(fajr, isha);
        p.ishaInterval = interval;
        // Same Turkey corrections as Batoul Apps' adhan-js; user offsets remain separate.
        if (this == TURKEY) p.methodAdjustments = new PrayerAdjustments(0, -7, 5, 4, 7, 0);
        return p;
    }
}
