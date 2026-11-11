package tv.afnon.core;

final class JavaInterop {
    static void check() {
        PrayerRules.INSTANCE.validateOffsets(new int[] {-30, 30, 0, 0, 0, 0});
        PrayerSelection selected = PrayerRules.INSTANCE.select(
            new double[] {1000, 2000, 4000, 6000, 8000, 10000}, 12000, 10000);
        if (selected.getCurrent() != 5 || selected.getNext() != 0 || selected.getNextTime() != 12000) {
            throw new AssertionError("Java caller lost the next-day prayer");
        }
    }
}
