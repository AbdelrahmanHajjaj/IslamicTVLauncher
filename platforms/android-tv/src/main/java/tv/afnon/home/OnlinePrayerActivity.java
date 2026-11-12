package tv.afnon.home;

import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.LocalDate;
import java.util.Date;

/** A manual advisory snapshot, with no schedule/settings editor. */
public class OnlinePrayerActivity extends Screen {
    private FocusTile back, retry;
    private TextView metadata, status;
    private LinearLayout table;
    private OnlinePrayer request;
    private OnlinePrayer.Result result;
    private boolean requested, resumed, failed;

    @Override protected void onCreate(Bundle state) {
        requested = state != null;
        failed = requested;
        super.onCreate(state);
    }

    @Override protected void onResume() {
        super.onResume(); resumed = true;
        if (!requested) { requested = true; checkOnline(); }
        else renderReport();
    }

    @Override protected void onPause() {
        resumed = false; cancelRequest(); super.onPause();
    }

    @Override void build() {
        newRoot();
        back = new FocusTile(this, 36);
        back.setRest(Palette.LAPIS_800, Palette.LINE_SOFT, 2);
        Icon arrow = new Icon(this, Icon.BACK, 2f);
        back.addView(arrow, new FrameLayout.LayoutParams(Ui.ipx(32), Ui.ipx(32), Gravity.CENTER));
        back.setLook(f -> arrow.setColor(f ? Palette.LAPIS_900 : Palette.PLASTER_100));
        back.setContentDescription(t.back()); back.setOnClickListener(v -> finish());
        placeTile(back, 96, 56, 72, 72);
        TextView title = text(displayBold, 52, Palette.PLASTER_100);
        title.setText(t.checkOnline());
        LinearLayout heading = new LinearLayout(this);
        heading.addView(title); lineHeight(title, 1.1f); heading.setClipChildren(false);
        place(heading, 208, 54, 1512, -2);
        metadata = text(ui400, 24, Palette.MIST_400);
        place(metadata, 96, 172, 1728, 100);
        status = text(ui400, 32, Palette.PLASTER_100);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        place(status, 96, 288, 1728, 140);
        table = new LinearLayout(this); table.setOrientation(LinearLayout.VERTICAL);
        place(table, 96, 284, 1728, 490);
        TextView note = text(ui400, 24, Palette.MIST_400);
        note.setText(t.onlineAdvisory()); place(note, 96, 800, 1728, 108);
        retry = new FocusTile(this, 16);
        retry.setRest(Palette.LAPIS_800, Palette.LINE_SOFT, 2);
        TextView label = text(ui500, 28, Palette.PLASTER_100);
        label.setGravity(Gravity.CENTER); label.setText(t.checkOnline());
        retry.addView(label, new FrameLayout.LayoutParams(-1, -1));
        retry.setLook(f -> label.setTextColor(f ? Palette.LAPIS_900 : Palette.PLASTER_100));
        retry.setContentDescription(t.checkOnline()); retry.setOnClickListener(v -> checkOnline());
        placeTile(retry, 96, 934, 580, 72);
        renderReport(); setContentView(root); back.requestFocus();
    }

    private void checkOnline() {
        cancelRequest(); result = null; failed = false;
        OnlinePrayer started = new OnlinePrayer(Prefs.city(this), Prefs.method(this), Prefs.madhab(this),
            Prefs.prayerOffsets(this), LocalDate.now());
        request = started; renderReport(); back.requestFocus();
        new Thread(() -> {
            OnlinePrayer.Result value;
            try { value = started.load(); } catch (Exception error) { value = null; }
            OnlinePrayer.Result completed = value;
            tick.post(() -> complete(started, completed));
        }, "prayer-check").start();
    }

    private void cancelRequest() {
        OnlinePrayer previous = request; request = null;
        if (previous != null) { previous.cancel(); failed = true; }
    }

    private void complete(OnlinePrayer completed, OnlinePrayer.Result value) {
        if (request != completed || !resumed || isFinishing() || isDestroyed()) return;
        request = null; result = value; failed = value == null; renderReport();
    }

    private void renderReport() {
        if (table == null) return;
        readClockFormat();
        table.removeAllViews();
        status.setVisibility(result == null ? View.VISIBLE : View.GONE);
        status.setText(failed ? t.onlineUnavailable() : t.onlineLoading());
        metadata.setVisibility(result == null ? View.GONE : View.VISIBLE);
        retry.setVisibility(request == null && (failed || result != null) ? View.VISIBLE : View.GONE);
        if (result == null) return;
        OnlinePrayer source = result.source;
        metadata.setText(t.onlineContext(t.ar ? source.city.ar : source.city.en, source.method, source.school, source.day(), result.timezone));
        reportRow(true, new String[] {"", t.onlineCalculated(), t.onlineReference(), t.onlineDifference()});
        for (int i = 0; i < 6; i++) {
            int delta = result.differences[i];
            reportRow(false, new String[] {t.prayerNames()[i], time(new Date((long) result.calculated[i]), 1f).toString(),
                time(new Date((long) result.reference[i]), 1f).toString(), t.d((delta > 0 ? "+" : "") + delta)});
        }
    }

    private void reportRow(boolean heading, String[] values) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        if (!heading) row.setBackgroundColor(Palette.LAPIS_800);
        int[] widths = {300, 400, 400, 628};
        for (int i = 0; i < values.length; i++) {
            TextView value = text(heading ? ui400 : ui500, heading ? 24 : 32, heading ? Palette.MIST_400 : Palette.PLASTER_100);
            value.setGravity(Gravity.CENTER); value.setText(values[i]);
            if (!heading && i > 0) value.setTextDirection(View.TEXT_DIRECTION_LTR);
            row.addView(value, new LinearLayout.LayoutParams(Ui.ipx(widths[i]), -1));
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Ui.ipx(66));
        lp.bottomMargin = Ui.ipx(4); table.addView(row, lp);
    }

    @Override void onTick() {}

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();
        if (key == KeyEvent.KEYCODE_BACK) { if (event.getAction() == KeyEvent.ACTION_UP) finish(); return true; }
        if (key == KeyEvent.KEYCODE_DPAD_UP || key == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (key == KeyEvent.KEYCODE_DPAD_DOWN && retry.getVisibility() == View.VISIBLE) retry.requestFocus();
                else back.requestFocus();
            }
            return true;
        }
        if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT) return true;
        return super.dispatchKeyEvent(event);
    }
}
