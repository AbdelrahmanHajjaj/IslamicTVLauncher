package tv.afnon.installer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * What the installer asks of the TV once connected: Afnon's setup provider (its cities, apps and buttons, so the
 * installer keeps no copies), and packages for the cleanup and its undo file.
 * Worker thread only.
 */
final class Tv {
    private static final String SETUP = "content://tv.afnon.home.setup/";
    /** What the cleanup turned off, one package a line, kept on the TV so any phone can undo it. */
    static final String CLEANED = "/data/local/tmp/afnon-cleaned.txt";

    private final Adb adb;
    Recovery recovery;

    Tv(Adb adb) {
        this.adb = adb;
    }

    // Afnon

    /** Installed Afnon's version name ("0.5"), or null if it isn't installed. */
    String afnonVersion() throws IOException, InterruptedException {
        Matcher m = Pattern.compile("versionName=(\\S+)")
                .matcher(adb.shell("dumpsys package " + Setup.PACKAGE + " | grep -m1 versionName").out);
        return m.find() ? m.group(1) : null;
    }

    /** A query on Afnon's setup provider (its JSON), or null when this Afnon has none (0.5 and older). */
    String query(String path) throws IOException, InterruptedException {
        Adb.Result r = adb.shell("content query --uri " + SETUP + path);
        int at = r.out.indexOf("json=");
        return at < 0 ? null : r.out.substring(at + 5).trim();
    }

    JSONObject info() throws IOException, InterruptedException {
        try {
            String s = query("info");
            return s == null ? null : new JSONObject(s);
        } catch (JSONException e) {
            return null;
        }
    }

    JSONArray list(String path) throws IOException, InterruptedException {
        try {
            String s = query(path);
            return s == null ? new JSONArray() : new JSONArray(s);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    /**
     * Sets settings given as name, value, name, value…, in one shell: each ADB shell costs about a second on a TCL,
     * while the inserts themselves are quick. True if the last one took.
     */
    boolean set(String... nameValue) throws IOException, InterruptedException {
        List<String> inserts = new ArrayList<>();
        for (int i = 0; i + 1 < nameValue.length; i += 2) {
            inserts.add("content insert --uri " + SETUP + "settings --bind name:s:" + nameValue[i]
                    + " --bind value:s:" + quote(nameValue[i + 1]));
        }
        return adb.shell(String.join("; ", inserts)).ok();
    }

    /** An app's icon as PNG bytes, or null. */
    byte[] icon(String pkg) throws IOException, InterruptedException {
        byte[] b = adb.bytes("content read --uri " + SETUP + "icon/" + pkg);
        // A PNG starts 0x89 'P' 'N' 'G'; anything else is an error message.
        return b.length > 8 && (b[0] & 0xff) == 0x89 && b[1] == 'P' ? b : null;
    }

    // Cleanup

    Set<String> enabledPackages() throws IOException, InterruptedException {
        Set<String> out = new HashSet<>();
        for (String line : adb.shell("pm list packages -e --user 0").out.split("\n")) {
            line = line.trim();
            if (line.startsWith("package:")) out.add(line.substring(8));
        }
        return out;
    }

    Set<String> disabledPackages() throws IOException, InterruptedException {
        Set<String> out = new HashSet<>();
        for (String line : adb.shell("pm list packages -d --user 0").out.split("\n")) {
            line = line.trim();
            if (line.startsWith("package:")) out.add(line.substring(8));
        }
        return out;
    }

    /** What earlier cleanups turned off: still off (checked), and back on since (a firmware update, say). */
    static final class Cleaned {
        final List<String> off = new ArrayList<>(), backOn = new ArrayList<>();
    }

    Cleaned cleaned() throws IOException, InterruptedException {
        Cleaned out = new Cleaned();
        Set<String> listed = new LinkedHashSet<>();
        for (String line : adb.shell("cat " + CLEANED + " 2>/dev/null").out.split("\n")) {
            if (!line.trim().isEmpty()) listed.add(line.trim());
        }
        if (listed.isEmpty()) return out;
        Set<String> enabled = enabledPackages();
        for (String p : listed) (enabled.contains(p) ? out.backOn : out.off).add(p);
        return out;
    }

    /** Turns one package off for the TV's user (not uninstalled) and notes it for undo. */
    boolean disable(String pkg) throws IOException, InterruptedException {
        if (Safety.protectedPackage(pkg)) throw new IOException("Protected package");
        if (!enabledPackages().contains(pkg)) return true;
        if (recovery == null) throw new IOException("Create the phone backup first");
        requireHome();
        if (pkg.equals(home().split("/")[0]) || pkg.startsWith("com.cvte.tv.launcher")) return false;
        String dreams = adb.shell("settings get secure screensaver_components").out;
        if (dreams.contains(pkg + "/")) return false;
        if (pkg.equals("com.google.android.tungsten.setupwraith")) {
            String wifi = adb.shell("dumpsys wifi").out;
            if (!wifi.contains("cname=") || wifi.contains("cname=" + pkg)) return false;
        }
        String details = adb.shell("dumpsys package " + pkg).out;
        if (details.contains("android.permission.TV_INPUT_HARDWARE")) return false;
        try { recovery.beforeDisable(pkg); } catch (Exception e) { throw new IOException("Backup failed", e); }
        Adb.Result r = adb.shell("pm disable-user --user 0 " + pkg);
        boolean ok = r.ok() && r.out.contains("disabled-user") && disabledPackages().contains(pkg);
        if (ok) adb.shell("grep -qxF " + quote(pkg) + " " + CLEANED + " 2>/dev/null || echo " + quote(pkg) + " >> " + CLEANED);
        return ok;
    }

    boolean enable(String pkg) throws IOException, InterruptedException {
        if (!Safety.packageName(pkg)) throw new IOException("Invalid package");
        return adb.shell("pm enable --user 0 " + pkg).ok() && enabledPackages().contains(pkg);
    }

    String home() throws IOException, InterruptedException {
        String output = adb.shell("cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME").out.trim();
        String[] lines = output.split("\n");
        return lines.length == 0 ? "" : lines[lines.length - 1].trim();
    }

    void startHome(String component) throws IOException, InterruptedException {
        if (!Safety.home(component)) throw new IOException("No safe Home component");
        Adb.Result r = adb.shell("am start -W -a android.intent.action.MAIN -c android.intent.category.HOME -n " + quote(component));
        if (!r.ok() || !r.out.contains("Status: ok") || r.out.contains("Error:")) throw new IOException("Home did not start");
    }

    void requireHome() throws IOException, InterruptedException {
        String component = "tv.afnon.home/.MainActivity";
        if (!Safety.sameHome(component, home())) throw new IOException("Afnon is not the selected Home");
        startHome(component);
        String focus = adb.shell("dumpsys activity activities").out;
        boolean visible = false;
        for (String line : focus.split("\n"))
            if ((line.contains("mResumedActivity") || line.contains("mFocusedApp") || line.contains("topResumedActivity"))
                && line.contains("tv.afnon.home/")) visible = true;
        if (!visible) throw new IOException("Afnon is not visibly in front; cleanup stopped");
    }

    void restoreRemote() throws IOException, InterruptedException {
        Adb.Result r = adb.shell("content call --uri content://tv.afnon.home.setup --method restore_remote");
        if (!r.ok() || !r.out.contains("restored=true")) throw new IOException("Remote mappings could not be restored; keep the backup");
    }

    void forgetCleaned() throws IOException, InterruptedException {
        adb.shell("rm -f " + CLEANED);
    }

    static String quote(String s) {
        return "'" + s.replace("'", "'\\''") + "'";
    }
}
