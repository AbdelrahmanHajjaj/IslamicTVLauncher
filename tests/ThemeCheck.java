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
        if (args.length > 8) ThemeSpec.validate(colors, args[8], 35, args[9]);
        rejects(() -> ThemeSpec.surface("blur"));
        int[] palette = new int[8];
        for (int i = 0; i < 8; i++) palette[i] = colors.get(ThemeSpec.COLORS[i]);
        ThemeSurface original = new ThemeSurface("classic", palette);
        for (int color : palette) if (original.fill(color) != color) throw new AssertionError("Classic changed");
        for (String name : ThemeSpec.SURFACES) {
            ThemeSurface surface = new ThemeSurface(name, palette);
            // Current prayer and selected tile colours retain their solid, contrasting semantics.
            for (int i = 4; i < 8; i++) if (surface.fill(palette[i]) != palette[i]) throw new AssertionError("State colour changed");
            if (surface.fill(0) != 0) throw new AssertionError("Transparent control changed");
        }
        rejects(() -> ThemeSpec.color("url(http://invalid)"));
        rejects(() -> ThemeSpec.color("#FFFFFF00"));
        rejects(() -> ThemeSpec.validate(colors, "video", 35));
        rejects(() -> ThemeSpec.validate(colors, "solid", 101));
        colors.put("plaster-100", colors.get("lapis-900"));
        rejects(() -> ThemeSpec.validate(colors, "solid", 0));
    }
}
