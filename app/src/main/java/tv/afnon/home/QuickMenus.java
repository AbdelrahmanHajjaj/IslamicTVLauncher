package tv.afnon.home;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** The same themed quick controls work on Home and in the prayer overlay. */
final class QuickMenus {
    static LinearLayout panel(Context c, String title) {
        Ui.init(c);
        LinearLayout panel = new LinearLayout(c);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setLayoutDirection(Screen.loadText(c).ar ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        panel.setPadding(Ui.ipx(28), Ui.ipx(24), Ui.ipx(28), Ui.ipx(24));
        panel.setClipChildren(false); panel.setClipToPadding(false);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Palette.LAPIS_900);
        bg.setCornerRadius(Ui.px(24)); bg.setStroke(Ui.ipx(2), Palette.GOLD_400_60);
        panel.setBackground(bg);
        if (title != null) {
            TextView heading = text(c, title, 28, Palette.GOLD_400);
            heading.setPadding(Ui.ipx(10), 0, Ui.ipx(10), Ui.ipx(18));
            panel.addView(heading);
        }
        return panel;
    }

    static TextView text(Context c, CharSequence value, int size, int color) {
        TextView text = new TextView(c);
        text.setTypeface(c.getResources().getFont(R.font.alexandria_regular));
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, Ui.px(size));
        text.setTextColor(color); text.setText(value);
        return text;
    }

    static FocusTile button(Context c, String label, Runnable action) {
        FocusTile b = new FocusTile(c, 22);
        b.setFocusableInTouchMode(true);
        b.setRest(Palette.LAPIS_800, Palette.LINE_SOFT, 1);
        TextView text = text(c, label, 24, Palette.PLASTER_100);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setPadding(Ui.ipx(22), 0, Ui.ipx(22), 0);
        text.setSingleLine(true); text.setEllipsize(android.text.TextUtils.TruncateAt.END);
        b.addView(text, new FrameLayout.LayoutParams(-1, -1));
        b.setLook(f -> text.setTextColor(f ? Palette.LAPIS_900 : Palette.PLASTER_100));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    static void row(LinearLayout panel, String label, Runnable action) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Ui.ipx(78));
        lp.topMargin = Ui.ipx(4);
        panel.addView(button(panel.getContext(), label, action), lp);
    }

    static View inputs(Context c, Runnable close) {
        Text t = Screen.loadText(c);
        LinearLayout panel = panel(c, t.inputsTitle());
        row(panel, t.home(), () -> { close.run(); InputSources.launch(c, new android.content.Intent(c, MainActivity.class)); });
        for (InputSources.Source source : InputSources.list(c))
            row(panel, source.label, () -> { close.run(); InputSources.open(c, source.intent); });
        row(panel, t.inputsSystem(), () -> { close.run(); InputSources.open(c, null); });
        ScrollView scroll = new ScrollView(c);
        scroll.setFillViewport(false); scroll.setClipToPadding(false);
        scroll.addView(panel);
        return scroll;
    }

    static void focusFirst(View content) {
        content.post(() -> {
            for (View v : content.getFocusables(View.FOCUS_FORWARD))
                if (v instanceof FocusTile && v.requestFocus()) break;
        });
    }

    static PopupWindow show(Activity a, View anchor) {
        PopupWindow popup = new PopupWindow();
        View content = inputs(a, popup::dismiss);
        content.measure(View.MeasureSpec.makeMeasureSpec(Ui.ipx(640), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        popup.setContentView(content); popup.setWidth(Ui.ipx(640));
        popup.setHeight(Math.min(content.getMeasuredHeight(), Ui.ipx(760)));
        popup.setFocusable(true); popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOnDismissListener(anchor::requestFocus);
        boolean ar = Screen.loadText(a).ar;
        popup.showAtLocation(anchor, Gravity.TOP | (ar ? Gravity.LEFT : Gravity.RIGHT), Ui.ipx(96), Ui.ipx(152));
        focusFirst(content);
        return popup;
    }
}
