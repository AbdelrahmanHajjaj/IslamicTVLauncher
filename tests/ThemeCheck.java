package tv.afnon.home;
import java.util.*;
public final class ThemeCheck {
    static void rejects(Runnable test) { try { test.run(); } catch (IllegalArgumentException expected) { return; } throw new AssertionError("Accepted invalid theme"); }
    public static void main(String[] args) {
        String[] defaults = {"#081224", "#0C1B33", "#12264A", "#1A3360", "#D8B46A", "#F0D79C", "#EFE7D6", "#9FB0CC"};
        Map<String, Integer> colors = new HashMap<>();
        for (int i = 0; i < 8; i++) colors.put(ThemeSpec.COLORS[i], ThemeSpec.color(args.length >= 8 ? args[i] : defaults[i]));
        for (String background : ThemeSpec.BACKGROUNDS) {
            ThemeSpec.validate(colors, background, 0);
            ThemeSpec.validate(colors, background, 100);
        }
        if (args.length > 8) ThemeSpec.validate(colors, args[8], 35);
        rejects(() -> ThemeSpec.color("url(http://invalid)"));
        rejects(() -> ThemeSpec.color("#FFFFFF00"));
        rejects(() -> ThemeSpec.validate(colors, "video", 35));
        rejects(() -> ThemeSpec.validate(colors, "solid", 101));
        colors.put("plaster-100", colors.get("lapis-900"));
        rejects(() -> ThemeSpec.validate(colors, "solid", 0));
    }
}
