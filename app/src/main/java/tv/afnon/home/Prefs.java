package tv.afnon.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Madhab;

/**
 * Afnon's own settings (SharedPreferences "afnon"), read by every screen and the screensaver. Language and digits
 * live in {@link Screen#loadText}; the room direction in {@link Screen#facing()}.
 */
final class Prefs {
    private Prefs() {}

    static SharedPreferences of(Context c) {
        return c.getSharedPreferences("afnon", Context.MODE_PRIVATE);
    }

    // ---------------------------------------------------------------- location

    static final class City {
        final String id, ar, en, countryAr, countryEn;
        final double lat, lng;
        /** The method that city's mosques follow; picked for it when the city changes. */
        final CalculationMethod method;

        City(String id, String ar, String en, String countryAr, String countryEn, double lat, double lng, CalculationMethod method) {
            this.id = id;
            this.ar = ar;
            this.en = en;
            this.countryAr = countryAr;
            this.countryEn = countryEn;
            this.lat = lat;
            this.lng = lng;
            this.method = method;
        }
    }

    private static final CalculationMethod EG = CalculationMethod.EGYPTIAN;
    static final City[] CITIES = {
        new City("cairo", "القاهرة", "Cairo", "مصر", "Egypt", 30.0444, 31.2357, EG),
        new City("giza", "الجيزة", "Giza", "مصر", "Egypt", 30.0131, 31.2089, EG),
        new City("alexandria", "الإسكندرية", "Alexandria", "مصر", "Egypt", 31.2001, 29.9187, EG),
        new City("mansoura", "المنصورة", "Mansoura", "مصر", "Egypt", 31.0409, 31.3785, EG),
        new City("tanta", "طنطا", "Tanta", "مصر", "Egypt", 30.7865, 31.0004, EG),
        new City("portsaid", "بورسعيد", "Port Said", "مصر", "Egypt", 31.2653, 32.3019, EG),
        new City("suez", "السويس", "Suez", "مصر", "Egypt", 29.9668, 32.5498, EG),
        new City("asyut", "أسيوط", "Asyut", "مصر", "Egypt", 27.1783, 31.1859, EG),
        new City("luxor", "الأقصر", "Luxor", "مصر", "Egypt", 25.6872, 32.6396, EG),
        new City("aswan", "أسوان", "Aswan", "مصر", "Egypt", 24.0889, 32.8998, EG),
        new City("makkah", "مكة المكرمة", "Makkah", "السعودية", "Saudi Arabia", 21.3891, 39.8579, CalculationMethod.UMM_AL_QURA),
        new City("madinah", "المدينة المنورة", "Madinah", "السعودية", "Saudi Arabia", 24.5247, 39.5692, CalculationMethod.UMM_AL_QURA),
        new City("riyadh", "الرياض", "Riyadh", "السعودية", "Saudi Arabia", 24.7136, 46.6753, CalculationMethod.UMM_AL_QURA),
        new City("dubai", "دبي", "Dubai", "الإمارات", "UAE", 25.2048, 55.2708, CalculationMethod.DUBAI),
        new City("kuwait", "الكويت", "Kuwait City", "الكويت", "Kuwait", 29.3759, 47.9774, CalculationMethod.KUWAIT),
        new City("doha", "الدوحة", "Doha", "قطر", "Qatar", 25.2854, 51.5310, CalculationMethod.QATAR),
        new City("amman", "عمّان", "Amman", "الأردن", "Jordan", 31.9539, 35.9106, CalculationMethod.MUSLIM_WORLD_LEAGUE),
    };

    static int cityIndex(Context c) {
        String id = of(c).getString("city", "cairo");
        for (int i = 0; i < CITIES.length; i++) if (CITIES[i].id.equals(id)) return i;
        return 0;
    }

    static City city(Context c) {
        return CITIES[cityIndex(c)];
    }

    /** A new city also takes that city's usual method; the method row can still override it afterwards. */
    static void setCity(Context c, int index) {
        of(c).edit().putString("city", CITIES[index].id).putString("method", CITIES[index].method.name()).apply();
    }

    static Coordinates where(Context c) {
        City city = city(c);
        return new Coordinates(city.lat, city.lng);
    }

    // ---------------------------------------------------------------- calculation

