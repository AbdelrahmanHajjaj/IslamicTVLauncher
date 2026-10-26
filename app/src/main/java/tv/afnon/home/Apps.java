package tv.afnon.home;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** The apps Afnon shows: which exist, their names and icons, launching, and the saved favourites order. */
final class Apps {
    private Apps() {}

    static final int FAVOURITE_SLOTS = FavouriteChoice.MAX;

    private static final String[] DEFAULT_FAVOURITES = {
        "com.google.android.youtube.tv", "net.mbc.shahidTV", "dev.ctv.tv", "com.tcl.browser"};
    /** Never listed: this launcher, the previous one, and the TV's own input/Live TV plumbing. */
    private static final Set<String> HIDDEN = new HashSet<>(Arrays.asList(
        "tv.afnon.home", "com.gathin.tvhome", "com.tcl.suspension", "com.tcl.tv"));

    private static final Map<String, String> AR_NAMES = new HashMap<>();
    private static final Map<String, String> EN_NAMES = new HashMap<>();
    static {
        AR_NAMES.put("com.google.android.youtube.tv", "يوتيوب");
        AR_NAMES.put("net.mbc.shahidTV", "شاهد");
        AR_NAMES.put("com.google.android.youtube.tvmusic", "يوتيوب ميوزك");
        AR_NAMES.put("com.android.vending", "متجر Play");
        AR_NAMES.put("com.tcl.ui_mediaCenter", "مشغل الوسائط");
        AR_NAMES.put("com.sami.salaty_tv", "صلاتي");
        AR_NAMES.put("com.android.tv.settings", "الإعدادات");
        AR_NAMES.put("com.google.android.play.games", "ألعاب Google Play");
        EN_NAMES.put("com.google.android.youtube.tv", "YouTube");
        EN_NAMES.put("net.mbc.shahidTV", "Shahid");
        EN_NAMES.put("com.android.vending", "Play Store");
        EN_NAMES.put("com.phlox.tvwebbrowser", "TV Bro");
    }

    private static List<String> cached;
    private static long cachedAt;
    private static final Map<String, ApplicationInfo> appInfo = new HashMap<>();

    static synchronized void invalidate() { cached = null; appInfo.clear(); }

