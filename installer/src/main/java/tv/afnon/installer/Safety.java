package tv.afnon.installer;

/** Pure validation used before any package or Home mutation. */
final class Safety {
    static boolean packageName(String p) {
        return p != null && p.matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+");
    }
    static boolean protectedPackage(String p) {
        return !packageName(p) || p.equals("tv.afnon.home")
            || p.matches("com\\.android\\.(systemui|shell|phone|settings|tv\\.settings|vending|externalstorage|providers\\..*|inputmethod\\..*|networkstack.*)")
            || p.matches("com\\.(google\\.)?android\\.(permissioncontroller|packageinstaller|ext\\.services|ext\\.shared|modulemetadata|overlay\\.modules.*|gms|gsf|inputmethod\\..*|tv\\.remote\\.service)")
            || p.contains("bluetooth") || p.contains("inputmethod") || p.contains("networkstack");
    }
    static boolean home(String value) {
        if (value == null || !value.matches("[A-Za-z_][\\w.]*/[\\w.$]+")) return false;
        String lower = value.toLowerCase(java.util.Locale.ROOT);
        for (String forbidden : new String[]{"factory", "hkcfac", "fallbackhome", "resolveractivity", "setupwraith", "provision"})
            if (lower.contains(forbidden)) return false;
        return true;
    }
    static boolean sameHome(String a, String b) { return canonical(a).equals(canonical(b)); }
    private static String canonical(String s) {
        int at = s.indexOf("/.");
        return at < 0 ? s : s.substring(0, at + 1) + s.substring(0, at) + s.substring(at + 1);
    }
}
