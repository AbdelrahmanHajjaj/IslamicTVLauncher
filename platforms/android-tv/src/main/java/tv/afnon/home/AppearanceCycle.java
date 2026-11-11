package tv.afnon.home;

import java.util.Random;

/** Rotation policy, independent of clocks, preferences and Android lifecycle. */
final class AppearanceCycle {
    static final int[] MINUTES = {0, Tokens.APPEARANCE_CYCLE_SHORT_MIN, Tokens.APPEARANCE_CYCLE_LONG_MIN};

    static int minutes(int value) { return value == MINUTES[1] || value == MINUTES[2] ? value : 0; }
    static long next(long now, int value) { return minutes(value) == 0 ? 0 : now + minutes(value) * 60_000L; }
    static boolean needsStart(long now, long at, int value) {
        return minutes(value) != 0 && (at <= 0 || at > next(now, value));
    }
    static boolean due(long now, long at, int value) { return minutes(value) != 0 && at > 0 && now >= at; }

    /** Uniform selection without an immediate repeat; unknown/imported selections can pick any item. */
    static int pick(int count, int current, Random random) {
        if (count <= 1) return 0;
        if (current < 0 || current >= count) return random.nextInt(count);
        int pick = random.nextInt(count - 1);
        return pick >= current ? pick + 1 : pick;
    }
}
