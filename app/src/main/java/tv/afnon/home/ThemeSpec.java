package tv.afnon.home;

import java.util.Map;

/** Bounds and readability checks, independent of Android for local tests. */
final class ThemeSpec {
    static final String[] SURFACES = {"classic", "glass", "cinema", "outline", "blocks", "soft", "frame"};
    static void surface(String name) {
        if (!java.util.Arrays.asList(SURFACES).contains(name)) throw new IllegalArgumentException("Unknown surface");
    }
    static final String[] BACKGROUNDS = {"lattice", "solid", "panorama", "daylight", "mountains", "dunes", "halo", "night_sky", "arches", "eight_point_stars", "mashrabiya", "arabesque", "star_tiles", "rosettes", "octagon_lattice", "corner_medallions"};
    static final String[] COLORS = {"lapis-950", "lapis-900", "lapis-800", "lapis-700", "gold-400", "gold-200", "plaster-100", "mist-400"};
    static int color(String value) {
        if (value == null || !value.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException("Use #RRGGBB colours");
        return (int)(0xff000000L | Long.parseLong(value.substring(1), 16));
    }
    static void validate(Map<String, Integer> colors, String background, int light) {
        if (!java.util.Arrays.asList(BACKGROUNDS).contains(background))
            throw new IllegalArgumentException("Unknown background");
        if (light < 0 || light > 100) throw new IllegalArgumentException("Lighting must be 0..100");
        for (String key : colors.keySet()) if (!java.util.Arrays.asList(COLORS).contains(key))
            throw new IllegalArgumentException("Unknown colour: " + key);
        for (String ground : new String[]{"lapis-950", "lapis-900", "lapis-800", "lapis-700"}) {
            for (String ink : new String[]{"plaster-100", "mist-400", "gold-400"})
                if (contrast(colors.get(ground), colors.get(ink)) < 4.5)
                    throw new IllegalArgumentException("Insufficient contrast: " + ink + " on " + ground);
        }
    }
    static void validate(Map<String, Integer> colors, String background, int light, String surface) {
        surface(surface);
        validate(colors, background, light);
        if ("glass".equals(surface)) {
            // Even over a white pixel the translucent panel must keep its text legible.
            int composite = ThemeSurface.mix(0xffffffff, colors.get("lapis-800"), Tokens.SURFACE_GLASS_OPACITY);
            for (String ink : new String[]{"plaster-100", "mist-400", "gold-400"})
                if (contrast(composite, colors.get(ink)) < 4.5)
                    throw new IllegalArgumentException("Insufficient glass contrast: " + ink);
        }
    }
    static double contrast(int a, int b) {
        double x = luminance(a), y = luminance(b);
        return (Math.max(x, y) + .05) / (Math.min(x, y) + .05);
    }
    private static double luminance(int color) {
        double[] rgb = {(color >> 16 & 255) / 255.0, (color >> 8 & 255) / 255.0, (color & 255) / 255.0};
        for (int i = 0; i < 3; i++) rgb[i] = rgb[i] <= .04045 ? rgb[i] / 12.92 : Math.pow((rgb[i] + .055) / 1.055, 2.4);
        return rgb[0] * .2126 + rgb[1] * .7152 + rgb[2] * .0722;
    }
}