    /**
     * Every app that has a TV or a phone-style launcher entry, one per package, not hidden. Cached for two
     * seconds: one screen build asks several times, and each query is slow on this TV.
     */
    static synchronized List<String> installed(Context c) {
        long now = android.os.SystemClock.uptimeMillis();
        if (cached != null && now - cachedAt < 2000) return new ArrayList<>(cached);
        appInfo.clear();
        PackageManager pm = c.getPackageManager();
        Set<String> found = new LinkedHashSet<>();
        for (String category : new String[] {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            for (ResolveInfo r : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category), 0)) {
                String p = r.activityInfo.packageName;
                if (!HIDDEN.contains(p)) { found.add(p); appInfo.put(p, r.activityInfo.applicationInfo); }
            }
        }
        cached = new ArrayList<>(found);
        java.util.Collections.sort(cached);
        cachedAt = now;
        return new ArrayList<>(cached);
    }

    /** Favourites first, in their saved order; then the rest by name in the screen's language. */
    static List<String> sorted(Context c, Text t) {
        List<String> all = installed(c);
        List<String> favs = favourites(c);
        List<String> rest = new ArrayList<>();
        for (String p : all) if (!favs.contains(p)) rest.add(p);
        Collator col = Collator.getInstance(t.ar ? new Locale("ar") : Locale.ENGLISH);
        Map<String, String> names = new HashMap<>();
        for (String p : rest) names.put(p, name(c, t, p));
        rest.sort((a, b) -> col.compare(names.get(a), names.get(b)));
        List<String> out = new ArrayList<>();
        for (String p : favs) if (all.contains(p)) out.add(p);
        out.addAll(rest);
        return out;
    }

    static List<String> allApps(Context c, Text t) {
        List<String> apps = sorted(c, t);
        if (prefs(c).getBoolean("hide_system_apps", false)) apps.removeIf(pkg -> {
            try {
                ApplicationInfo app = info(c, pkg);
                return AppFilter.hideSystem(pkg, app.flags, app.category);
            }
            catch (PackageManager.NameNotFoundException gone) { return true; }
        });
        return apps;
    }

    static List<String> recent(Context c) {
        List<String> all = installed(c), out = new ArrayList<>();
        for (String pkg : prefs(c).getString("recent_apps", "").split(","))
            if (all.contains(pkg) && !out.contains(pkg) && out.size() < 5) out.add(pkg);
        return out;
    }

    static void remember(Context c, String pkg) {
        if (pkg == null || pkg.isEmpty() || pkg.startsWith("@") || HIDDEN.contains(pkg)) return;
        LinkedHashSet<String> recent = new LinkedHashSet<>();
        recent.add(pkg);
        for (String old : prefs(c).getString("recent_apps", "").split(","))
            if (!old.isEmpty() && recent.size() < 5) recent.add(old);
        prefs(c).edit().putString("recent_apps", TextUtils.join(",", recent)).apply();
    }

    // ---------------------------------------------------------------- favourites

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("afnon", Context.MODE_PRIVATE);
    }

    /** At most {@link #FAVOURITE_SLOTS}, in Home order. Uninstalled ones are kept, so a reinstall brings them back. */
    static List<String> favourites(Context c) {
        String s = prefs(c).getString("favourites", null);
        if (s == null) return new ArrayList<>(Arrays.asList(DEFAULT_FAVOURITES));
        List<String> out = new ArrayList<>();
        for (String p : s.split(",")) if (!p.isEmpty()) out.add(p);
        return out;
    }

    /** Up to nine Home favourites, with at least four slots when enough apps are installed. */
    static List<String> slots(Context c, Text t) {
        List<String> installed = installed(c);
        List<String> out = new ArrayList<>();
        List<String> saved = favourites(c);
        int count = Math.min(FAVOURITE_SLOTS, Math.max(4, saved.size()));
        for (String p : saved) if (installed.contains(p) && !out.contains(p) && out.size() < count) out.add(p);
        if (out.size() < count) for (String p : sorted(c, t)) if (out.size() < count && !out.contains(p)) out.add(p);
        return out;
    }

    static void saveFavourites(Context c, List<String> favs) {
        List<String> clean = new ArrayList<>(new LinkedHashSet<>(favs));
        if (clean.size() > FAVOURITE_SLOTS) clean = clean.subList(0, FAVOURITE_SLOTS);
        prefs(c).edit().putString("favourites", TextUtils.join(",", clean)).apply();
    }

    // ---------------------------------------------------------------- one app

    // The launcher query already supplied this; asking Android again per label/icon stalls All apps.
    private static synchronized ApplicationInfo info(Context c, String pkg) throws PackageManager.NameNotFoundException {
        ApplicationInfo value = appInfo.get(pkg);
        if (value == null) { value = c.getPackageManager().getApplicationInfo(pkg, 0); appInfo.put(pkg, value); }
        return value;
    }

    static String name(Context c, Text t, String pkg) {
        if (t.ar && AR_NAMES.containsKey(pkg)) return AR_NAMES.get(pkg);
        if (EN_NAMES.containsKey(pkg)) return EN_NAMES.get(pkg);
        try {
            PackageManager pm = c.getPackageManager();
            return pm.getApplicationLabel(info(c, pkg)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }

    static Drawable icon(Context c, String pkg) {
        if (pkg == null) return null;
        try {
            return c.getPackageManager().getApplicationIcon(info(c, pkg));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /** Opens the app's TV entry, or its phone-style one; back at its main screen, not wherever it was left. */
    /** The app's TV launch intent, or its phone one, or null if it isn't installed. */
    static Intent launchIntent(Context c, String pkg) {
        PackageManager pm = c.getPackageManager();
        Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
        return i != null ? i : pm.getLaunchIntentForPackage(pkg);
    }

    static void launch(Context c, String pkg) {
        Intent i = launchIntent(c, pkg);
        if (i == null) return;
        try {
            c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
            remember(c, pkg);
        } catch (ActivityNotFoundException | SecurityException ignored) {
            // The app went away or refuses outside launches; stay where we are.
        }
    }

    static boolean canUninstall(Context c, String pkg) {
        if (HIDDEN.contains(pkg)) return false;
        try {
            int flags = c.getPackageManager().getApplicationInfo(pkg, 0).flags;
            return (flags & (android.content.pm.ApplicationInfo.FLAG_SYSTEM
                | android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0;
        } catch (PackageManager.NameNotFoundException e) { return false; }
    }

    static boolean isLatin(String s) {
        for (int i = 0; i < s.length(); i++) if (Character.UnicodeBlock.of(s.charAt(i)) == Character.UnicodeBlock.ARABIC) return false;
        return true;
    }
}
