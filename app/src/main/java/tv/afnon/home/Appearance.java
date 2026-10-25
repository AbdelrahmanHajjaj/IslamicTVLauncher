package tv.afnon.home;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import org.json.JSONObject;
import java.util.*;

/** Local themes; static 480x270 ambient art shares the existing foreground clock tick. */
final class Appearance {
    static final String[] PRESETS = {"mihrab", "aurora", "ember", "forest", "custom"};
    private static final int[] BASE = {Tokens.LAPIS_950, Tokens.LAPIS_900, Tokens.LAPIS_800, Tokens.LAPIS_700,
        Tokens.GOLD_400, Tokens.GOLD_200, Tokens.PLASTER_100, Tokens.MIST_400};
    private static String loaded = "";
    private static boolean reduceMotion;
    static long motion(long duration) { return reduceMotion ? 0 : duration; }
    private static String mode = "lattice";
    private static int lighting = 35;
    private static String bitmapKey = "";
    private static Bitmap bitmap;

    static String key(Context c) {
        return Prefs.of(c).getString("theme", "mihrab") + ":" + Prefs.of(c).getString("theme_json", "")
            + ":" + Prefs.of(c).getString("background", "theme") + ":" + Prefs.of(c).getInt("lighting", -1);
    }
    static int tint(int color, Map<Integer, Integer> colors) {
        return (color & 0xff000000) | (colors.getOrDefault(color | 0xff000000, color) & 0xffffff);
    }
    static boolean reduced(Context c) { return Prefs.of(c).getBoolean("reduce_motion", false); }

    static JSONObject checked(String json) throws Exception {
        if (json.length() > 16384) throw new IllegalArgumentException("Theme is too large");
        JSONObject object = new JSONObject(json);
        Set<String> known = new HashSet<>(Arrays.asList("version", "colors", "background", "lighting"));
        for (Iterator<String> it = object.keys(); it.hasNext();) if (!known.contains(it.next())) throw new IllegalArgumentException("Unknown theme field");
        if (!(object.get("version") instanceof Integer) || object.getInt("version") != 1) throw new IllegalArgumentException("Expected theme version 1");
        if (object.has("lighting") && !(object.get("lighting") instanceof Integer)) throw new IllegalArgumentException("Lighting must be an integer");
        if (object.has("background") && !(object.get("background") instanceof String)) throw new IllegalArgumentException("Background must be text");
        Map<String, Integer> colors = new LinkedHashMap<>();
        for (int i = 0; i < BASE.length; i++) colors.put(ThemeSpec.COLORS[i], BASE[i]);
        JSONObject changes = object.has("colors") ? object.getJSONObject("colors") : new JSONObject();
        for (Iterator<String> it = changes.keys(); it.hasNext();) {
            String name = it.next();
            if (!(changes.get(name) instanceof String)) throw new IllegalArgumentException("Colour must be text");
            colors.put(name, ThemeSpec.color(changes.getString(name)));
        }
        ThemeSpec.validate(colors, object.optString("background", "lattice"), object.optInt("lighting", 35));
        return object;
    }

    static void importTheme(Context c, String json) throws Exception {
        String checked = checked(json).toString();
        if (!Prefs.of(c).edit().putString("theme_json", checked).putString("theme", "custom")
                .remove("background").remove("lighting").commit()) throw new java.io.IOException("Could not save theme");
    }

    private static byte[] read(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] bytes = new byte[4096]; int n;
        while ((n = in.read(bytes)) != -1) {
            if (out.size() + n > 16384) throw new java.io.IOException("Theme too large");
            out.write(bytes, 0, n);
        }
        return out.toByteArray();
    }

    static void load(Context c) {
        reduceMotion = reduced(c);
        String key = key(c);
        if (key.equals(loaded)) return;
        Map<Integer, Integer> colors = new HashMap<>();
        mode = "lattice"; lighting = 35;
        String name = Prefs.of(c).getString("theme", "mihrab");
        try {
            JSONObject object;
            if ("custom".equals(name)) object = checked(Prefs.of(c).getString("theme_json", "{}"));
            else if (Arrays.asList(PRESETS).contains(name) && !"mihrab".equals(name)) {
                try (java.io.InputStream in = c.getAssets().open("themes/" + name + ".json")) {
                    object = checked(new String(read(in), java.nio.charset.StandardCharsets.UTF_8));
                }
            } else object = checked("{\"version\":1}");
            JSONObject changes = object.optJSONObject("colors");
            for (int i = 0; changes != null && i < BASE.length; i++)
                if (changes.has(ThemeSpec.COLORS[i])) colors.put(BASE[i], ThemeSpec.color(changes.getString(ThemeSpec.COLORS[i])));
            mode = object.optString("background", "lattice"); lighting = object.optInt("lighting", 35);
        } catch (Exception ignored) { /* Invalid saved data falls back to the original Mihrab. */ }
        String background = Prefs.of(c).getString("background", "theme");
        if (Arrays.asList("lattice", "solid", "panorama", "daylight").contains(background)) mode = background;
        int light = Prefs.of(c).getInt("lighting", -1);
        if (light >= 0 && light <= 100) lighting = light;
        Palette.apply(colors);
        loaded = key;
        bitmapKey = "";
    }

    static String backgroundKey(Context c) {
        load(c);
        return loaded + ("daylight".equals(mode) ? ":" + System.currentTimeMillis() / 60000 : "");
    }

    static Drawable background(Context c) {
        load(c);
        if ("lattice".equals(mode)) return Ui.starLattice(c);
        if ("solid".equals(mode)) return new android.graphics.drawable.ColorDrawable(Palette.LAPIS_900);
        String key = backgroundKey(c);
        if (bitmap == null || !key.equals(bitmapKey)) {
            Bitmap next = Bitmap.createBitmap(480, 270, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(next);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            float hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + Calendar.getInstance().get(Calendar.MINUTE) / 60f;
            float daylight = "daylight".equals(mode) ? Math.max(.15f, (float)Math.sin((hour - 6) / 12 * Math.PI)) : .7f;
            int glow = blend(Palette.LAPIS_900, Palette.GOLD_400, lighting / 100f * .3f * daylight);
            p.setShader(new LinearGradient(0, 0, 480, 270, new int[]{Palette.LAPIS_950, Palette.LAPIS_900, glow}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, 480, 270, p);
            p.setShader(new RadialGradient(360, 190, 210, glow, 0, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, 480, 270, p);
            p.setShader(null);
            p.setColor(blend(Palette.LAPIS_950, glow, .18f));
            Path ridge = new Path(); ridge.moveTo(0, 225); ridge.cubicTo(110, 160, 160, 265, 280, 215);
            ridge.cubicTo(360, 180, 400, 210, 480, 155); ridge.lineTo(480, 270); ridge.lineTo(0, 270); ridge.close();
            canvas.drawPath(ridge, p);
            bitmap = next; bitmapKey = key;
        }
        BitmapDrawable result = new BitmapDrawable(c.getResources(), bitmap);
        result.setFilterBitmap(true);
        return result;
    }
    private static int blend(int a, int b, float f) {
        return Color.rgb((int)(Color.red(a) * (1 - f) + Color.red(b) * f),
            (int)(Color.green(a) * (1 - f) + Color.green(b) * f), (int)(Color.blue(a) * (1 - f) + Color.blue(b) * f));
    }
}
