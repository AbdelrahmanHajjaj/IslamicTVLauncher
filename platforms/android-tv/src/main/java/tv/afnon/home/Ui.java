package tv.afnon.home;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

/** Scaling from design px (1920×1080) to screen px, plus the shared motion curve and background. */
final class Ui {
    private Ui() {}

    static float scale = 1f;

    static void init(Context c) {
        Appearance.load(c);
        scale = c.getResources().getDisplayMetrics().widthPixels / 1920f;
    }

    static float px(float designPx) { return designPx * scale; }

    static int ipx(float designPx) { return Math.round(designPx * scale); }

    static float factor(Context c) { return Prefs.uiScale(c) / 100f; }

    static float safeInline(Context c) {
        float factor = factor(c);
        if (factor <= 1f) return Tokens.SPACE_SAFE_INLINE;
        return Math.max(Tokens.SPACE_SAFE_INLINE, 960 - 960 / factor + FocusTile.REACH
            + (Tokens.LAYOUT_HOME_TILE_MAX_WIDTH + 2 * FocusTile.REACH) * (Tokens.FOCUS_SCALE - 1) / 2 + Tokens.SPACE_2 / factor);
    }

    /** Window width stays physical; measure enough logical content for its scaled height and touch bounds. */
    static View sizedPanel(View content) {
        float factor = factor(content.getContext());
        if (factor == 1f) return content;
        FrameLayout viewport = new FrameLayout(content.getContext()) {
            @Override protected void onMeasure(int widthSpec, int heightSpec) {
                int width = MeasureSpec.getSize(widthSpec);
                int childHeightSpec = MeasureSpec.getMode(heightSpec) == MeasureSpec.UNSPECIFIED ? heightSpec
                    : MeasureSpec.makeMeasureSpec(Math.round(MeasureSpec.getSize(heightSpec) / factor), MeasureSpec.AT_MOST);
                content.measure(MeasureSpec.makeMeasureSpec(Math.round(width / factor), MeasureSpec.EXACTLY), childHeightSpec);
                setMeasuredDimension(resolveSize(width, widthSpec), resolveSize((int) Math.ceil(content.getMeasuredHeight() * factor), heightSpec));
            }
        };
        int direction = content.getLayoutDirection();
        viewport.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        content.setLayoutDirection(direction);
        content.setPivotX(0); content.setPivotY(0);
        content.setScaleX(factor); content.setScaleY(factor);
        viewport.addView(content, new FrameLayout.LayoutParams(-1, -2, Gravity.TOP | Gravity.START));
        return viewport;
    }

    /** Local dialog resources preserve system font scale and the Activity's window token. */
    static Context dialogContext(Context c) {
        float factor = factor(c);
        if (factor == 1f) return c;
        Configuration configuration = new Configuration(c.getResources().getConfiguration());
        configuration.densityDpi = Math.round(configuration.densityDpi * factor);
        ContextThemeWrapper themed = new ContextThemeWrapper(c, R.style.Theme_Afnon);
        themed.applyOverrideConfiguration(configuration);
        return themed;
    }

    /** motion.easing-standard: cubic-bezier(0.2, 0.8, 0.2, 1). */
    static final Interpolator EASE = new PathInterpolator(0.2f, 0.8f, 0.2f, 1f);

    /**
     * The eight-point star lattice on the lapis ground: one 96px tile (two squares, one turned 45°, and four
     * spokes) in gold-400 at 7%, repeated. Drawn once into a bitmap, so it costs nothing per frame.
     */
    static Drawable starLattice(Context c) {
        return starLattice(c, Palette.LAPIS_900, Palette.PATTERN_LINE, 96);
    }

    static Drawable starLattice(Context c, int ground, int line, float cell) {
        int s = Math.max(1, ipx(cell));
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        cv.drawColor(ground);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(px(1.5f * cell / 96));
        p.setColor(line);
        float k = s / 96f;
        cv.drawRect(28 * k, 28 * k, 68 * k, 68 * k, p);
        cv.save();
        cv.rotate(45, 48 * k, 48 * k);
        cv.drawRect(28 * k, 28 * k, 68 * k, 68 * k, p);
        cv.restore();
        cv.drawLine(0, 48 * k, 20 * k, 48 * k, p);
        cv.drawLine(76 * k, 48 * k, 96 * k, 48 * k, p);
        cv.drawLine(48 * k, 0, 48 * k, 20 * k, p);
        cv.drawLine(48 * k, 76 * k, 48 * k, 96 * k, p);
        BitmapDrawable d = new BitmapDrawable(c.getResources(), b);
        d.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        return d;
    }
}
