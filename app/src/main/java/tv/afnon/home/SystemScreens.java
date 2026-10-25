package tv.afnon.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.widget.Toast;
import java.util.*;

/** User-selected system screens with vendor fallbacks; no shell or hidden API in the launcher. */
final class SystemScreens {
    static void open(Activity a, String action) {
        for (Intent i : new Intent[]{new Intent(action), new Intent(Settings.ACTION_SETTINGS),
                new Intent(Intent.ACTION_MAIN).setPackage("com.android.tv.settings").addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)}) {
            try { a.startActivity(i); return; } catch (ActivityNotFoundException | SecurityException ignored) { }
        }
        Toast.makeText(a, Screen.loadText(a).systemUnavailable(), Toast.LENGTH_LONG).show();
    }
    static void home(Activity a) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            android.app.role.RoleManager roles = a.getSystemService(android.app.role.RoleManager.class);
            if (roles != null && roles.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) && !roles.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)) {
                try { a.startActivityForResult(roles.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME), 90); return; }
                catch (RuntimeException ignored) { }
            }
        }
        open(a, Settings.ACTION_HOME_SETTINGS);
    }
    static void dreams(Activity a) {
        Text t = Screen.loadText(a);
        List<String> labels = new ArrayList<>(), components = new ArrayList<>();
        String selected = Settings.Secure.getString(a.getContentResolver(), "screensaver_components");
        for (ResolveInfo info : a.getPackageManager().queryIntentServices(new Intent("android.service.dreams.DreamService"), 0)) {
            if (info.serviceInfo == null || !"android.permission.BIND_DREAM_SERVICE".equals(info.serviceInfo.permission)) continue;
            String component = new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name).flattenToString();
            components.add(component);
            ComponentName current = selected == null ? null : ComponentName.unflattenFromString(selected);
            labels.add(info.loadLabel(a.getPackageManager()).toString() + (current != null && component.equals(current.flattenToString()) ? " ✓" : ""));
        }
        labels.add(t.saverSystem());
        new AlertDialog.Builder(a).setTitle(t.screensaver()).setItems(labels.toArray(new String[0]), (d, index) -> {
            if (index == components.size()) { open(a, Settings.ACTION_DREAM_SETTINGS); return; }
            try {
                if (!Settings.Secure.putString(a.getContentResolver(), "screensaver_components", components.get(index))
                        || !Settings.Secure.putInt(a.getContentResolver(), "screensaver_enabled", 1)) throw new SecurityException();
                Toast.makeText(a, t.saved(), Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) { open(a, Settings.ACTION_DREAM_SETTINGS); }
        }).setNegativeButton(t.back(), null).show();
    }
}
