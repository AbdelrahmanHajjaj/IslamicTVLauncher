package tv.afnon.home;
public final class ButtonPressCheck {
    static void check(boolean ok) { if (!ok) throw new AssertionError("Incorrect remote press capture"); }
    public static void main(String[] args) {
        ButtonPress press = new ButtonPress();
        check(press.up(4159, false) == 0);
        check(!press.down(4159, 2));
        check(press.down(4159, 0));
        check(press.down(4159, 1));
        check(!press.down(4062, 0));
        check(press.up(4062, false) == 0 && press.holds(4159));
        check(press.up(4159, false) == 4159 && !press.holds(4159));
        check(press.up(4159, false) == 0);
        check(press.down(4062, 0));
        check(press.up(4062, true) == 0 && !press.holds(4062));
        check(press.down(4159, 0) && press.up(4159, false) == 4159);
        check(!press.down(0, 0));
    }
}
