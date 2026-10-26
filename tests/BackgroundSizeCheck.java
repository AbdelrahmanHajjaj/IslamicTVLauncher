package tv.afnon.home;

public final class BackgroundSizeCheck {
    public static void main(String[] args) {
        check(1920, 1080, 1920, 1080);
        check(1280, 720, 1280, 720);
        check(3840, 2160, 1920, 1080);
        check(7680, 4320, 1920, 1080);
        check(2560, 1080, 1920, 810);
        check(0, 0, 1, 1);
        check(-1, -1, 1, 1);
    }

    private static void check(int w, int h, int expectedW, int expectedH) {
        BackgroundSize size = new BackgroundSize(w, h);
        if (size.width != expectedW || size.height != expectedH)
            throw new AssertionError(w + "x" + h + " became " + size.width + "x" + size.height);
        if ((long)size.width * size.height * 4 > 8_294_400L)
            throw new AssertionError("Background exceeded the Full HD memory budget");
    }
}
