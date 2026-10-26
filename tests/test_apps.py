"""Exercise the real app catalogue with counted Android package-manager calls."""
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]

# Only Android boundaries are faked; Apps, AppFilter and favourite policy are production code.
STUBS = {
    "android/content/Context.java": """
package android.content;
public class Context {
    public static final int MODE_PRIVATE = 0, RECEIVER_NOT_EXPORTED = 4;
    public Context application = this;
    public android.content.pm.PackageManager pm = new android.content.pm.PackageManager();
    public SharedPreferences prefs = new SharedPreferences();
    public BroadcastReceiver receiver;
    public IntentFilter filter;
    public int registrations, receiverFlags;
    public Context getApplicationContext() { return application; }
    public android.content.pm.PackageManager getPackageManager() { return pm; }
    public SharedPreferences getSharedPreferences(String name, int mode) { return prefs; }
    public void startActivity(Intent i) {}
    public Intent registerReceiver(BroadcastReceiver r, IntentFilter f) {
        registrations++; receiver = r; filter = f; return null;
    }
    public Intent registerReceiver(BroadcastReceiver r, IntentFilter f, int flags) {
        receiverFlags = flags; return registerReceiver(r, f);
    }
}
""",
    "android/content/Intent.java": """
package android.content;
public class Intent {
    public static final String ACTION_MAIN = "main", CATEGORY_LEANBACK_LAUNCHER = "tv", CATEGORY_LAUNCHER = "launcher",
        ACTION_PACKAGE_ADDED = "added", ACTION_PACKAGE_REMOVED = "removed", ACTION_PACKAGE_CHANGED = "changed", ACTION_PACKAGE_REPLACED = "replaced";
    public static final int FLAG_ACTIVITY_NEW_TASK = 1, FLAG_ACTIVITY_RESET_TASK_IF_NEEDED = 2;
    public Intent(String action) {}
    public Intent addCategory(String category) { return this; }
    public Intent addFlags(int flags) { return this; }
}
""",
    "android/content/IntentFilter.java": """
package android.content;
public class IntentFilter {
    public java.util.Set<String> actions = new java.util.HashSet<>(), schemes = new java.util.HashSet<>();
    public void addAction(String action) { actions.add(action); }
    public void addDataScheme(String scheme) { schemes.add(scheme); }
}
""",
    "android/content/BroadcastReceiver.java": "package android.content; public abstract class BroadcastReceiver { public abstract void onReceive(Context c, Intent i); }",
    "android/content/ActivityNotFoundException.java": "package android.content; public class ActivityNotFoundException extends RuntimeException {}",
    "android/content/SharedPreferences.java": """
package android.content;
public class SharedPreferences {
    public java.util.Map<String, Object> values = new java.util.HashMap<>();
    public String getString(String key, String fallback) { return (String) values.getOrDefault(key, fallback); }
    public boolean getBoolean(String key, boolean fallback) { return (Boolean) values.getOrDefault(key, fallback); }
    public SharedPreferences edit() { return this; }
    public SharedPreferences putString(String key, String value) { values.put(key, value); return this; }
    public void apply() {}
}
""",
    "android/content/pm/ApplicationInfo.java": """
package android.content.pm;
public class ApplicationInfo {
    public static final int FLAG_SYSTEM = 1, FLAG_UPDATED_SYSTEM_APP = 128,
        CATEGORY_GAME = 0, CATEGORY_AUDIO = 1, CATEGORY_VIDEO = 2, CATEGORY_IMAGE = 3;
    public int flags, category = -1;
    public String packageName, label;
}
""",
    "android/content/pm/ActivityInfo.java": "package android.content.pm; public class ActivityInfo { public String packageName; public ApplicationInfo applicationInfo; }",
    "android/content/pm/ResolveInfo.java": "package android.content.pm; public class ResolveInfo { public ActivityInfo activityInfo = new ActivityInfo(); }",
    "android/content/pm/PackageManager.java": """
package android.content.pm;
public class PackageManager {
    public static class NameNotFoundException extends Exception {}
    public java.util.Map<String, ApplicationInfo> apps = new java.util.HashMap<>();
    public int queries, labels, infoCalls;
    public void add(String pkg, String label) {
        ApplicationInfo a = new ApplicationInfo(); a.packageName = pkg; a.label = label; apps.put(pkg, a);
    }
    public java.util.List<ResolveInfo> queryIntentActivities(android.content.Intent intent, int flags) {
        queries++; java.util.List<ResolveInfo> result = new java.util.ArrayList<>();
        for (ApplicationInfo a : apps.values()) {
            ResolveInfo r = new ResolveInfo(); r.activityInfo.packageName = a.packageName;
            r.activityInfo.applicationInfo = a; result.add(r);
        }
        return result;
    }
    public ApplicationInfo getApplicationInfo(String pkg, int flags) throws NameNotFoundException {
        infoCalls++; if (!apps.containsKey(pkg)) throw new NameNotFoundException(); return apps.get(pkg);
    }
    public CharSequence getApplicationLabel(ApplicationInfo info) { labels++; return info.label; }
    public android.graphics.drawable.Drawable getApplicationIcon(ApplicationInfo info) { return null; }
    public android.content.Intent getLeanbackLaunchIntentForPackage(String pkg) { return null; }
    public android.content.Intent getLaunchIntentForPackage(String pkg) { return null; }
}
""",
    "android/graphics/drawable/Drawable.java": "package android.graphics.drawable; public class Drawable {}",
    "android/os/Build.java": "package android.os; public class Build { public static class VERSION { public static int SDK_INT; } }",
    "android/os/SystemClock.java": "package android.os; public class SystemClock { public static long now; public static long uptimeMillis() { return now; } }",
    "android/text/TextUtils.java": 'package android.text; public class TextUtils { public static String join(CharSequence separator, Iterable<?> values) { java.util.StringJoiner j = new java.util.StringJoiner(separator); for (Object v : values) j.add(v.toString()); return j.toString(); } }',
    "tv/afnon/home/Text.java": "package tv.afnon.home; final class Text { final boolean ar; Text(boolean arabic) { ar = arabic; } }",
}


class AppCacheChecks(unittest.TestCase):
    def test_idle_cache_package_changes_and_language(self):
        with tempfile.TemporaryDirectory() as output:
            sources = []
            for name, code in STUBS.items():
                path = Path(output) / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(code, encoding="utf-8")
                sources.append(str(path))
            source = ROOT / "app/src/main/java/tv/afnon/home"
            result = subprocess.run([shutil.which("javac"), "-encoding", "UTF-8", "-d", output,
                *sources, *(str(source / name) for name in ("Apps.java", "AppFilter.java", "FavouriteChoice.java")),
                str(ROOT / "tests/AppCacheCheck.java")], capture_output=True, text=True)
            self.assertEqual(result.returncode, 0, result.stderr)
            for sdk in (26, 34):
                with self.subTest(android=sdk):
                    result = subprocess.run([shutil.which("java"), "-cp", output,
                        "tv.afnon.home.AppCacheCheck", str(sdk)], capture_output=True, text=True)
                    self.assertEqual(result.returncode, 0, result.stderr)
