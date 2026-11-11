package tv.afnon.home;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Hands Afnon's remote buttons to TCL's own key remapping, so RemoteKeys need not filter keys. With key filtering
 * on, Android re-injects every key and injected keys never auto-repeat: holding Volume moved it one step.
 *
 * TCL's window manager (TclCustomShortCutKeyManager in tcl-service.jar) reads Settings.Global "CustomKeyList", a JSON
 * array of keycodes, and for each one "CustomKey_<code>": {"keycode", "behavior": [{"keyaction", "type", "intent",
 * "appnotinstall"}]}. keyaction 1 acts on press, 2 on long press, 3 on release; type 1 starts an activity, 2 a service,
 * 3 sends a broadcast. It runs before any app sees the key and swallows it.
 *
 * That code catches nothing, inside system_server, so mistakes restart the TV's whole UI:
 * - An intent with no package, or one whose activity is missing, throws. Every intent names its package (TCL checks
 *   it is installed first and otherwise runs appnotinstall), and appnotinstall is a broadcast, which cannot throw.
 * - Changing "CustomKey_<code>" while <code> is not in the list throws (an observer outlives the list entry). So
 *   codes are never dropped from the list, only emptied, and keys are written a moment after the list, once TCL
 *   has read it.
 */
final class TclKeys {

    private static final String TAG = "AfnonKeys";
    private static final String FEATURE = "com.tcl.software.tinput_shortcut_config";
    private static final String LIST = "CustomKeyList";
    private static final String KEY_PREFIX = "CustomKey_";
    private static final String MISSING = "tv.afnon.home.KEY_TARGET_MISSING";
    /** Time for TCL's observer to reread the list before the keys change. */
    private static final long LIST_SETTLE_MS = 1000;

    private TclKeys() {
    }

    /**
     * Maps each keycode to its intent. Returns false when this TV has no TCL remapping or the settings can't be
     * written (no WRITE_SECURE_SETTINGS grant); the caller then filters keys itself.
     */
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable pending;

    static boolean install(Context c, Map<Integer, Intent> targets) {
        if (!c.getPackageManager().hasSystemFeature(FEATURE)) return false;
        ContentResolver r = c.getContentResolver();
        if (pending != null) handler.removeCallbacks(pending);
        try {
            Set<String> codes = codes(Settings.Global.getString(r, LIST));
            JSONObject values = new JSONObject();
            for (Map.Entry<Integer, Intent> e : targets.entrySet()) {
                Intent i = e.getValue();
                if (i.getPackage() == null && i.getComponent() != null) i.setPackage(i.getComponent().getPackageName());
                if (i.getPackage() == null) continue;
                String code = String.valueOf(e.getKey());
                values.put(code, entry(e.getKey(), i).toString());
                codes.add(code);
            }
            JSONArray list = new JSONArray();
            for (String code : codes) list.put(code);
            String oldList = Settings.Global.getString(r, LIST);
            if (!codes(oldList).equals(codes) && !Settings.Global.putString(r, LIST, list.toString())) return false;
            pending = () -> writeKeys(c, values);
            handler.postDelayed(pending, LIST_SETTLE_MS);
            return true;
        } catch (SecurityException | JSONException e) {
            Log.w(TAG, "TCL key remapping unavailable", e);
            return false;
        }
    }

    /** Only Afnon-owned values are restored; another app's later mapping belongs to that app. */
    static boolean restore(Context c) {
        if (pending != null) handler.removeCallbacks(pending);
        return writeKeys(c, new JSONObject());
    }

    private static boolean writeKeys(Context c, JSONObject values) {
        ContentResolver r = c.getContentResolver();
        android.content.SharedPreferences saved = c.getSharedPreferences("remote_original", Context.MODE_PRIVATE);
        try {
            JSONObject owned = new JSONObject(saved.getString("keys", "{}"));
            Set<String> removed = new HashSet<>();
            for (Iterator<String> it = owned.keys(); it.hasNext();) {
                String code = it.next();
                if (values.has(code)) continue;
                JSONObject record = owned.getJSONObject(code);
                String current = Settings.Global.getString(r, KEY_PREFIX + code);
                if (owns(record, current)) {
                    // Keep the list entry: TCL's observer can outlive it. Empty restores the firmware default.
                    String original = record.isNull("original") ? "" : record.getString("original");
                    if (!Settings.Global.putString(r, KEY_PREFIX + code, original)) return false;
                }
                removed.add(code);
            }
            for (String code : removed) owned.remove(code);
            for (Iterator<String> it = values.keys(); it.hasNext();) {
                String code = it.next();
                String current = Settings.Global.getString(r, KEY_PREFIX + code);
                JSONObject record = owned.optJSONObject(code);
                // A user or another app changed this since our last write: preserve their new original.
                if (record == null || !(owns(record, current)))
                    record = new JSONObject().put("original", current == null ? JSONObject.NULL : current);
                record.put("pending", values.getString(code));
                owned.put(code, record);
            }
            if (!saved.edit().putString("keys", owned.toString()).commit()) return false;
            for (Iterator<String> it = values.keys(); it.hasNext();) {
                String code = it.next(), value = values.getString(code);
                if (!value.equals(Settings.Global.getString(r, KEY_PREFIX + code))
                        && !Settings.Global.putString(r, KEY_PREFIX + code, value)) return false;
                JSONObject record = owned.getJSONObject(code);
                record.put("applied", value);
                record.remove("pending");
            }
            return saved.edit().putString("keys", owned.toString()).commit();
        } catch (SecurityException | JSONException e) {
            Log.w(TAG, "Could not save or restore Afnon button mappings", e);
            return false;
        }
    }

    private static boolean owns(JSONObject record, String value) {
        return record.has("applied") && record.optString("applied").equals(value)
            || record.has("pending") && record.optString("pending").equals(value);
    }

    private static JSONObject entry(int code, Intent target) throws JSONException {
        JSONObject missing = new JSONObject()
                .put("type", 3)
                .put("intent", new Intent(MISSING).toUri(Intent.URI_INTENT_SCHEME));
        JSONObject behavior = new JSONObject()
                .put("keyaction", 1)
                .put("type", 1)
                .put("intent", target.toUri(Intent.URI_INTENT_SCHEME))
                .put("appnotinstall", missing);
        return new JSONObject().put("keycode", code).put("behavior", new JSONArray().put(behavior));
    }

    private static Set<String> codes(String json) throws JSONException {
        Set<String> out = new java.util.LinkedHashSet<>();
        if (json == null || json.isEmpty()) return out;
        JSONArray a = new JSONArray(json);
        for (int i = 0; i < a.length(); i++) {
            String code = a.getString(i);
            if (!code.matches("[0-9]{1,6}")) throw new JSONException("Invalid TCL key list");
            out.add(code);
        }
        return out;
    }
}
