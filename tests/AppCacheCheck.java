package tv.afnon.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import java.util.Arrays;
import java.util.List;

public class AppCacheCheck {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        android.os.Build.VERSION.SDK_INT = Integer.parseInt(args[0]);
        Context app = new Context(), activity = new Context();
        activity.application = app;
        activity.pm = app.pm;
        activity.prefs = app.prefs;
        PackageManager pm = app.pm;
        pm.add("alpha", "Alpha"); pm.add("beta", "Beta");
        pm.add("tv.afnon.home", "Hidden launcher");
        pm.add("com.google.android.youtube.tv", "YouTube from Android");
        app.prefs.putString("favourites", "beta");
        Text english = new Text(false), arabic = new Text(true);

        List<String> first = Apps.allApps(activity, english);
        check(first.equals(Arrays.asList("beta", "alpha", "com.google.android.youtube.tv")), "Favourite order, deduplication and hidden launcher");
        int queries = pm.queries;
        first.clear();
        android.os.SystemClock.now += 60_000;
        check(Apps.installed(app).size() == 3, "Returned lists must not mutate the catalogue");
        check(pm.queries == queries, "Home idle must not expire the catalogue before All apps");
        check(app.registrations == 1 && activity.registrations == 0, "Observe changes once without retaining an activity");
        check(app.filter.schemes.contains("package") && app.filter.actions.size() == 4, "Watch add/remove/change/replace");
        check(app.receiverFlags == (Integer.parseInt(args[0]) >= 33 ? Context.RECEIVER_NOT_EXPORTED : 0), "Receiver API compatibility");

        Apps.name(app, english, "beta");
        int labelReads = pm.labels;
        Apps.allApps(app, english);
        Apps.name(app, english, "beta");
        check(pm.labels == labelReads && pm.infoCalls == 0, "Reuse queried metadata and labels");
        check(Apps.name(app, arabic, "com.google.android.youtube.tv").equals("يوتيوب"), "Arabic override");
        check(Apps.name(app, english, "com.google.android.youtube.tv").equals("YouTube"), "Arabic must not poison English");

        pm.apps.remove("alpha"); pm.add("gamma", "Gamma"); pm.add("beta", "Updated beta");
        app.receiver.onReceive(app, new Intent(Intent.ACTION_PACKAGE_REPLACED));
        check(!Apps.installed(app).contains("alpha") && Apps.installed(app).contains("gamma"), "Same-count replacements refresh the catalogue");
        check(Apps.name(app, english, "beta").equals("Updated beta"), "Package changes refresh labels");
        check(pm.queries == queries + 2 && app.registrations == 1, "One refreshed catalogue, one observer");

        pm.apps.get("beta").label = "Localized beta";
        Apps.invalidate();
        check(Apps.name(app, english, "beta").equals("Localized beta"), "Resume/configuration invalidation refreshes labels");
        pm.apps.get("gamma").flags = 1;
        app.prefs.values.put("hide_system_apps", true);
        check(!Apps.allApps(app, english).contains("gamma"), "System utility filter still applies");
        check(Apps.installed(app).contains("gamma"), "Filtering must not mutate the catalogue");
    }
}
