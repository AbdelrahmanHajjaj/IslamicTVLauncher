package tv.afnon.core;

final class JavaInterop {
    static void check() {
        if (HomeNavigation.INSTANCE.initial(3, 0) != HomeNavigation.FIRST_APP
            || HomeNavigation.INSTANCE.move(HomeNavigation.FIRST_APP, 3, 0, 3, HomeNavigation.FIRST_APP, 0, 1, false) != HomeNavigation.ALL_APPS)
            throw new AssertionError("Java Home input contract");
        if (PrayerRules.INSTANCE.normalizeOffset(Integer.MIN_VALUE) != -30
            || PrayerRules.MAX_HIJRI_OFFSET_DAYS != 2) throw new AssertionError("Java settings contract");
        if (!ReminderRules.INSTANCE.dueReminder(300000, 0, 5) || ReminderRules.INSTANCE.minutesLeft(60001, 0) != 2)
            throw new AssertionError("Java reminder contract");
        PrayerRules.INSTANCE.validateOffsets(new int[] {-30, 30, 0, 0, 0, 0});
        PrayerSelection selected = PrayerRules.INSTANCE.select(
            new double[] {1000, 2000, 4000, 6000, 8000, 10000}, 12000, 10000);
        if (selected.getCurrent() != 5 || selected.getNext() != 0 || selected.getNextTime() != 12000) {
            throw new AssertionError("Java caller lost the next-day prayer");
        }
    }
}
