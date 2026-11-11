package tv.afnon.home;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

public final class PrefsCheck {
    static final class Memory implements SharedPreferences {
        final Map<String, Object> values = new HashMap<>();
        int writes;
        public int getInt(String key, int fallback) { return (Integer) values.getOrDefault(key, fallback); }
        public String getString(String key, String fallback) { return (String) values.getOrDefault(key, fallback); }
        public boolean getBoolean(String key, boolean fallback) { return (Boolean) values.getOrDefault(key, fallback); }
        public Editor edit() {
            return new Editor() {
                final Map<String, Object> pending = new HashMap<>();
                public Editor putInt(String k, int v) { pending.put(k, v); return this; }
                public Editor putString(String k, String v) { pending.put(k, v); return this; }
                public Editor putBoolean(String k, boolean v) { pending.put(k, v); return this; }
                public void apply() { values.putAll(pending); writes++; }
            };
        }
    }
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static Context context(Memory saved) {
        return new Context() {
            public SharedPreferences getSharedPreferences(String name, int mode) {
                check(name.equals("afnon") && mode == MODE_PRIVATE, "Retain the existing preference store");
                return saved;
            }
        };
    }
    public static void main(String[] args) {
        Memory saved = new Memory();
        Context context = context(saved);
        check(java.util.Arrays.equals(Prefs.prayerOffsets(context), new int[6]), "Default offsets stay zero");
        saved.values.put("theme", "aurora"); saved.values.put("startup", true); saved.values.put("input", "HDMI_2");
        for (int prayer = 0; prayer < 6; prayer++) {
            String key = "prayer_offset_" + prayer;
            for (int value : new int[] {Integer.MIN_VALUE, -31, -30, 0, 30, 31, Integer.MAX_VALUE}) {
                saved.values.put(key, value);
                check(Prefs.prayerOffset(context, prayer) == Math.max(-30, Math.min(30, value)), "Clamp saved offsets");
                check(saved.values.get(key).equals(value), "Reading never rewrites old settings");
            }
            for (int value : new int[] {-30, 0, 30}) {
                Prefs.setPrayerOffset(context, prayer, value);
                check(saved.values.get(key).equals(value), "Write the existing key");
            }
        }
        Map<String, Object> before = new HashMap<>(saved.values);
        int writes = saved.writes;
        for (int[] invalid : new int[][] {{-1,0}, {6,0}, {0,-31}, {0,31}, {0,Integer.MIN_VALUE}, {0,Integer.MAX_VALUE}}) {
            try { Prefs.setPrayerOffset(context, invalid[0], invalid[1]); throw new AssertionError("Invalid offset accepted"); }
            catch (IllegalArgumentException expected) { check(expected.getMessage().equals("Invalid prayer offset"), "Retain error text"); }
        }
        check(saved.writes == writes && saved.values.equals(before), "Rejected writes preserve all settings");
        saved.values.put("hijri_offset", 99);
        check(Prefs.hijriOffset(context) == 99, "Retain legacy Hijri read policy");
        for (int value : new int[] {Integer.MIN_VALUE, -3, -2, 0, 2, 3, Integer.MAX_VALUE}) {
            Prefs.setHijriOffset(context, value);
            check(Prefs.hijriOffset(context) == Math.max(-2, Math.min(2, value)), "Clamp Hijri writes");
        }
        check(saved.values.get("theme").equals("aurora") && saved.values.get("startup").equals(true)
            && saved.values.get("input").equals("HDMI_2"), "Prayer edits preserve unrelated settings");
    }
}
