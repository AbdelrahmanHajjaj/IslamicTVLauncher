package tv.afnon.installer;
public final class SafetyCheck {
    public static void main(String[] args) {
        for (String p : new String[]{"android", "tv.afnon.home", "com.android.systemui", "com.android.ext.services",
                "com.google.android.permissioncontroller", "com.android.providers.media", "com.android.vending", "x;reboot"})
            if (!Safety.protectedPackage(p)) throw new AssertionError(p);
        if (Safety.protectedPackage("com.example.optional")) throw new AssertionError("Optional app blocked");
        if (Safety.home("com.cvte.tv.hkcfac/.Main") || Safety.home("android/com.android.internal.app.ResolverActivity"))
            throw new AssertionError("Unsafe Home accepted");
        if (!Safety.sameHome("tv.afnon.home/.MainActivity", "tv.afnon.home/tv.afnon.home.MainActivity"))
            throw new AssertionError("Short Home component differs");
    }
}
