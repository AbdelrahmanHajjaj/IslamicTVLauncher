package tv.afnon.home;

/** Small, static surface treatments. No filters, textures, extra layers or animation. */
final class ThemeSurface {
    final String name;
    final float radius, stroke, opacity;
    final int panel, edge;
    private final int[] colors;

    ThemeSurface(String name, int[] colors) {
        this.name = name;
        this.colors = colors;
        float r = -1, s = 0, a = 1;
        int p = colors[2], e = colors[4];
        switch (name) {
            case "glass":
                r = Tokens.SURFACE_GLASS_RADIUS; s = Tokens.SURFACE_GLASS_STROKE;
                a = Tokens.SURFACE_GLASS_OPACITY;
                e = mix(p, colors[6], Tokens.SURFACE_GLASS_EDGE); break;
            case "cinema":
                r = Tokens.SURFACE_CINEMA_RADIUS; p = colors[0]; break;
            case "outline":
                r = Tokens.SURFACE_OUTLINE_RADIUS; s = Tokens.SURFACE_OUTLINE_STROKE; p = colors[0]; break;
            case "blocks":
                r = Tokens.SURFACE_BLOCKS_RADIUS; p = colors[3]; break;
            case "soft":
                r = Tokens.SURFACE_SOFT_RADIUS; break;
            case "frame":
                r = Tokens.SURFACE_FRAME_RADIUS; s = Tokens.SURFACE_FRAME_STROKE; p = colors[1]; break;
            default: break;
        }
        radius = r; stroke = s; opacity = a; panel = p; edge = e;
    }

    boolean classic() { return "classic".equals(name); }
    boolean modern() { return !classic() && !"frame".equals(name); }
    float radius(float original) { return radius < 0 ? original : radius; }
    boolean isPanel(int color) {
        return color != 0 && ((color | 0xff000000) == colors[2] || (color | 0xff000000) == colors[3]);
    }
    int fill(int color) {
        if (classic() || !isPanel(color)) return color;
        return (Math.round((color >>> 24) * opacity) << 24) | (panel & 0xffffff);
    }
    int archEdge() {
        if (classic()) return colors[4];
        if ("cinema".equals(name) || "soft".equals(name)) return panel;
        return edge;
    }
    float archStroke() { return classic() ? Tokens.SURFACE_ARCH_STROKE : stroke; }
    static int mix(int a, int b, float amount) {
        int out = 0xff000000;
        for (int shift = 0; shift <= 16; shift += 8)
            out |= (int)(((a >> shift) & 255) * (1 - amount) + ((b >> shift) & 255) * amount) << shift;
        return out;
    }
}
