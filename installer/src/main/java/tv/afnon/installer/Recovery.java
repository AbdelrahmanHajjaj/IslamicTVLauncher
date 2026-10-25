package tv.afnon.installer;

import android.util.AtomicFile;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Iterator;

/** Private phone backup, exported in the same format as tools/device.py. Worker thread only. */
final class Recovery {
    final File file;
    final JSONObject record;
    private final Adb adb;
    private final AtomicFile atomic;
    static final String[] SETTINGS = {"secure/screensaver_components", "secure/screensaver_enabled",
        "secure/sleep_timeout", "secure/enabled_accessibility_services", "secure/accessibility_enabled", "global/CustomKeyList"};

    Recovery(Adb adb, File directory, String target, Tv tv) throws Exception {
        this.adb = adb;
        String serial = adb.shell("getprop ro.serialno").out.trim();
        String identity = serial.isEmpty() || serial.equals("unknown") ? target : serial;
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8));
        StringBuilder id = new StringBuilder();
        for (byte b : hash) id.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
        File folder = new File(directory, "backups/" + id);
        if (!folder.isDirectory() && !folder.mkdirs()) throw new IOException("Cannot create recovery folder");
        file = new File(folder, "recovery.json");
        atomic = new AtomicFile(file);
        if (file.exists()) {
            record = new JSONObject(new String(atomic.readFully(), StandardCharsets.UTF_8));
            if (record.optInt("version") != 1 || !id.toString().equals(record.optString("device")))
                throw new IOException("Wrong recovery record");
            return;
        }
        if (tv.enabledPackages().isEmpty()) throw new IOException("TV returned no packages");
        record = new JSONObject().put("version", 1).put("device", id.toString()).put("home", tv.home())
            .put("enabled", new JSONArray(tv.enabledPackages())).put("disabled", new JSONArray(tv.disabledPackages()))
            .put("packages_changed", new JSONArray()).put("settings_changed", new JSONObject());
        JSONObject settings = new JSONObject();
        for (String name : SETTINGS) settings.put(name, setting(name));
        String global = adb.shell("settings list global").out;
        for (String line : global.split("\n")) {
            String key = line.split("=", 2)[0];
            if (key.matches("CustomKey_[0-9]+")) settings.put("global/" + key, setting("global/" + key));
        }
        record.put("settings", settings);
        JSONObject diagnostics = new JSONObject();
        for (String cmd : new String[]{"getprop", "pm list packages --user 0", "settings list secure", "settings list global", "settings list system"})
            diagnostics.put(cmd, adb.shell(cmd).out);
        record.put("diagnostics", diagnostics);
        save();
    }

    private Object setting(String name) throws IOException, InterruptedException {
        String value = adb.shell("settings get " + name.replace('/', ' ')).out.trim();
        return value.equals("null") ? JSONObject.NULL : value;
    }

    void prepareSetup() throws Exception {
        // Save intended writes before setup: an ADB disconnect must not lose the undo record.
        String name = "secure/enabled_accessibility_services";
        Object current = setting(name);
        java.util.List<String> services = new java.util.ArrayList<>();
        if (current != JSONObject.NULL) for (String service : current.toString().split(":"))
            if (!service.isEmpty()) services.add(service);
        if (!services.contains("tv.afnon.home/.RemoteKeys") && !services.contains("tv.afnon.home/tv.afnon.home.RemoteKeys"))
            services.add("tv.afnon.home/.RemoteKeys");
        JSONObject changed = record.getJSONObject("settings_changed");
        String desired = String.join(":", services);
        if (!desired.equals(current)) changed.put(name, desired);
        if (!"1".equals(setting("secure/accessibility_enabled"))) changed.put("secure/accessibility_enabled", "1");
        save();
    }

    void beforeDisable(String pkg) throws Exception {
        JSONArray changed = record.getJSONArray("packages_changed");
        for (int i = 0; i < changed.length(); i++) if (pkg.equals(changed.getString(i))) return;
        changed.put(pkg);
        save();
    }

    void save() throws IOException {
        FileOutputStream out = atomic.startWrite();
        try {
            out.write(record.toString().getBytes(StandardCharsets.UTF_8));
            atomic.finishWrite(out);
        } catch (IOException e) { atomic.failWrite(out); throw e; }
    }

    void restore(Tv tv) throws Exception {
        String home = record.getString("home");
        if (!Safety.home(home)) throw new IOException("No original working Home recorded; Afnon remains installed");
        JSONArray changed = record.getJSONArray("packages_changed"), enabled = record.getJSONArray("enabled");
        for (int i = 0; i < changed.length(); i++) {
            String pkg = changed.getString(i);
            if (!Safety.packageName(pkg)) throw new IOException("Invalid package in recovery record");
            for (int k = 0; k < enabled.length(); k++)
                if (pkg.equals(enabled.getString(k)) && !tv.enable(pkg)) throw new IOException("Could not restore " + pkg);
        }
        if (!tv.enable(home.split("/")[0])) throw new IOException("Original Home is unavailable");
        tv.startHome(home);
        if (!adb.shell("cmd package set-home-activity --user 0 " + Tv.quote(home)).ok() || !Safety.sameHome(home, tv.home()))
            throw new IOException("Original Home selection failed; Afnon stays available");
        JSONObject settings = record.getJSONObject("settings"), applied = record.getJSONObject("settings_changed");
        if (applied.has("secure/enabled_accessibility_services")) tv.restoreRemote();
        java.util.List<String> keys = new java.util.ArrayList<>();
        for (Iterator<String> it = applied.keys(); it.hasNext();) keys.add(it.next());
        keys.sort(java.util.Comparator.comparing(key -> !key.equals("secure/enabled_accessibility_services")));
        for (String key : keys) {
            if (!key.matches("(?:secure|global|system)/[A-Za-z_0-9]+")) throw new IOException("Invalid recovery setting");
            if (!setting(key).equals(applied.get(key))) {
                if (key.equals("secure/enabled_accessibility_services") && !settings.optString(key).contains("tv.afnon.home/")) {
                    java.util.List<String> kept = new java.util.ArrayList<>();
                    for (String entry : setting(key).toString().split(":"))
                        if (!entry.isEmpty() && !entry.equals("null") && !entry.equals("tv.afnon.home/.RemoteKeys")
                            && !entry.equals("tv.afnon.home/tv.afnon.home.RemoteKeys")) kept.add(entry);
                    if (!adb.shell("settings put secure enabled_accessibility_services " + Tv.quote(String.join(":", kept))).ok())
                        throw new IOException("Could not restore accessibility services");
                }
                continue;
            }
            if (key.equals("secure/accessibility_enabled") && setting("secure/enabled_accessibility_services") != JSONObject.NULL
                && !setting("secure/enabled_accessibility_services").toString().isEmpty()) continue;
            String command = settings.isNull(key) ? "settings delete " + key.replace('/', ' ')
                : "settings put " + key.replace('/', ' ') + " " + Tv.quote(settings.getString(key));
            if (!adb.shell(command).ok() || !setting(key).equals(settings.get(key))) throw new IOException("Could not restore " + key);
        }
        tv.startHome(home);
    }
}