    static final CalculationMethod[] METHODS = {
        CalculationMethod.EGYPTIAN, CalculationMethod.UMM_AL_QURA, CalculationMethod.MUSLIM_WORLD_LEAGUE,
        CalculationMethod.KARACHI, CalculationMethod.DUBAI, CalculationMethod.KUWAIT, CalculationMethod.QATAR,
        CalculationMethod.NORTH_AMERICA, CalculationMethod.MOON_SIGHTING_COMMITTEE, CalculationMethod.SINGAPORE};

    static CalculationMethod method(Context c) {
        String name = of(c).getString("method", null);
        if (name != null) for (CalculationMethod m : METHODS) if (m.name().equals(name)) return m;
        return city(c).method;
    }

    static void setMethod(Context c, CalculationMethod m) {
        of(c).edit().putString("method", m.name()).apply();
    }

    static Madhab madhab(Context c) {
        return "hanafi".equals(of(c).getString("madhab", "shafi")) ? Madhab.HANAFI : Madhab.SHAFI;
    }

    static void setMadhab(Context c, Madhab m) {
        of(c).edit().putString("madhab", m == Madhab.HANAFI ? "hanafi" : "shafi").apply();
    }

    /** Days added to the computed Hijri date, for when the local moon sighting differs: −2 to +2. */
    static int hijriOffset(Context c) {
        return of(c).getInt("hijri_offset", 0);
    }

    static void setHijriOffset(Context c, int days) {
        of(c).edit().putInt("hijri_offset", Math.max(-2, Math.min(2, days))).apply();
    }

    // ---------------------------------------------------------------- reminders

    /** Minutes before each prayer to show the reminder; 0 is off. */
    static int remindMinutes(Context c) {
        return of(c).getInt("remind", 15);
    }

    static void setRemindMinutes(Context c, int minutes) {
        of(c).edit().putInt("remind", minutes).apply();
    }

    /** The adhan as a whole; each prayer's bell (Prayer day) still applies under it. */
    static boolean adhan(Context c) {
        return of(c).getBoolean("adhan", true);
    }

    static void setAdhan(Context c, boolean on) {
        of(c).edit().putBoolean("adhan", on).apply();
    }

    /** Each prayer's own adhan bell (Prayer day); on by default except Sunrise, which has no adhan. */
    static boolean bell(Context c, int prayer) {
        return of(c).getBoolean("bell_" + prayer, prayer != PrayerDay.SUNRISE);
    }

    /** The recording (Adhans id) for every prayer but Fajr, and the one for Fajr, whose adhan has its own line. */
    static String voice(Context c, boolean fajr) {
        return fajr ? of(c).getString("voice_fajr", "chebae_fajr") : of(c).getString("voice", "alafasy");
    }

    static void setVoice(Context c, boolean fajr, String id) {
        of(c).edit().putString(fajr ? "voice_fajr" : "voice", id).apply();
    }

    // ---------------------------------------------------------------- radio

    /** After the adhan paused the radio: true resumes it (the default), false leaves it off. */
    static boolean radioResume(Context c) {
        return of(c).getBoolean("radio_resume", true);
    }

    static void setRadioResume(Context c, boolean on) {
        of(c).edit().putBoolean("radio_resume", on).apply();
    }

    /** Minutes of screensaver before the screen goes dark while the radio plays: 0 at once (the default), -1 never. */
    static int radioScreenOff(Context c) {
        return of(c).getInt("radio_dark", 0);
    }

    static void setRadioScreenOff(Context c, int minutes) {
        of(c).edit().putInt("radio_dark", minutes).apply();
    }

    // ---------------------------------------------------------------- display

    /** "system" (the TV's own Date & time setting, the default), "12" or "24". */
    static String clock(Context c) {
        return of(c).getString("clock", "system");
    }

    static boolean clock24(Context c) {
        String s = clock(c);
        return "system".equals(s) ? DateFormat.is24HourFormat(c) : "24".equals(s);
    }

    /** Everything a built screen depends on; a screen whose key changed while it was away rebuilds. */
    static String key(Context c) {
        SharedPreferences p = of(c);
        return p.getString("locale", "") + "|" + p.getString("digits", "") + "|" + clock24(c) + "|" + cityIndex(c)
            + "|" + method(c) + "|" + madhab(c) + "|" + hijriOffset(c) + "|" + p.getInt("facing", -1);
    }
}
