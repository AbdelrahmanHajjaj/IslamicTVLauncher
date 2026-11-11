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
        panel.setPadding(Ui.ipx(Tokens.LAYOUT_QUICK_MENU_PADDING_INLINE), Ui.ipx(Tokens.SPACE_4), Ui.ipx(Tokens.LAYOUT_QUICK_MENU_PADDING_INLINE), Ui.ipx(Tokens.SPACE_4));
        panel.setClipChildren(false); panel.setClipToPadding(false);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Palette.LAPIS_900);
        bg.setCornerRadius(Ui.px(Tokens.RADIUS_PANEL)); bg.setStroke(Ui.ipx(Tokens.BORDER_CHIP_WIDTH), Palette.GOLD_400_60);
        panel.setBackground(bg);
        if (title != null) {
            TextView heading = text(c, title, Tokens.FONT_SIZE_QUICK_HEADING, Palette.GOLD_400);
            heading.setPadding(Ui.ipx(Tokens.LAYOUT_QUICK_MENU_HEADING_INSET), 0, Ui.ipx(Tokens.LAYOUT_QUICK_MENU_HEADING_INSET), Ui.ipx(Tokens.LAYOUT_QUICK_MENU_HEADING_GAP));
            panel.addView(heading);
        }
        return panel;
    }

    static TextView text(Context c, CharSequence value, float size, int color) {
        TextView text = new TextView(c);
        text.setTypeface(c.getResources().getFont(R.font.alexandria_regular));
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, Ui.px(size));
        text.setTextColor(color); text.setText(value);
        return text;
    }

    static FocusTile button(Context c, String label, Runnable action) {
        FocusTile b = new FocusTile(c, Tokens.RADIUS_QUICK_BUTTON);
        b.setFocusableInTouchMode(true);
        b.setRest(Palette.LAPIS_800, Palette.LINE_SOFT, Tokens.BORDER_THIN);
        TextView text = text(c, label, Tokens.FONT_SIZE_CONTROL, Palette.PLASTER_100);
        text.setGravity(Gravity.CENTER_VERTICAL); text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        text.setPadding(Ui.ipx(Tokens.LAYOUT_QUICK_MENU_BUTTON_PADDING), 0, Ui.ipx(Tokens.LAYOUT_QUICK_MENU_BUTTON_PADDING), 0);
        text.setSingleLine(true); text.setEllipsize(android.text.TextUtils.TruncateAt.END);
        b.addView(text, new FrameLayout.LayoutParams(-1, -1));
        b.setLook(f -> text.setTextColor(f ? Palette.LAPIS_900 : Palette.PLASTER_100));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    static FocusTile iconButton(Context c, String label, int kind, boolean showLabel, Runnable action) {
        FocusTile b = new FocusTile(c, Tokens.RADIUS_ICON_BUTTON);
        b.setFocusableInTouchMode(true);
        b.setRest(Palette.LAPIS_800, Palette.LINE_SOFT, Tokens.BORDER_CHIP_WIDTH);
        b.setContentDescription(label);
        LinearLayout row = new LinearLayout(c); row.setGravity(Gravity.CENTER);
        row.setPadding(Ui.ipx(Tokens.SPACE_4), 0, Ui.ipx(Tokens.SPACE_4), 0);
        Icon icon = new Icon(c, kind, Tokens.BORDER_CHIP_WIDTH);
        row.addView(icon, new LinearLayout.LayoutParams(Ui.ipx(Tokens.LAYOUT_QUICK_MENU_ICON_SIZE), Ui.ipx(Tokens.LAYOUT_QUICK_MENU_ICON_SIZE)));
        TextView text = text(c, label, Tokens.FONT_SIZE_CONTROL, Palette.PLASTER_100);
        text.setSingleLine(true); text.setVisibility(showLabel ? View.VISIBLE : View.GONE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2); lp.setMarginStart(Ui.ipx(Tokens.SPACE_2));
        row.addView(text, lp); b.addView(row, new FrameLayout.LayoutParams(-2, -1));
        b.setLook(f -> { int color = f ? Palette.LAPIS_900 : Palette.PLASTER_100; icon.setColor(color); text.setTextColor(color); });
        b.setOnClickListener(v -> action.run()); return b;
    }

    static void row(LinearLayout panel, String label, Runnable action) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Ui.ipx(Tokens.LAYOUT_QUICK_MENU_ROW_HEIGHT));
        lp.topMargin = Ui.ipx(Tokens.LAYOUT_QUICK_MENU_ROW_GAP);
        panel.addView(button(panel.getContext(), label, action), lp);
    }

    static View inputs(Context c, Runnable close) {
        Text t = Screen.loadText(c);
        LinearLayout panel = panel(c, null);
        LinearLayout header = new LinearLayout(c); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(c, t.inputsTitle(), Tokens.FONT_SIZE_QUICK_HEADING, Palette.GOLD_400);
        title.setPaddingRelative(Ui.ipx(Tokens.LAYOUT_QUICK_MENU_HEADING_INSET), 0, 0, 0);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        FocusTile advanced = iconButton(c, t.advancedInputs(), Icon.MORE, false, () -> { close.run(); InputSources.open(c, null); });
        header.addView(advanced, new LinearLayout.LayoutParams(-2, Ui.ipx(Tokens.LAYOUT_BUTTON_HEIGHT)));
        panel.addView(header);
        row(panel, t.home(), () -> { close.run(); InputSources.launch(c, new android.content.Intent(c, MainActivity.class)); });
        panel.getChildAt(1).setFocusedByDefault(true);
        for (InputSources.Source source : InputSources.list(c))
            row(panel, source.label, () -> { close.run(); InputSources.open(c, source.intent); });
        ScrollView scroll = new ScrollView(c);
        scroll.setFillViewport(false); scroll.setClipToPadding(false);
        scroll.addView(panel);
        return scroll;
    }

    static void focusFirst(View content) {
        content.post(() -> {
            for (View v : content.getFocusables(View.FOCUS_FORWARD))
                if (v.isFocusedByDefault() && v.requestFocus()) return;
            for (View v : content.getFocusables(View.FOCUS_FORWARD))
                if (v instanceof FocusTile && v.requestFocus()) break;
        });
    }

    static PopupWindow show(Activity a, View anchor) {
        PopupWindow popup = new PopupWindow();
        View content = inputs(a, popup::dismiss);
        content.measure(View.MeasureSpec.makeMeasureSpec(Ui.ipx(Tokens.LAYOUT_QUICK_MENU_WIDTH), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        popup.setContentView(content); popup.setWidth(Ui.ipx(Tokens.LAYOUT_QUICK_MENU_WIDTH));
        popup.setHeight(Math.min(content.getMeasuredHeight(), Ui.ipx(Tokens.LAYOUT_QUICK_MENU_MAX_HEIGHT)));
        popup.setFocusable(true); popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOnDismissListener(anchor::requestFocus);
        boolean ar = Screen.loadText(a).ar;
        popup.showAtLocation(anchor, Gravity.TOP | Gravity.getAbsoluteGravity(Gravity.END, ar ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR), Ui.ipx(Tokens.SPACE_SAFE_INLINE), Ui.ipx(Tokens.LAYOUT_QUICK_MENU_TOP));
        focusFirst(content);
        return popup;
    }
}
