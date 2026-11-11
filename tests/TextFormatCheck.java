package tv.afnon.home;

import com.batoulapps.adhan.Madhab;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/** Edge-case output captured before moving formatter literals into shared data. */
public final class TextFormatCheck {
    interface Value { Object get(); }
    private static MessageDigest digest;
    private static void check(String label, Value value) {
        String result;
        try {
            Object out = value.get();
            result = out instanceof String[] ? Arrays.toString((String[]) out) : String.valueOf(out);
        } catch (RuntimeException error) { result = "throws:" + error.getClass().getName(); }
        digest.update((label + "\0" + result + "\n").getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] ignored) throws Exception {
        for (boolean ar : new boolean[] {false, true}) for (boolean eastern : new boolean[] {false, true}) {
            Text t = new Text(ar, eastern);
            t.cityName = "City 7 · 8";
            digest = MessageDigest.getInstance("SHA-256");
            for (String sample : new String[] {"sample 7 · 8", "", null}) {
                check("replaceWith", () -> t.replaceWith(sample));
                check("suhoorEnds", () -> t.suhoorEnds(sample));
                check("apSub", () -> t.apSub(sample));
                check("network", () -> t.sectionNetwork(sample));
                check("place", () -> t.place(sample, sample));
                check("hijriToday", () -> t.hijriToday(sample));
                check("in", () -> t.in(sample));
                for (long minutes : new long[] {-1, 0, 1, 2, 3, 10, 11, 59, 60, 61, 150}) {
                    check("inMinutes", () -> t.inMinutes(minutes, sample));
                    check("apTitle", () -> t.apTitle(sample, minutes, sample));
                }
            }
            check("charSequence", () -> t.apSub(new StringBuilder("1:07 · 8")));
            for (int n : new int[] {Integer.MIN_VALUE, -31, -30, -2, -1, 0, 1, 2, 3, 10, 30, 31, Integer.MAX_VALUE}) {
                check("all", () -> t.all(n));
                check("days", () -> t.days(n));
                check("offsetMinutes", () -> t.offsetMinutes(n));
                check("offsetCount", () -> t.offsetCount(n));
                check("buttonNumber", () -> t.buttonNumber(n));
                check("bearingLabel", () -> t.bearingLabel(n));
                check("facingLine", () -> t.facingLine(n));
            }
            for (boolean flag : new boolean[] {false, true}) {
                check("ampm", () -> t.ampm(flag));
                check("bell", () -> t.bell(flag));
                check("passed", () -> t.passed(flag));
                check("roomButton", () -> t.roomButton(flag));
                Radio.Station station = new Radio.Station();
                station.ar = "إذاعة 7 · 8"; station.en = "Station 7 · 8";
                station.whereAr = "مكان 7 · 8"; station.whereEn = "Place 7 · 8"; station.reciter = flag;
                check("station", () -> t.station(station));
                check("stationWhere", () -> t.stationWhere(station));
                check("stationChoice", () -> t.stationChoice(station));
                check("radioRowHelp", () -> t.radioRowHelp(station));
            }
            for (int state : new int[] {-9, 0, 1, 2, 3, 4, 5, 999}) check("radioStatus", () -> t.radioStatus(state));
            for (int position : new int[] {-1, 0, 1, 2, 9, 10}) check("favNote", () -> t.favNote(position));
            for (Madhab school : new Madhab[] {Madhab.HANAFI, Madhab.SHAFI, null}) check("school", () -> t.school(school));
            for (double angle : new double[] {-720, -360, -22.500001, -22.5, -0.0, 0, 22.499999, 22.5, 44.9, 337.5, 360, 720}) {
                check("octant", () -> t.octant(angle));
                check("relative", () -> t.qiblaRelative(angle));
                check("direction", () -> t.qiblaDirection(angle));
                check("location", () -> t.locationHelp(angle, -angle));
            }
            for (int interval : new int[] {0, 90}) {
                PrayerMethod method = new PrayerMethod();
                method.ar = "طريقة"; method.en = "Method";
                method.parameters.fajrAngle = 19.5; method.parameters.ishaAngle = 18; method.parameters.ishaInterval = interval;
                check("methodName", () -> t.methodName(method));
                check("methodAngles", () -> t.methodAngles(method));
            }
            check("languages", t::languages);
            check("radioDarkHelp", t::radioDarkHelp);
            check("roomRow", t::roomRow);
            StringBuilder hash = new StringBuilder();
            for (byte b : digest.digest()) hash.append(String.format("%02x", b & 255));
            System.out.println(ar + "-" + eastern + ":" + hash);
        }
    }
}
