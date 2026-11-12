package tv.afnon.home;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.batoulapps.adhan.Madhab;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;
import tv.afnon.core.PrayerComparison;

/** Real Android JSON/time/views; fake HTTPS for negative cases, optional real TLS separately. */
final class OnlinePrayerDeviceCheck {
    private static final LocalDate DATE = LocalDate.of(2026, 11, 12);
    private static final String[] NAMES = {"Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"};
    private static final Prefs.City CAIRO = Prefs.CITIES[0];

    static void run(Instrumentation test, boolean network) {
        Bundle result = new Bundle(); int code = Activity.RESULT_CANCELED;
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Africa/Cairo"));
            if (network) {
                OnlinePrayer.Result live = request().load();
                result.putString("stream", "Actual Android verified HTTPS: " + live.source.day() + " " + live.timezone
                    + " differences=" + Arrays.toString(live.differences) + ".\n");
            } else {
                rules(); transport(); ui(test);
                result.putString("stream", "Native online comparison passed: strict JSON/ISO/identity, 19 authorities, raw civil Fajr/offsets, bounded HTTPS/cancellation/deadline, bilingual real views/focus/lifecycle. HTTPS negative cases are mocked.\n");
            }
            code = Activity.RESULT_OK;
        } catch (Throwable error) { result.putString("stream", android.util.Log.getStackTraceString(error)); }
        finally { TimeZone.setDefault(previous); }
        test.finish(code, result);
    }

    static OnlinePrayer request() { return new OnlinePrayer(CAIRO, PrayerMethod.EGYPTIAN, Madhab.SHAFI, new int[6], DATE); }

    static JSONObject fixture() throws Exception {
        JSONObject times = new JSONObject();
        String[] clock = {"04:48", "06:18", "11:39", "14:40", "17:00", "18:20"};
        for (int i = 0; i < 6; i++) times.put(NAMES[i], "2026-11-12T" + clock[i] + ":00+02:00");
        return new JSONObject().put("code", 200).put("data", new JSONObject().put("timings", times)
            .put("date", new JSONObject().put("gregorian", new JSONObject().put("date", "12-11-2026")))
            .put("meta", new JSONObject().put("latitude", CAIRO.lat).put("longitude", CAIRO.lng).put("timezone", "Africa/Cairo")
                .put("method", new JSONObject().put("id", 5)).put("school", "STANDARD").put("latitudeAdjustmentMethod", "MIDDLE_OF_THE_NIGHT")));
    }

    private interface Change { void apply(JSONObject body) throws Exception; }

    private static void rules() throws Exception {
        OnlinePrayer.Result result = request().parse(fixture().toString());
        check(Arrays.equals(result.differences, new int[] {0, 1, -1, 1, 0, 0}), "Native Cairo signed minute comparison");
        check(result.calculated[0] == 1794451680000.0, "Comparison uses today's civil Fajr after Isha");
        int[] offsets = {5, 0, -3, 0, 0, 0};
        OnlinePrayer shifted = new OnlinePrayer(CAIRO, PrayerMethod.EGYPTIAN, Madhab.SHAFI, offsets, DATE); offsets[0] = 30;
        check(Arrays.equals(shifted.parse(fixture().toString()).differences, new int[] {-5, 1, 2, 1, 0, 0}), "Retain offsets and snapshot isolation");
        for (Change change : new Change[] {
            b -> b.put("code", "200"), b -> b.getJSONObject("data").getJSONObject("date").getJSONObject("gregorian").put("date", "13-11-2026"),
            b -> b.getJSONObject("data").getJSONObject("meta").put("latitude", "30.0444"),
            b -> b.getJSONObject("data").getJSONObject("meta").put("longitude", CAIRO.lng + 0.01),
            b -> b.getJSONObject("data").getJSONObject("meta").getJSONObject("method").put("id", 5.5),
            b -> b.getJSONObject("data").getJSONObject("meta").put("school", "HANAFI"),
            b -> b.getJSONObject("data").getJSONObject("meta").put("latitudeAdjustmentMethod", "ANGLE_BASED"),
            b -> b.getJSONObject("data").getJSONObject("meta").put("timezone", "<img src=x>"),
            b -> b.getJSONObject("data").getJSONObject("timings").remove("Fajr"),
            b -> b.getJSONObject("data").getJSONObject("timings").put("Asr", "2026-02-30T14:00:00+02:00"),
            b -> b.getJSONObject("data").getJSONObject("timings").put("Asr", "2026-11-12T25:00:00+02:00"),
            b -> b.getJSONObject("data").getJSONObject("timings").put("Asr", "2026-11-12T14:00:00+14:59"),
            b -> b.getJSONObject("data").getJSONObject("timings").put("Asr", "2026-11-13T14:00:00+02:00"),
        }) {
            JSONObject invalid = fixture(); change.apply(invalid); rejects(() -> request().parse(invalid.toString()));
        }
        for (PrayerMethod method : PrayerMethod.values()) {
            JSONObject body = fixture(), meta = body.getJSONObject("data").getJSONObject("meta");
            int id = PrayerComparison.INSTANCE.aladhanMethod(method.name());
            meta.put("method", new JSONObject().put("id", id).put("params", new JSONObject().put("shafaq", "general")));
            meta.put("school", "HANAFI").put("latitudeAdjustmentMethod", id == 15 ? "NONE" : "MIDDLE_OF_THE_NIGHT");
            body.getJSONObject("data").getJSONObject("timings").put("Isha", "2026-11-13T00:15:00+02:00");
            OnlinePrayer check = new OnlinePrayer(CAIRO, method, Madhab.HANAFI, new int[6], DATE);
            check(check.parse(body.toString()).reference.length == 6, "Method " + method + " and next-day Isha");
            check(check.url().toString().contains("&method=" + id + "&school=1&"), "Request method/school " + method);
        }
    }

    private interface Checked { void run() throws Exception; }
    private static void rejects(Checked action) throws Exception {
        try { action.run(); } catch (Exception expected) { return; }
        throw new AssertionError("Invalid reference was accepted");
    }

    private static void transport() throws Exception {
        Fake connection = new Fake(fixture().toString());
        OnlinePrayer check = fake(connection);
        check(check.load().reference.length == 6 && connection.disconnected, "Bounded request completes and closes");
        check(!connection.getInstanceFollowRedirects() && !connection.getUseCaches(), "No redirects or response cache");
        check(connection.getConnectTimeout() > 0 && connection.getReadTimeout() > 0, "Socket timeouts configured");
        check(connection.getRequestProperty("Accept-Encoding").equals("identity"), "Raw body limit uses identity encoding");
        for (String kind : new String[] {"redirect", "content", "large", "json", "tls"}) {
            Fake bad = new Fake(kind.equals("large") ? new String(new char[32769]).replace('\0', ' ') : kind.equals("json") ? "bad json" : fixture().toString());
            if (kind.equals("redirect")) bad.status = 302;
            if (kind.equals("content")) bad.type = "text/html";
            if (kind.equals("tls")) bad.failure = true;
            rejects(() -> fake(bad).load()); check(bad.disconnected, "Close after " + kind);
        }
        Fake before = new Fake(fixture().toString()); OnlinePrayer cancelled = fake(before); cancelled.cancel();
        rejects(cancelled::load); check(!before.opened, "Cancellation before load sends no request");
        Fake blocked = new Fake(""); blocked.block = true; OnlinePrayer pending = fake(blocked);
        Throwable[] failure = new Throwable[1];
        Thread worker = new Thread(() -> { try { pending.load(); } catch (Throwable error) { failure[0] = error; } }); worker.start();
        check(blocked.reading.await(3, TimeUnit.SECONDS), "Request reached blocked read");
        pending.cancel(); worker.join(3000);
        check(!worker.isAlive() && failure[0] != null && blocked.disconnected, "Cancellation aborts real worker boundary");
        Fake expired = new Fake(""); expired.block = true;
        long start = SystemClock.elapsedRealtime(); rejects(() -> fake(expired).load());
        long elapsed = SystemClock.elapsedRealtime() - start;
        check(expired.disconnected && elapsed >= 11500 && elapsed < 20000, "One-shot absolute deadline aborts blocked body");
    }

    private static OnlinePrayer fake(Fake connection) {
        return new OnlinePrayer(CAIRO, PrayerMethod.EGYPTIAN, Madhab.SHAFI, new int[6], DATE) {
            @Override HttpsURLConnection open() { connection.opened = true; return connection; }
        };
    }

    private static void ui(Instrumentation test) throws Exception {
        test.setInTouchMode(false);
        SharedPreferences prefs = Prefs.of(test.getTargetContext()); Map<String, ?> saved = prefs.getAll();
        OnlinePrayerActivity[] active = new OnlinePrayerActivity[1];
        SettingsActivity[] parent = new SettingsActivity[1];
        try {
            for (String language : new String[] {"ar", "en"}) {
                check(prefs.edit().putString("locale", language).putString("clock", "24").commit(), "Set UI language/clock");
                parent[0] = (SettingsActivity) test.startActivitySync(new Intent(test.getTargetContext(), SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                test.waitForIdleSync(); UiContractDeviceCheck.waitFocus(parent[0]);
                if (parent[0].getCurrentFocus() == null || !(parent[0].getCurrentFocus().getTag() instanceof SettingsActivity.Row)) UiContractDeviceCheck.key(test, parent[0], KeyEvent.KEYCODE_DPAD_CENTER);
                for (int i = 0; i < 12 && parent[0].getCurrentFocus() != binding(parent[0].root); i++) UiContractDeviceCheck.key(test, parent[0], KeyEvent.KEYCODE_DPAD_DOWN);
                check(parent[0].getCurrentFocus() == binding(parent[0].root), "Remote reaches the actual prayer.online row");
                Instrumentation.ActivityMonitor monitor = test.addMonitor(OnlinePrayerActivity.class.getName(), null, false);
                try {
                    UiContractDeviceCheck.key(test, parent[0], KeyEvent.KEYCODE_DPAD_CENTER);
                    active[0] = (OnlinePrayerActivity) test.waitForMonitorWithTimeout(monitor, 5000);
                } finally { test.removeMonitor(monitor); }
                check(active[0] != null, "Settings action opens the native comparison");
                OnlinePrayerActivity view = active[0]; test.waitForIdleSync(); UiContractDeviceCheck.waitFocus(view);
                OnlinePrayer source = request(); OnlinePrayer.Result value = source.parse(fixture().toString());
                UiContractDeviceCheck.onMain(test, () -> {
                    OnlinePrayer old = (OnlinePrayer) UiContractDeviceCheck.field(view, "request");
                    set(view, "request", source); if (old != null) old.cancel();
                    deliver(view, source, value);
                    check(((LinearLayout) UiContractDeviceCheck.field(view, "table")).getChildCount() == 7, "Six rows plus headings");
                    check(view.getCurrentFocus() == UiContractDeviceCheck.field(view, "back"), "Completion preserves Back focus");
                    check(cell(view, 4, 1).equals(view.t.d("17:00")), "Comparison honors saved 24-hour clock");
                    check(prefs.edit().putString("clock", "12").commit(), "Set 12-hour clock");
                    set(view, "request", source); deliver(view, source, value);
                    check(cell(view, 4, 1).equals(view.t.d("5:00") + " " + view.t.ampm(false)), "Comparison honors saved 12-hour clock");
                    check(prefs.edit().putString("clock", "24").commit(), "Restore 24-hour fixture");
                    set(view, "request", source); deliver(view, source, value);
                });
                test.waitForIdleSync();
                UiContractDeviceCheck.key(test, view, KeyEvent.KEYCODE_DPAD_DOWN);
                check(view.getCurrentFocus() == UiContractDeviceCheck.field(view, "retry"), "Down reaches retry");
                UiContractDeviceCheck.key(test, view, KeyEvent.KEYCODE_DPAD_UP);
                check(view.getCurrentFocus() == UiContractDeviceCheck.field(view, "back"), "Up returns to Back");
                UiContractDeviceCheck.onMain(test, () -> bounds(view.root));
                Bitmap image = test.getUiAutomation().takeScreenshot();
                try (FileOutputStream output = new FileOutputStream(test.getTargetContext().getCacheDir() + "/online-" + language + ".png")) { image.compress(Bitmap.CompressFormat.PNG, 100, output); }
                image.recycle();
                OnlinePrayer paused = request();
                UiContractDeviceCheck.onMain(test, () -> {
                    OnlinePrayer pending = request(); set(view, "request", pending); deliver(view, source, null);
                    check(UiContractDeviceCheck.field(view, "result") == value, "Stale callback preserves current request/report");
                    deliver(view, pending, null);
                    check(((TextView) UiContractDeviceCheck.field(view, "status")).getText().toString().equals(view.t.onlineUnavailable()), "Retryable localized failure");
                    for (int key : new int[] {KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_CENTER}) {
                        view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, key)); view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, key));
                    }
                    OnlinePrayer retried = (OnlinePrayer) UiContractDeviceCheck.field(view, "request");
                    check(retried != null, "Remote retry starts a fresh manual request");
                    set(view, "request", paused); retried.cancel();
                });
                test.getTargetContext().startActivity(new Intent(test.getContext(), UiContractAppFixture.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                long until = SystemClock.uptimeMillis() + 5000;
                while (!foreground(test, "tv.afnon.home.test") && SystemClock.uptimeMillis() < until) SystemClock.sleep(25);
                check(foreground(test, "tv.afnon.home.test"), "Fixture interrupts comparison in a separate process");
                test.waitForIdleSync();
                UiContractDeviceCheck.onMain(test, () -> {
                    deliver(view, paused, value);
                    check(UiContractDeviceCheck.field(view, "result") == null && UiContractDeviceCheck.field(view, "request") == null, "Paused request cannot restore a view");
                });
                test.getUiAutomation().waitForIdle(250, 3000);
                try (InputStream input = new android.os.ParcelFileDescriptor.AutoCloseInputStream(
                    test.getUiAutomation().executeShellCommand("input keyevent KEYCODE_BACK"))) {
                    while (input.read() != -1) { /* Drain the bounded shell command before checking return focus. */ }
                }
                test.waitForIdleSync(); UiContractDeviceCheck.waitFocus(view);
                UiContractDeviceCheck.onMain(test, () -> {
                    check(UiContractDeviceCheck.field(view, "request") == null, "Resume does not automatically retry");
                });
                UiContractDeviceCheck.key(test, view, KeyEvent.KEYCODE_BACK); test.waitForIdleSync();
                check(view.isFinishing() || view.isDestroyed(), "Back closes comparison"); active[0] = null;
                UiContractDeviceCheck.waitFocus(parent[0]);
                check(parent[0].getCurrentFocus() == binding(parent[0].root), "Back restores prayer.online focus");
                test.runOnMainSync(() -> parent[0].finish()); parent[0] = null;
            }
        } finally {
            if (active[0] != null) test.runOnMainSync(() -> active[0].finish());
            if (parent[0] != null) test.runOnMainSync(() -> parent[0].finish());
            SharedPreferences.Editor restore = prefs.edit();
            for (String key : new String[] {"locale", "clock"}) {
                if (saved.containsKey(key)) restore.putString(key, (String) saved.get(key)); else restore.remove(key);
            }
            check(restore.commit() && prefs.getAll().equals(saved), "UI check preserves all preferences");
        }
    }

    private static boolean foreground(Instrumentation test, String pkg) {
        android.view.accessibility.AccessibilityNodeInfo node = test.getUiAutomation().getRootInActiveWindow();
        if (node == null) return false;
        boolean matches = pkg.contentEquals(node.getPackageName()); node.recycle(); return matches;
    }

    private static View binding(View view) {
        if (view.getTag() instanceof SettingsActivity.Row && ((SettingsActivity.Row) view.getTag()).contract.getId().equals("prayer.online")) return view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            View found = binding(((ViewGroup) view).getChildAt(i)); if (found != null) return found;
        }
        return null;
    }

    private static String cell(OnlinePrayerActivity view, int prayer, int column) {
        LinearLayout table = (LinearLayout) UiContractDeviceCheck.field(view, "table");
        return ((TextView) ((LinearLayout) table.getChildAt(prayer + 1)).getChildAt(column)).getText().toString();
    }

    private static void bounds(View view) {
        if (view.getVisibility() != View.VISIBLE) return;
        if (view instanceof TextView && ((TextView) view).length() > 0) {
            TextView text = (TextView) view;
            check(text.getLayout() != null && text.getLayout().getHeight() + text.getPaddingTop() + text.getPaddingBottom() <= text.getHeight(), "Text height fits: " + text.getText());
            int[] at = new int[2]; view.getLocationOnScreen(at);
            check(at[0] >= 0 && at[1] >= 0 && at[0] + view.getWidth() <= view.getRootView().getWidth()
                && at[1] + view.getHeight() <= view.getRootView().getHeight(), "Text stays on screen: " + text.getText());
        }
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) bounds(((ViewGroup) view).getChildAt(i));
    }

    static void set(Object target, String name, Object value) {
        try { java.lang.reflect.Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value); }
        catch (Exception error) { throw new AssertionError(error); }
    }
    static void deliver(OnlinePrayerActivity target, OnlinePrayer request, OnlinePrayer.Result value) {
        try {
            java.lang.reflect.Method method = OnlinePrayerActivity.class.getDeclaredMethod("complete", OnlinePrayer.class, OnlinePrayer.Result.class);
            method.setAccessible(true); method.invoke(target, request, value);
        } catch (Exception error) { throw new AssertionError(error); }
    }

    private static final class Fake extends HttpsURLConnection {
        final byte[] bytes; int status = 200; String type = "application/json";
        boolean opened, failure, block; volatile boolean disconnected;
        final CountDownLatch reading = new CountDownLatch(1), closed = new CountDownLatch(1);
        Fake(String body) throws Exception { super(new URL("https://api.aladhan.com/")); bytes = body.getBytes("UTF-8"); }
        @Override public int getResponseCode() throws IOException { if (failure) throw new javax.net.ssl.SSLHandshakeException("Fixture certificate failure"); return status; }
        @Override public String getContentType() { return type; }
        @Override public InputStream getInputStream() {
            if (!block) return new ByteArrayInputStream(bytes);
            return new InputStream() { @Override public int read() throws IOException {
                reading.countDown(); try { closed.await(); } catch (InterruptedException error) { Thread.currentThread().interrupt(); }
                throw new IOException("Connection closed");
            }};
        }
        @Override public void disconnect() { disconnected = true; closed.countDown(); }
        @Override public boolean usingProxy() { return false; }
        @Override public void connect() {}
        @Override public String getCipherSuite() { return "fixture"; }
        @Override public Certificate[] getLocalCertificates() { return null; }
        @Override public Certificate[] getServerCertificates() { return new Certificate[0]; }
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
