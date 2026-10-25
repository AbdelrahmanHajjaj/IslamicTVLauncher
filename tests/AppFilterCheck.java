package tv.afnon.home;

import android.content.pm.ApplicationInfo;

public final class AppFilterCheck {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        int system = ApplicationInfo.FLAG_SYSTEM;
        int updated = system | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        int unknown = ApplicationInfo.CATEGORY_UNDEFINED;
        for (String pkg : new String[] {"com.android.vending", "com.google.android.play.games",
                "com.google.android.videos", "com.google.android.youtube.tv", "com.google.android.youtube.tvmusic",
                "com.netflix.ninja", "com.tcl.gallery", "com.tcl.ui_mediaCenter"}) {
            check(!AppFilter.hideSystem(pkg, system, unknown), pkg + " stays visible when preinstalled");
            check(!AppFilter.hideSystem(pkg, updated, unknown), pkg + " stays visible after an update");
        }
        for (String pkg : new String[] {"com.android.tv.settings", "com.tcl.initsetup", "com.tcl.guard",
                "com.google.android.gms", "com.google.android.youtube.tv.helper"}) {
            check(AppFilter.hideSystem(pkg, system, unknown), pkg + " is a system utility");
            check(AppFilter.hideSystem(pkg, updated, unknown), pkg + " remains a utility after an update");
            check(!AppFilter.hideSystem(pkg, 0, unknown), "Downloaded apps are never hidden by this switch");
        }
        for (int category : new int[] {ApplicationInfo.CATEGORY_GAME, ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_IMAGE}) {
            check(!AppFilter.hideSystem("example.preinstalled.media", system, category), "Classified media and games stay visible");
        }
        check(AppFilter.hideSystem("example.preinstalled.utility", system, unknown), "Unknown system utilities stay hidden");
    }
}
