package tv.afnon.home;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ReplacementSpan;
import android.widget.TextView;
import android.view.View;

/** Inline vectors follow the text's colour, baseline and reading direction, including focus. */
final class PrayerIcon extends ReplacementSpan {
    private static final int[] DRAWABLES = {R.drawable.ic_prayer_fajr, R.drawable.ic_prayer_sunrise,
        R.drawable.ic_prayer_dhuhr, R.drawable.ic_prayer_asr, R.drawable.ic_prayer_maghrib, R.drawable.ic_prayer_isha};
    private final TextView view;
    private final int prayer;
    private final Drawable icon;

    private PrayerIcon(TextView view, int prayer) {
        this.view = view; this.prayer = prayer;
        icon = view.getContext().getDrawable(DRAWABLES[prayer]).mutate();
    }

    static void set(TextView view, int prayer, CharSequence text) {
        if (prayer < 0 || prayer >= DRAWABLES.length) throw new IllegalArgumentException("Invalid prayer icon");
        PrayerIcon icon = null;
        if (view.getText() instanceof Spanned) {
            Spanned previous = (Spanned) view.getText();
            for (PrayerIcon old : previous.getSpans(0, previous.length(), PrayerIcon.class))
                if (old.prayer == prayer) { icon = old; break; }
        }
        if (icon == null) icon = new PrayerIcon(view, prayer);
        SpannableStringBuilder label = new SpannableStringBuilder("\uFFFC").append(text);
        label.setSpan(icon, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(label);
        view.setContentDescription(text);
    }

    private float size(Paint paint) { return Math.min(paint.getTextSize(), Ui.px(38)); }

    @Override public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt metrics) {
        if (metrics != null) paint.getFontMetricsInt(metrics);
        return (int) Math.ceil(size(paint) + paint.getTextSize() * .24f);
    }

    @Override public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top,
            int y, int bottom, Paint paint) {
        int size = Math.round(size(paint));
        icon.setBounds(0, 0, size, size); icon.setTint(paint.getColor());
        float gap = view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? paint.getTextSize() * .24f : 0;
        canvas.save(); canvas.translate(x + gap, y + (paint.ascent() + paint.descent() - size) / 2);
        icon.draw(canvas); canvas.restore();
    }
}
