package tv.afnon.home;

import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.data.DateComponents;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;
import tv.afnon.core.PrayerComparison;
import tv.afnon.core.PrayerRules;

/** One manual snapshot; no settings writes or schedule replacement. */
class OnlinePrayer {
    static final int DEADLINE_MS = 12_000, MAX_BYTES = 32768;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd-MM-uuuu", Locale.ROOT);
    private static final String[] NAMES = {"Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"};
    final Prefs.City city;
    final PrayerMethod method;
    final Madhab school;
    final LocalDate date;
    private final int[] offsets;
    private volatile HttpsURLConnection connection;
    private volatile boolean cancelled;

    OnlinePrayer(Prefs.City city, PrayerMethod method, Madhab school, int[] offsets, LocalDate date) {
        PrayerRules.INSTANCE.validateOffsets(offsets);
        this.city = city; this.method = method; this.school = school; this.date = date; this.offsets = offsets.clone();
    }

    String day() { return date.format(DAY); }

    URL url() throws IOException {
        return new URL("https://api.aladhan.com/v1/timings/" + day() + "?latitude=" + city.lat + "&longitude=" + city.lng
            + "&method=" + PrayerComparison.INSTANCE.aladhanMethod(method.name()) + "&school=" + (school == Madhab.HANAFI ? 1 : 0)
            + "&latitudeAdjustmentMethod=1&shafaq=general&iso8601=true");
    }

    HttpsURLConnection open() throws IOException { return (HttpsURLConnection) url().openConnection(); }

    synchronized void cancel() {
        if (cancelled) return;
        cancelled = true;
        HttpsURLConnection active = connection;
        // Disconnect can block; cancellation must never block the remote/UI thread.
        if (active != null) new Thread(active::disconnect, "prayer-check-cancel").start();
    }

    private void checkCancelled() throws IOException { if (cancelled) throw new IOException("Online check cancelled"); }

    Result load() throws Exception {
        checkCancelled();
        Timer deadline = new Timer("prayer-check-deadline", true);
        deadline.schedule(new TimerTask() { @Override public void run() { OnlinePrayer.this.cancel(); } }, DEADLINE_MS);
        try {
            connection = open();
            connection.setConnectTimeout(6000); connection.setReadTimeout(6000);
            connection.setInstanceFollowRedirects(false); connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Accept-Encoding", "identity");
            checkCancelled();
            if (connection.getResponseCode() != 200) throw new IOException("Online reference unavailable");
            String type = connection.getContentType();
            if (type == null || !type.split(";", 2)[0].trim().equalsIgnoreCase("application/json")) throw new IOException("Invalid reference content type");
            try (InputStream input = connection.getInputStream(); ByteArrayOutputStream body = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[2048];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    checkCancelled();
                    if (body.size() + count > MAX_BYTES) throw new IOException("Reference response too large");
                    body.write(buffer, 0, count);
                }
                checkCancelled();
                Result result = parse(body.toString(StandardCharsets.UTF_8.name()));
                checkCancelled();
                return result;
            }
        } finally {
            deadline.cancel();
            HttpsURLConnection active = connection; connection = null;
            if (active != null) active.disconnect();
        }
    }

    Result parse(String json) throws Exception {
        JSONObject body = new JSONObject(json), data = body.getJSONObject("data"), meta = data.getJSONObject("meta");
        int id = PrayerComparison.INSTANCE.aladhanMethod(method.name());
        JSONObject authority = meta.getJSONObject("method");
        if (number(body, "code") != 200 || !day().equals(data.getJSONObject("date").getJSONObject("gregorian").get("date"))
            || Math.abs(number(meta, "latitude") - city.lat) > 0.00001 || Math.abs(number(meta, "longitude") - city.lng) > 0.00001
            || number(authority, "id") != id || !(school == Madhab.HANAFI ? "HANAFI" : "STANDARD").equals(meta.get("school"))
            || !(id == 15 ? "NONE" : "MIDDLE_OF_THE_NIGHT").equals(meta.get("latitudeAdjustmentMethod"))
            || id == 15 && !"general".equals(authority.getJSONObject("params").get("shafaq"))) throw new IOException("Mismatched reference response");
        Object zone = meta.get("timezone");
        if (!(zone instanceof String) || !((String) zone).matches("[A-Za-z0-9_+\\-/]{1,80}")) throw new IOException("Invalid reference timezone");
        JSONObject timings = data.getJSONObject("timings");
        double[] reference = new double[6], calculated = new double[6];
        long anchor = date.atTime(12, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
        for (int i = 0; i < 6; i++) {
            Object value = timings.get(NAMES[i]);
            if (!(value instanceof String) || !((String) value).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}")) throw new IOException("Invalid reference time");
            OffsetDateTime time = OffsetDateTime.parse((String) value);
            reference[i] = time.toInstant().toEpochMilli();
            if (time.getYear() < 2000 || Math.abs(time.getOffset().getTotalSeconds()) > 14 * 3600
                || Math.abs(reference[i] - anchor) > 36 * 3600000.0 || i > 0 && reference[i] <= reference[i - 1]
                || i > 0 && i < 5 && !time.toLocalDate().equals(date)) throw new IOException("Invalid reference day");
        }
        Date[] times = PrayerDay.civilTimes(new DateComponents(date.getYear(), date.getMonthValue(), date.getDayOfMonth()),
            new Coordinates(city.lat, city.lng), method, school, offsets);
        for (int i = 0; i < 6; i++) calculated[i] = times[i].getTime();
        return new Result(this, (String) zone, calculated, reference, PrayerComparison.INSTANCE.minutes(calculated, reference));
    }

    private static double number(JSONObject object, String key) throws Exception {
        Object value = object.get(key);
        if (!(value instanceof Number) || !Double.isFinite(((Number) value).doubleValue())) throw new IOException("Invalid reference number");
        return ((Number) value).doubleValue();
    }

    static final class Result {
        final OnlinePrayer source;
        final String timezone;
        final double[] calculated, reference;
        final int[] differences;
        Result(OnlinePrayer source, String timezone, double[] calculated, double[] reference, int[] differences) {
            this.source = source; this.timezone = timezone; this.calculated = calculated; this.reference = reference; this.differences = differences;
        }
    }
}
