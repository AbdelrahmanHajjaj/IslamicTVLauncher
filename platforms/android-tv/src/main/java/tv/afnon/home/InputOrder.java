package tv.afnon.home;

import java.util.Locale;

/** Hardware tuners take priority over preview services; prefer digital over analog. */
final class InputOrder {
    static boolean betterTuner(String candidate, String current) {
        if (current == null) return true;
        boolean hardware = candidate.matches(".*/HW\\d+$"), previousHardware = current.matches(".*/HW\\d+$");
        if (hardware != previousHardware) return hardware;
        boolean analog = candidate.toLowerCase(Locale.ROOT).contains("analog");
        boolean previousAnalog = current.toLowerCase(Locale.ROOT).contains("analog");
        return analog != previousAnalog ? !analog : candidate.compareTo(current) < 0;
    }
}
