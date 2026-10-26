package tv.afnon.home;

/** Capture one complete press; a held key or an unmatched release must not assign a button. */
final class ButtonPress {
    private int held;
    boolean down(int code, int repeat) {
        if (code <= 0 || (held != 0 && held != code) || (held == 0 && repeat != 0)) return false;
        held = code;
        return true;
    }
    boolean holds(int code) { return held != 0 && held == code; }
    int up(int code, boolean canceled) {
        if (!holds(code)) return 0;
        held = 0;
        return canceled ? 0 : code;
    }
}
