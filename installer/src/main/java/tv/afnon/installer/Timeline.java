package tv.afnon.installer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

/** The install steps as a list: each with a mark for waiting, working, done, failed, or skipped (optional). */
final class Timeline extends LinearLayout {
    enum State { WAITING, WORKING, DONE, FAILED, SKIPPED }

    private final Mark[] marks;
    private final TextView[] labels, notes;

    Timeline(Context c, String... steps) {
        super(c);
        setOrientation(VERTICAL);
        android.graphics.drawable.GradientDrawable bg = Ui.round(Ui.LAPIS_800, 18, c);
        bg.setStroke(Ui.dp(c, 1), Ui.LINE_SOFT);
        setBackground(bg);
        int pad = Ui.dp(c, 18);
        setPadding(pad, pad, pad, pad);
        marks = new Mark[steps.length];
        labels = new TextView[steps.length];
        notes = new TextView[steps.length];
        for (int i = 0; i < steps.length; i++) {
            LinearLayout row = Ui.row(c);
            row.setGravity(Gravity.TOP);
            row.setPadding(0, Ui.dp(c, i == 0 ? 0 : 6), 0, Ui.dp(c, 6));
            marks[i] = new Mark(c);
            int s = Ui.dp(c, 22);
            LayoutParams mp = new LayoutParams(s, s);
            mp.setMarginEnd(Ui.dp(c, 14));
            row.addView(marks[i], mp);
            LinearLayout words = Ui.column(c);
            labels[i] = Ui.text(c, steps[i], Ui.MIST, 15, Ui.medium());
            labels[i].setPadding(0, Ui.dp(c, 2), 0, 0);
            words.addView(labels[i]);
            notes[i] = Ui.small(c, "");
            notes[i].setVisibility(GONE);
            words.addView(notes[i], Ui.wide(c, 4));
            row.addView(words, Ui.fill(c));
            addView(row, Ui.wide(c, 0));
        }
    }

    void set(int i, State s) {
        marks[i].set(s);
        labels[i].setTextColor(s == State.WAITING ? Ui.MIST : s == State.FAILED ? Ui.CORAL : Ui.PLASTER);
        if (s != State.WORKING) note(i, null);
    }

    /** A lone spinning mark, for "waiting for…" lines. */
    static View spinner(Context c) {
        Mark m = new Mark(c);
        m.set(State.WORKING);
        return m;
    }

    void note(int i, String text) {
        notes[i].setText(text);
        notes[i].setVisibility(text == null ? GONE : VISIBLE);
    }

    private static final class Mark extends View {
        private State state = State.WAITING;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF r = new RectF();
        private final ValueAnimator spin = ValueAnimator.ofFloat(0, 360);
        private float angle;

        Mark(Context c) {
            super(c);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            spin.setDuration(900);
            spin.setRepeatCount(ValueAnimator.INFINITE);
            spin.setInterpolator(new LinearInterpolator());
            spin.addUpdateListener(a -> {
                angle = (float) a.getAnimatedValue();
                invalidate();
            });
        }

        void set(State s) {
            state = s;
            if (s == State.WORKING) {
                if (!spin.isStarted()) spin.start();
            } else {
                spin.cancel();
            }
            invalidate();
        }

        @Override protected void onDetachedFromWindow() {
            spin.cancel();
            super.onDetachedFromWindow();
        }

        @Override protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight(), sw = w * 0.1f;
            r.set(sw, sw, w - sw, h - sw);
            p.setStrokeWidth(sw);
            switch (state) {
                case WAITING:
                    p.setStyle(Paint.Style.STROKE);
                    p.setColor(0x669fb0cc);
                    c.drawOval(r, p);
                    break;
                case WORKING:
                    p.setStyle(Paint.Style.STROKE);
                    p.setColor(Ui.LAPIS_700);
                    c.drawOval(r, p);
                    p.setColor(Ui.GOLD_400);
                    c.drawArc(r, angle - 90, 100, false, p);
                    break;
                case DONE:
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(Ui.GOLD_400);
                    c.drawOval(r, p);
                    p.setStyle(Paint.Style.STROKE);
                    p.setColor(Ui.LAPIS_900);
                    p.setStrokeWidth(w * 0.12f);
                    Path tick = new Path();
                    tick.moveTo(w * 0.3f, h * 0.52f);
                    tick.lineTo(w * 0.45f, h * 0.66f);
                    tick.lineTo(w * 0.71f, h * 0.38f);
                    c.drawPath(tick, p);
                    break;
                case FAILED:
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(Ui.CORAL);
                    c.drawOval(r, p);
                    p.setStyle(Paint.Style.STROKE);
                    p.setColor(Ui.LAPIS_900);
                    p.setStrokeWidth(w * 0.12f);
                    c.drawLine(w * 0.36f, h * 0.36f, w * 0.64f, h * 0.64f, p);
                    c.drawLine(w * 0.64f, h * 0.36f, w * 0.36f, h * 0.64f, p);
                    break;
                case SKIPPED:
                    p.setStyle(Paint.Style.STROKE);
                    p.setColor(Ui.MIST);
                    c.drawOval(r, p);
                    c.drawLine(w * 0.34f, h * 0.5f, w * 0.66f, h * 0.5f, p);
                    break;
            }
        }
    }
}
