package tv.afnon.home;

/** Visibility decisions are shared by window events and the existing prayer clock check. */
final class DockPolicy {
    static boolean validMode(String mode) { return "hidden".equals(mode) || "tab".equals(mode) || "off".equals(mode); }
    static boolean validPosition(String position) { return "top".equals(position) || "bottom".equals(position); }
    static boolean tab(String mode, String pkg, String screen, boolean asleep) {
        return "tab".equals(mode) && !asleep && !pkg.isEmpty() && !pkg.equals("tv.afnon.home")
            && !pkg.equals("com.android.systemui") && !pkg.contains("permissioncontroller")
            && !pkg.equals("com.android.settings") && !pkg.equals("com.android.tv.settings") && !pkg.equals("com.tcl.settings")
            && !screen.contains("Dream") && !screen.contains("Screensaver");
    }
}
