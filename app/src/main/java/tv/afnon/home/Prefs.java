package tv.afnon.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

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
        final PrayerMethod method;
        /** The Asr school most of its mosques follow, also picked when the city changes. */
        final Madhab madhab;

        City(String id, String ar, String en, String countryAr, String countryEn, double lat, double lng, PrayerMethod method) {
            this(id, ar, en, countryAr, countryEn, lat, lng, method, Madhab.SHAFI);
        }

        City(String id, String ar, String en, String countryAr, String countryEn, double lat, double lng, PrayerMethod method, Madhab madhab) {
            this.id = id;
            this.ar = ar;
            this.en = en;
            this.countryAr = countryAr;
            this.countryEn = countryEn;
            this.lat = lat;
            this.lng = lng;
            this.method = method;
            this.madhab = madhab;
        }
    }

    static final City[] CITIES = CityCatalog.CITIES;

    static int cityIndex(Context c) {
        String id = of(c).getString("city", "cairo");
        for (int i = 0; i < CITIES.length; i++) if (CITIES[i].id.equals(id)) return i;
        return 0;
    }

    static City city(Context c) {
        return CITIES[cityIndex(c)];
    }

    /** A new city also takes that city's usual method and Asr school; their rows can still override them after. */
    static void setCity(Context c, int index) {
        City city = CITIES[index];
        of(c).edit().putString("city", city.id).putString("method", city.method.name())
            .putString("madhab", city.madhab == Madhab.HANAFI ? "hanafi" : "shafi").apply();
    }

    /**
     * Location's first step: each region's countries (English names, as in {@link #CITIES}), in the order of
     * {@link Text#regions()}. Every country in CITIES must be in exactly one region, or its cities can't be picked.
     */
    static final String[][] REGIONS = CityCatalog.REGIONS;

    /** The region (index into {@link #REGIONS}) a city's country is in; -1 if it is in none. */
    static int region(City city) {
        for (int r = 0; r < REGIONS.length; r++) for (String n : REGIONS[r]) if (n.equals(city.countryEn)) return r;
        return -1;
    }

    static Coordinates where(Context c) {
        City city = city(c);
        return new Coordinates(city.lat, city.lng);
    }

    static final int MAX_PRAYER_OFFSET = 30;

    static int prayerOffset(Context c, int prayer) {
        return Math.max(-MAX_PRAYER_OFFSET, Math.min(MAX_PRAYER_OFFSET, of(c).getInt("prayer_offset_" + prayer, 0)));
    }

    static void setPrayerOffset(Context c, int prayer, int minutes) {
        if (prayer < 0 || prayer >= 6 || Math.abs((long) minutes) > MAX_PRAYER_OFFSET)
            throw new IllegalArgumentException("Invalid prayer offset");
        of(c).edit().putInt("prayer_offset_" + prayer, minutes).apply();
    }

    static int[] prayerOffsets(Context c) {
        int[] offsets = new int[6];
        for (int i = 0; i < offsets.length; i++) offsets[i] = prayerOffset(c, i);
        return offsets;
    }

    // ---------------------------------------------------------------- calculation

    static final PrayerMethod[] METHODS = PrayerMethod.values();

    static PrayerMethod method(Context c) {
        String name = of(c).getString("method", null);
        if (name != null) for (PrayerMethod m : METHODS) if (m.name().equals(name)) return m;
        return city(c).method;
    }

    static void setMethod(Context c, PrayerMethod m) {
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

    /** Say «اقترب موعد صلاة …» with the reminder card or banner (Announce, on by default). */
    static boolean remindVoice(Context c) {
        return of(c).getBoolean("remind_voice", true);
    }

    static void setRemindVoice(Context c, boolean on) {
        of(c).edit().putBoolean("remind_voice", on).apply();
    }

    /** Say «حان الآن موعد صلاة …» when a prayer's time begins with its adhan off (Announce, on by default). */
    static boolean announce(Context c) {
        return of(c).getBoolean("announce", true);
    }

    static void setAnnounce(Context c, boolean on) {
        of(c).edit().putBoolean("announce", on).apply();
    }

    /**
     * When the call screen closes by itself: 1, 5 or 10 minutes after it opens ({@link Tokens#MOTION_CALL_AUTO_DISMISS_MS}
     * by default), or 0 when the adhan ends.
     */
    static int callCloseMinutes(Context c) {
        return of(c).getInt("call_close", (int) (Tokens.MOTION_CALL_AUTO_DISMISS_MS / 60_000));
    }

    static void setCallCloseMinutes(Context c, int minutes) {
        of(c).edit().putInt("call_close", minutes).apply();
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
    static boolean quickLabels(Context c) { return of(c).getBoolean("quick_labels", false); }

    static boolean classicHome(Context c) { return "classic".equals(of(c).getString("home_layout", "two_rows")); }

    static int homeCountdown(Context c) { return HomeLayout.clamp(of(c).getInt("home_countdown", 0), HomeLayout.HIDDEN); }
    static int homeSide(Context c) { return HomeLayout.clamp(of(c).getInt("home_side", 0), 2); }
    static int homeBar(Context c) { return HomeLayout.clamp(of(c).getInt("home_bar", 0), 1); }
    static int homePrayers(Context c) { return HomeLayout.clamp(of(c).getInt("home_prayers", 0), HomeLayout.NONE); }

    static String key(Context c) {
        SharedPreferences p = of(c);
        return p.getString("locale", "") + "|" + p.getString("digits", "") + "|" + clock24(c) + "|" + cityIndex(c)
            + "|" + method(c) + "|" + madhab(c) + "|" + java.util.Arrays.toString(prayerOffsets(c)) + "|" + hijriOffset(c) + "|" + p.getInt("facing", -1) + "|" + Appearance.key(c) + "|" + Appearance.reduced(c) + "|" + p.getBoolean("hide_system_apps", false) + "|" + p.getString("dock_mode", "hidden") + "|" + p.getString("dock_position", "bottom") + "|" + quickLabels(c) + "|" + classicHome(c)
            + "|" + homeCountdown(c) + "|" + homeSide(c) + "|" + homeBar(c) + "|" + homePrayers(c) + "|" + Appearance.cycleMinutes(c);
    }
}
