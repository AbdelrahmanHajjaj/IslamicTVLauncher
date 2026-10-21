package tv.afnon.home;

import android.graphics.Color;

/** Values from afnon-mihrab/tokens.json, in px at 1920×1080. Multiply lengths by {@link Ui#px}. */
final class Tokens {
    private Tokens() {}

    static final int LAPIS_900 = 0xFF0C1B33;
    static final int LAPIS_800 = 0xFF12264A;
    static final int LAPIS_700 = 0xFF1A3360;
    static final int GOLD_400 = 0xFFD8B46A;
    static final int GOLD_200 = 0xFFF0D79C;
    static final int PLASTER_100 = 0xFFEFE7D6;
    static final int MIST_400 = 0xFF9FB0CC;
    /** lapis-800 at 60%: past prayer cells. */
    static final int LAPIS_800_60 = Color.argb(153, 0x12, 0x26, 0x4A);
    /** border.chip: rgba(239,231,214,0.25). */
    static final int LINE_SOFT = Color.argb(64, 239, 231, 214);
    /** Arch foot button border at rest: gold-400 at 60%. */
    static final int GOLD_400_60 = Color.argb(153, 0xD8, 0xB4, 0x6A);
    /** Inner arch line: rgba(216,180,106,.45). */
    static final int ARCH_INNER = Color.argb(115, 0xD8, 0xB4, 0x6A);
    /** Dashed "All apps" border: rgba(239,231,214,.3). */
    static final int LINE_DASH = Color.argb(77, 239, 231, 214);

    // focus.*
    static final float FOCUS_RING_WIDTH = 4;
    static final float FOCUS_RING_OFFSET = 6;
    static final float FOCUS_SCALE = 1.06f;
    static final long DUR_FOCUS_MS = 120;

    // radius.*
    static final float RADIUS_CELL = 14;
    static final float RADIUS_TILE = 18;

    // layout.*
    static final float ARCH_X = 96, ARCH_Y = 120, ARCH_W = 600, ARCH_H = 900;
    static final float COURT_X = 760, COURT_W = 1064;

    /** Minutes before a prayer when the arch switches to its approaching outline. */
    static final int APPROACH_MIN = 15;
}
