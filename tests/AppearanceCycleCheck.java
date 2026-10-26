package tv.afnon.home;

import java.util.Random;

public final class AppearanceCycleCheck {
    public static void main(String[] args) {
        long now = 1_800_000_000_000L;
        for (int minutes : new int[]{15, 30}) {
            long at = AppearanceCycle.next(now, minutes);
            check(at - now == minutes * 60_000L, "interval");
            check(!AppearanceCycle.due(at - 1, at, minutes), "not early");
            check(AppearanceCycle.due(at, at, minutes), "exact boundary");
            check(AppearanceCycle.due(at + 86_400_000L, at, minutes), "return after a day away");
            check(!AppearanceCycle.due(at, AppearanceCycle.next(at, minutes), minutes), "one rotation only");
            check(AppearanceCycle.needsStart(now, 0, minutes), "missing schedule");
            check(AppearanceCycle.needsStart(now - 60_000, at, minutes), "clock moved back");
            check(!AppearanceCycle.needsStart(now, at, minutes), "saved deadline survives restart");
        }
        for (int value : new int[]{0, -1, 1, 60, Integer.MAX_VALUE}) {
            check(AppearanceCycle.next(now, value) == 0, "off or unsupported");
            check(!AppearanceCycle.due(now, now - 1, value), "off never rotates");
        }
        check(AppearanceCycle.next(now, 30) - AppearanceCycle.next(now, 15) == 900_000, "interval change");
        Random random = new Random(42);
        for (int count : new int[]{16, 20}) for (int current = -1; current <= count; current++) {
            boolean[] seen = new boolean[count];
            for (int i = 0; i < 1000; i++) {
                int picked = AppearanceCycle.pick(count, current, random);
                check(picked >= 0 && picked < count && picked != current, "valid non-repeating choice");
                seen[picked] = true;
            }
            for (int i = 0; i < count; i++) if (i != current) check(seen[i], "all alternatives reachable");
        }
    }
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
