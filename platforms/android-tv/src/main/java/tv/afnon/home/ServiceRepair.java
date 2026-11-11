package tv.afnon.home;

import android.content.*;
import android.provider.Settings;

/** CleanYourTV's opt-in force-stop recovery pattern. Copyright (c) 2026 Gathin, MIT. */
final class ServiceRepair {
    static void connected(Context c) { Prefs.of(c).edit().putBoolean("service_was_connected", true).apply(); }
    static void disconnected(Context c) { Prefs.of(c).edit().remove("service_was_connected").apply(); }
    static void restore(Context c) {
        if (!Prefs.of(c).getBoolean("repair_service", false) || !Prefs.of(c).getBoolean("service_was_connected", false)) return;
        if (c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != android.content.pm.PackageManager.PERMISSION_GRANTED) return;
        try {
            String list = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (list == null || list.equals("null")) list = "";
            ComponentName ours = new ComponentName(c, RemoteKeys.class);
            for (String entry : list.split(":")) if (ours.equals(ComponentName.unflattenFromString(entry))) return;
            Settings.Secure.putString(c.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                list + (list.isEmpty() ? "" : ":") + ours.flattenToShortString());
            Settings.Secure.putInt(c.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
        } catch (RuntimeException ignored) { }
    }
}
