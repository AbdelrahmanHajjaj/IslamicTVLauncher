package tv.afnon.home;

import android.content.pm.ApplicationInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class AppFilter {
    private AppFilter() {}

    // shortcut: some TV preloads omit their media category; extend this list for confirmed misclassifications.
    private static final Set<String> EVERYDAY_APPS = new HashSet<>(Arrays.asList(
        "com.android.vending", "com.google.android.play.games", "com.google.android.videos",
        "com.google.android.youtube.tv", "com.google.android.youtube.tvmusic", "com.netflix.ninja",
        "com.tcl.gallery", "com.tcl.ui_mediaCenter"));

    static boolean hideSystem(String pkg, int flags, int category) {
        if ((flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0
                || EVERYDAY_APPS.contains(pkg)) return false;
        switch (category) {
            case ApplicationInfo.CATEGORY_GAME:
            case ApplicationInfo.CATEGORY_AUDIO:
            case ApplicationInfo.CATEGORY_VIDEO:
            case ApplicationInfo.CATEGORY_IMAGE:
                return false;
            default:
                return true;
        }
    }
}
