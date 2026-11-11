package tv.afnon.home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.View;
import java.util.HashMap;
import java.util.Map;

/** App colours sampled once per Home build; only a focus change animates the light. */
final class FocusGlow extends View {
    private final Map<String, Integer> colours = new HashMap<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator transition;
    private int colour, target;

    FocusGlow(Context context) { super(context); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }

    void show(String pkg) {
        int next = 0;
        if (pkg != null) {
            Integer sampled = colours.get(pkg);
            if (sampled == null) { sampled = sample(pkg); colours.put(pkg, sampled); }
            int strength = Math.max(0, Math.min(100, Prefs.of(getContext()).getInt("lighting", 35)));
            next = (strength * 70 / 100 << 24) | (sampled & 0xffffff);
        }
        if (target == next) return;
        target = next;
        if (transition != null) transition.cancel();
        if (Appearance.reduced(getContext())) { colour = next; shader(); return; }
        transition = ValueAnimator.ofArgb(colour, next);
        transition.setDuration(280);
        transition.addUpdateListener(a -> { colour = (Integer) a.getAnimatedValue(); shader(); });
        transition.start();
    }

    void stop() { if (transition != null) transition.end(); }
    @Override protected void onDetachedFromWindow() { if (transition != null) transition.cancel(); super.onDetachedFromWindow(); }
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) { shader(); }
    private void shader() {
        if (getWidth() <= 0) return;
        float x = getLayoutDirection() == LAYOUT_DIRECTION_RTL ? .25f : .75f;
        paint.setShader(new RadialGradient(getWidth() * x, getHeight() * .5f, getWidth() * .65f,
            new int[] {colour, colour & 0xffffff}, null, Shader.TileMode.CLAMP));
        invalidate();
    }
    @Override protected void onDraw(Canvas canvas) { if (Color.alpha(colour) != 0) canvas.drawRect(0, 0, getWidth(), getHeight(), paint); }

    /** Adapted from CleanYourTV AppAdapter.vividColor (Gathin, MIT); see docs/CLEANYOURTV-LICENSE.txt. */
    private int sample(String pkg) {
        Drawable live;
        try {
            live = getContext().getPackageManager().getApplicationBanner(pkg);
            if (live == null) live = Apps.icon(getContext(), pkg);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) { return Palette.GOLD_400; }
        if (live == null) return Palette.GOLD_400;
        Drawable d = live.getConstantState() != null ? live.getConstantState().newDrawable().mutate() : live;
        Rect saved = new Rect(d.getBounds());
        Bitmap image = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[144];
        try {
            d.setBounds(0, 0, 12, 12);
            d.draw(new Canvas(image));
            image.getPixels(pixels, 0, 12, 0, 0, 12, 12);
        } catch (RuntimeException e) { return Palette.GOLD_400; }
        finally { d.setBounds(saved); image.recycle(); }
        int best = Palette.GOLD_400, count = 0;
        float bestScore = -1;
        long red = 0, green = 0, blue = 0;
        for (int p : pixels) {
            if (Color.alpha(p) < 128) continue;
            int r = Color.red(p), g = Color.green(p), b = Color.blue(p);
            red += r; green += g; blue += b; count++;
            int max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
            float saturation = max == 0 ? 0 : (max - min) / (float) max;
            float score = saturation * Math.min(1f, max / 255f * 1.4f);
            if (score > bestScore) { bestScore = score; best = p; }
        }
        if (count == 0) return Palette.GOLD_400;
        if (bestScore < .25f) best = Color.rgb((int)(red / count), (int)(green / count), (int)(blue / count));
        float[] hsv = new float[3];
        Color.colorToHSV(best, hsv);
        hsv[1] = Math.min(1, hsv[1] * 1.1f);
        hsv[2] = Math.max(.5f, hsv[2]);
        return Color.HSVToColor(hsv);
    }
}
