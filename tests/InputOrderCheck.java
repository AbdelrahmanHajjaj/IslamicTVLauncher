package tv.afnon.home;
public class InputOrderCheck {
    public static void main(String[] args) {
        check(InputOrder.betterTuner("com.tcl/TDTV/HW1413742848", null));
        check(InputOrder.betterTuner("com.tcl/TDTV/HW1413742848", "com.google/Preview"));
        check(!InputOrder.betterTuner("com.google/Preview", "com.tcl/TDTV/HW1413742848"));
        check(InputOrder.betterTuner("vendor/Digital/HW2", "vendor/Analog/HW1"));
        check(!InputOrder.betterTuner("vendor/Analog/HW1", "vendor/Digital/HW2"));
        check(InputOrder.betterTuner("vendor/Tuner/HW1", "vendor/Tuner/HW2"));
        check(!InputOrder.betterTuner("vendor/Tuner/HW2", "vendor/Tuner/HW1"));
    }
    private static void check(boolean value) { if (!value) throw new AssertionError("Wrong Live TV tuner"); }
}
