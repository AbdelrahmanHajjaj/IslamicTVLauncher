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
    private static final CalculationMethod MWL = CalculationMethod.MUSLIM_WORLD_LEAGUE;
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
        new City("jeddah", "جدة", "Jeddah", "السعودية", "Saudi Arabia", 21.5292, 39.1611, CalculationMethod.UMM_AL_QURA),
        new City("dammam", "الدمام", "Dammam", "السعودية", "Saudi Arabia", 26.4333, 50.1000, CalculationMethod.UMM_AL_QURA),
        new City("taif", "الطائف", "Taif", "السعودية", "Saudi Arabia", 21.2667, 40.4167, CalculationMethod.UMM_AL_QURA),
        new City("tabuk", "تبوك", "Tabuk", "السعودية", "Saudi Arabia", 28.3972, 36.5789, CalculationMethod.UMM_AL_QURA),
        new City("abha", "أبها", "Abha", "السعودية", "Saudi Arabia", 18.2167, 42.5000, CalculationMethod.UMM_AL_QURA),
        new City("dubai", "دبي", "Dubai", "الإمارات", "UAE", 25.2048, 55.2708, CalculationMethod.DUBAI),
        new City("abudhabi", "أبو ظبي", "Abu Dhabi", "الإمارات", "UAE", 24.4511, 54.3969, CalculationMethod.DUBAI),
        new City("sharjah", "الشارقة", "Sharjah", "الإمارات", "UAE", 25.3375, 55.4119, CalculationMethod.DUBAI),
        new City("alain", "العين", "Al Ain", "الإمارات", "UAE", 24.2075, 55.7447, CalculationMethod.DUBAI),
        new City("kuwait", "الكويت", "Kuwait City", "الكويت", "Kuwait", 29.3759, 47.9774, CalculationMethod.KUWAIT),
        new City("doha", "الدوحة", "Doha", "قطر", "Qatar", 25.2854, 51.5310, CalculationMethod.QATAR),
        new City("amman", "عمّان", "Amman", "الأردن", "Jordan", 31.9539, 35.9106, MWL),
        new City("irbid", "إربد", "Irbid", "الأردن", "Jordan", 32.5556, 35.8500, MWL),
        new City("zarqa", "الزرقاء", "Zarqa", "الأردن", "Jordan", 32.0833, 36.1000, MWL),
        new City("aqaba", "العقبة", "Aqaba", "الأردن", "Jordan", 29.5322, 35.0061, MWL),
        new City("jerusalem", "القدس", "Jerusalem", "فلسطين", "Palestine", 31.7789, 35.2256, MWL),
        new City("ramallah", "رام الله", "Ramallah", "فلسطين", "Palestine", 31.8969, 35.2017, MWL),
        new City("nablus", "نابلس", "Nablus", "فلسطين", "Palestine", 32.2211, 35.2608, MWL),
        new City("hebron", "الخليل", "Hebron", "فلسطين", "Palestine", 31.5286, 35.0969, MWL),
        new City("bethlehem", "بيت لحم", "Bethlehem", "فلسطين", "Palestine", 31.7044, 35.2064, MWL),
        new City("jenin", "جنين", "Jenin", "فلسطين", "Palestine", 32.4611, 35.3000, MWL),
        new City("tulkarm", "طولكرم", "Tulkarm", "فلسطين", "Palestine", 32.3111, 35.0308, MWL),
        new City("qalqilya", "قلقيلية", "Qalqilya", "فلسطين", "Palestine", 32.1904, 34.9685, MWL),
        new City("salfit", "سلفيت", "Salfit", "فلسطين", "Palestine", 32.0819, 35.1822, MWL),
        new City("tubas", "طوباس", "Tubas", "فلسطين", "Palestine", 32.3222, 35.3686, MWL),
        new City("jericho", "أريحا", "Jericho", "فلسطين", "Palestine", 31.8561, 35.4600, MWL),
        new City("gaza", "غزة", "Gaza", "فلسطين", "Palestine", 31.5075, 34.4597, MWL),
        new City("jabalia", "جباليا", "Jabalia", "فلسطين", "Palestine", 31.5281, 34.4831, MWL),
        new City("deiralbalah", "دير البلح", "Deir al-Balah", "فلسطين", "Palestine", 31.4167, 34.3500, MWL),
        new City("khanyunis", "خان يونس", "Khan Yunis", "فلسطين", "Palestine", 31.3444, 34.3031, MWL),
        new City("rafah", "رفح", "Rafah", "فلسطين", "Palestine", 31.2725, 34.2586, MWL),
        new City("beirut", "بيروت", "Beirut", "لبنان", "Lebanon", 33.8869, 35.5131, MWL),
        new City("tripoli", "طرابلس", "Tripoli", "لبنان", "Lebanon", 34.4367, 35.8344, MWL),
        new City("sidon", "صيدا", "Sidon", "لبنان", "Lebanon", 33.5606, 35.3758, MWL),
        new City("tyre", "صور", "Tyre", "لبنان", "Lebanon", 33.2667, 35.2000, MWL),
        new City("baalbek", "بعلبك", "Baalbek", "لبنان", "Lebanon", 34.0061, 36.2086, MWL),
        new City("damascus", "دمشق", "Damascus", "سوريا", "Syria", 33.5131, 36.2919, MWL),
        new City("aleppo", "حلب", "Aleppo", "سوريا", "Syria", 36.2000, 37.1600, MWL),
        new City("homs", "حمص", "Homs", "سوريا", "Syria", 34.7333, 36.7167, MWL),
        new City("hama", "حماة", "Hama", "سوريا", "Syria", 35.1350, 36.7500, MWL),
        new City("latakia", "اللاذقية", "Latakia", "سوريا", "Syria", 35.5200, 35.7781, MWL),
        new City("deirezzor", "دير الزور", "Deir ez-Zor", "سوريا", "Syria", 35.3333, 40.1500, MWL),
        new City("baghdad", "بغداد", "Baghdad", "العراق", "Iraq", 33.3153, 44.3661, MWL),
        new City("basra", "البصرة", "Basra", "العراق", "Iraq", 30.5150, 47.8100, MWL),
        new City("mosul", "الموصل", "Mosul", "العراق", "Iraq", 36.3417, 43.1292, MWL),
        new City("erbil", "أربيل", "Erbil", "العراق", "Iraq", 36.1911, 44.0094, MWL),
        new City("najaf", "النجف", "Najaf", "العراق", "Iraq", 32.0290, 44.3396, MWL),
        new City("karbala", "كربلاء", "Karbala", "العراق", "Iraq", 32.6167, 44.0333, MWL),
        new City("kirkuk", "كركوك", "Kirkuk", "العراق", "Iraq", 35.4667, 44.4000, MWL),
        new City("sulaymaniyah", "السليمانية", "Sulaymaniyah", "العراق", "Iraq", 35.5500, 45.4333, MWL),
        new City("manama", "المنامة", "Manama", "البحرين", "Bahrain", 26.2167, 50.5833, MWL),
        new City("muharraq", "المحرق", "Muharraq", "البحرين", "Bahrain", 26.2533, 50.6083, MWL),
        new City("muscat", "مسقط", "Muscat", "عُمان", "Oman", 23.6139, 58.5922, MWL),
        new City("salalah", "صلالة", "Salalah", "عُمان", "Oman", 17.0197, 54.0897, MWL),
        new City("sohar", "صحار", "Sohar", "عُمان", "Oman", 24.3420, 56.7299, MWL),
        new City("nizwa", "نزوى", "Nizwa", "عُمان", "Oman", 22.9333, 57.5333, MWL),
        new City("sanaa", "صنعاء", "Sanaa", "اليمن", "Yemen", 15.3500, 44.2000, MWL),
        new City("aden", "عدن", "Aden", "اليمن", "Yemen", 12.8000, 45.0333, MWL),
        new City("taiz", "تعز", "Taiz", "اليمن", "Yemen", 13.5789, 44.0219, MWL),
        new City("hodeidah", "الحديدة", "Hodeidah", "اليمن", "Yemen", 14.8022, 42.9511, MWL),
        new City("mukalla", "المكلا", "Mukalla", "اليمن", "Yemen", 14.5333, 49.1333, MWL),
        new City("newyork", "نيويورك", "New York", "الولايات المتحدة", "United States", 40.7128, -74.0060, CalculationMethod.NORTH_AMERICA),
        new City("chicago", "شيكاغو", "Chicago", "الولايات المتحدة", "United States", 41.8781, -87.6298, CalculationMethod.NORTH_AMERICA),
        new City("dallas", "دالاس", "Dallas", "الولايات المتحدة", "United States", 32.7767, -96.7970, CalculationMethod.NORTH_AMERICA),
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
