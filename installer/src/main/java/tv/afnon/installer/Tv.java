package tv.afnon.installer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.muntashirakon.adb.AdbStream;

/**
 * What the installer asks of the TV once connected: Afnon's setup provider (its cities, apps and buttons, so the
 * installer keeps no copies), packages for the cleanup and its undo file, and which remote button was pressed.
 * Worker thread only.
 */
final class Tv {
    private static final String SETUP = "content://tv.afnon.home.setup/";
    /** What the cleanup turned off, one package a line, kept on the TV so any phone can undo it. */
    static final String CLEANED = "/data/local/tmp/afnon-cleaned.txt";

    private final Adb adb;

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

    boolean set(String name, String value) throws IOException, InterruptedException {
        return adb.shell("content insert --uri " + SETUP + "settings --bind name:s:" + name
                + " --bind value:s:" + quote(value)).ok();
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
        for (String line : adb.shell("pm list packages -e").out.split("\n")) {
            line = line.trim();
            if (line.startsWith("package:")) out.add(line.substring(8));
        }
        return out;
    }

    Set<String> disabledPackages() throws IOException, InterruptedException {
        Set<String> out = new HashSet<>();
        for (String line : adb.shell("pm list packages -d").out.split("\n")) {
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
        Adb.Result r = adb.shell("pm disable-user --user 0 " + pkg);
        boolean ok = r.ok() && !r.out.contains("Exception");
        // Noted only once: the undo list holds each package one time.
        if (ok) adb.shell("grep -qx '" + pkg + "' " + CLEANED + " 2>/dev/null || echo " + pkg + " >> " + CLEANED);
        return ok;
    }

    boolean enable(String pkg) throws IOException, InterruptedException {
        return adb.shell("pm enable " + pkg).ok();
    }

    /** The package of the app in front on the TV, or null. */
    String foreground() throws IOException, InterruptedException {
        String out = adb.shell("dumpsys activity activities | grep -m 1 -E 'mResumedActivity|topResumedActivity|ResumedActivity'").out;
        Matcher m = Pattern.compile(" u\\d+ ([\\w.]+)/").matcher(out);
        return m.find() ? m.group(1) : null;
    }

    /** Back to the TV's home screen (Afnon). */
    void home() throws IOException, InterruptedException {
        adb.shell("am start -a android.intent.action.MAIN -c android.intent.category.HOME");
    }

    void forgetCleaned() throws IOException, InterruptedException {
        adb.shell("rm -f " + CLEANED);
    }

    // Remote buttons

    /** Buttons Afnon handles itself or that move around the TV: never offered for an app. */
    private static final Set<String> NOT_APP_BUTTONS = new HashSet<>(Arrays.asList(
            "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT", "DPAD_CENTER", "ENTER", "BACK", "HOME", "MENU",
            "VOLUME_UP", "VOLUME_DOWN", "VOLUME_MUTE", "MUTE", "POWER", "SLEEP", "WAKEUP", "CHANNEL_UP", "CHANNEL_DOWN",
            "SETTINGS", "NOTIFICATION", "TV_INPUT", "TV", "ALL_APPS", "ESCAPE", "DEL", "SEARCH", "ASSISTANT",
            "VOICE_ASSIST", "MEDIA_PLAY_PAUSE", "MEDIA_PLAY", "MEDIA_PAUSE", "MEDIA_STOP", "MEDIA_NEXT",
            "MEDIA_PREVIOUS", "MEDIA_REWIND", "MEDIA_FAST_FORWARD", "INFO_DISABLED"));

    interface Buttons {
        /** A button was pressed: its Android key name (as in the TV's key layout, without KEYCODE_). */
        void pressed(String key);
        /** A button that can't open an app (navigation, volume, Settings…). */
        void notApp(String key);
    }

    private AdbStream listening;

    /**
     * Watches the TV's input devices and reports each button press until {@link #stopListening()}. Raw events from
     * getevent, named through each device's key layout file, which is what Android itself uses; injected keys and
     * TCL's own remapping don't hide anything from it.
     */
    void listen(Buttons buttons) throws IOException, InterruptedException {
        Map<String, Map<Integer, String>> layouts = layouts();
        AdbStream s = adb.stream("getevent");
        listening = s;
        Pattern down = Pattern.compile("(/dev/input/event\\d+): 0001 ([0-9a-f]{4}) 00000001");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.openInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                Matcher m = down.matcher(line);
                if (!m.find()) continue;
                Map<Integer, String> layout = layouts.get(m.group(1));
                String key = layout == null ? null : layout.get(Integer.parseInt(m.group(2), 16));
                if (key == null) continue;
                if (NOT_APP_BUTTONS.contains(key) || key.matches("\\d|NUMPAD_.*")) buttons.notApp(key);
                else buttons.pressed(key);
            }
        } catch (IOException stopped) {
            // stopListening() closed the stream.
        } finally {
            listening = null;
        }
    }

    void stopListening() {
        AdbStream s = listening;
        if (s != null) {
            try {
                s.close();
            } catch (IOException ignored) {
                // Already closed.
            }
        }
    }

    /** For each input device, its key layout: Linux key code to Android key name. */
    private Map<String, Map<Integer, String>> layouts() throws IOException, InterruptedException {
        Map<String, String> files = new HashMap<>();
        String path = null;
        for (String line : adb.shell("dumpsys input").out.split("\n")) {
            String t = line.trim();
            if (t.startsWith("Path: /dev/input/")) path = t.substring(6).trim();
            else if (t.startsWith("KeyLayoutFile:") && path != null) {
                String file = t.substring(14).trim();
                if (!file.isEmpty()) files.put(path, file);
                path = null;
            }
        }
        Map<String, Map<Integer, String>> out = new HashMap<>();
        Map<String, Map<Integer, String>> read = new HashMap<>();
        Pattern key = Pattern.compile("^\\s*key\\s+(\\d+)\\s+(\\S+)");
        for (Map.Entry<String, String> e : files.entrySet()) {
            Map<Integer, String> layout = read.get(e.getValue());
            if (layout == null) {
                layout = new HashMap<>();
                for (String line : adb.shell("cat " + e.getValue()).out.split("\n")) {
                    Matcher m = key.matcher(line);
                    if (m.find()) layout.put(Integer.parseInt(m.group(1)), m.group(2));
                }
                read.put(e.getValue(), layout);
            }
            out.put(e.getKey(), layout);
        }
        return out;
    }

    /** A remote key name for people: "TCL_NETFLIX" → "Netflix", "BUTTON_3" → "Button 3". */
    static String buttonName(String key) {
        String k = key.startsWith("KEYCODE_") ? key.substring(8) : key;
        if (k.matches("\\d+")) return "#" + k;
        k = k.replaceFirst("^(TCL|SONY|HISENSE|XIAOMI|PHILIPS|SKYWORTH)_", "");
        StringBuilder b = new StringBuilder();
        for (String w : k.split("_")) {
            if (w.isEmpty()) continue;
            if (b.length() > 0) b.append(' ');
            b.append(w.charAt(0)).append(w.substring(1).toLowerCase(java.util.Locale.ROOT));
        }
        return b.toString().replace("Youtube", "YouTube");
    }

    private static String quote(String s) {
        return "'" + s.replace("'", "'\\''") + "'";
    }
}
