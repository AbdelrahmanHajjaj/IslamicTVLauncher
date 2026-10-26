package tv.afnon.home;
public class DockPolicyCheck {
    public static void main(String[] args) {
        check(DockPolicy.tab("tab", "com.tcl.tv", "com.tcl.tv.TVActivity", false));
        check(DockPolicy.tab("tab", "com.netflix.ninja", "PlayerActivity", false));
        check(!DockPolicy.tab("hidden", "com.tcl.tv", "TVActivity", false));
        check(!DockPolicy.tab("off", "com.tcl.tv", "TVActivity", false));
        check(!DockPolicy.tab("tab", "com.tcl.tv", "TVActivity", true));
        check(!DockPolicy.tab("tab", "tv.afnon.home", "MainActivity", false));
        check(!DockPolicy.tab("tab", "com.android.tv.settings", "SettingsActivity", false));
        check(!DockPolicy.tab("tab", "com.google.android.backdrop", "DreamActivity", false));
        check(!DockPolicy.tab("tab", "", "", false));
        check(DockPolicy.validMode("hidden") && DockPolicy.validMode("tab") && DockPolicy.validMode("off"));
        check(!DockPolicy.validMode("invalid"));
        check(DockPolicy.validPosition("top") && DockPolicy.validPosition("bottom"));
        check(!DockPolicy.validPosition("invalid"));
    }
    private static void check(boolean value) { if (!value) throw new AssertionError("Prayer bar visibility"); }
}
