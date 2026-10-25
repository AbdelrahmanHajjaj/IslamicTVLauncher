package tv.afnon.home;

import android.accessibilityservice.AccessibilityService;
import android.content.*;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.PowerManager;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import android.widget.*;
import java.util.Date;

/** One small, non-focusable tab; only an explicitly opened panel receives remote navigation. */
final class PrayerDock {
    private final AccessibilityService service;
    private final WindowManager wm;
    private View window;
    private String foreground = "", screen = "", page = "";
    private boolean returnToDock;
    private TextView next;
    private final TextView[] times = new TextView[6], names = new TextView[6], tags = new TextView[6];
    private final View[] cells = new View[6];
    private long shownMinute = -1;
    private String shownPrefs = "";
    private final SharedPreferences.OnSharedPreferenceChangeListener changed = (p, key) -> {
        if ("dock_mode".equals(key) || "dock_position".equals(key)) collapse();
        else if ("quick_labels".equals(key) && "dock".equals(page)) open("dock", true);
    };
    private final BroadcastReceiver power = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) { if (Intent.ACTION_SCREEN_OFF.equals(i.getAction())) hide(); }
    };

    PrayerDock(AccessibilityService service) {
        this.service = service; wm = service.getSystemService(WindowManager.class);
        Prefs.of(service).registerOnSharedPreferenceChangeListener(changed);
        service.registerReceiver(power, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    void stop() {
        hide(); Prefs.of(service).unregisterOnSharedPreferenceChangeListener(changed);
        service.unregisterReceiver(power);
    }

    void onWindow(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString(), cls = String.valueOf(event.getClassName());
        // Overlay widgets and transient volume dialogs do not replace the app underneath.
        if (cls.startsWith("android.widget.") || cls.startsWith("android.view.") || cls.endsWith("Dialog")) return;
        if (!pkg.equals(foreground) || !cls.equals(screen)) hide();
        foreground = pkg; screen = cls;
        update(new Date(), asleep());
    }

    private boolean asleep() {
        PowerManager pm = service.getSystemService(PowerManager.class);
        return pm != null && !pm.isInteractive();
    }

    private String mode() { return Prefs.of(service).getString("dock_mode", "hidden"); }

    void toggle() {
        if (!page.isEmpty()) { collapse(); return; }
        if ("off".equals(mode())) {
            Toast.makeText(service, Screen.loadText(service).dockDisabled(), Toast.LENGTH_SHORT).show(); return;
        }
        open("dock", true);
    }

    void inputs() { open("inputs", false); }

    void hide() { remove(); page = ""; }

    private void remove() {
        if (window != null) try { wm.removeView(window); } catch (RuntimeException ignored) { }
        window = null; next = null; java.util.Arrays.fill(times, null); java.util.Arrays.fill(names, null);
        java.util.Arrays.fill(tags, null); java.util.Arrays.fill(cells, null);
        shownMinute = -1;
    }

    private void collapse() { hide(); update(new Date(), asleep()); }

    private void back() {
        if (!"dock".equals(page) && returnToDock) open("dock", true); else collapse();
    }

    private void open(String which, boolean fromDock) {
        remove(); page = which; returnToDock = fromDock;
        Ui.init(service);
        FrameLayout root = new FrameLayout(service) {
            @Override public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.KEYCODE_BACK || e.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
                    if (e.getAction() == KeyEvent.ACTION_UP && !e.isCanceled()) back();
                    return true;
                }
                return super.dispatchKeyEvent(e);
            }
        };
        View content = "inputs".equals(which) ? QuickMenus.inputs(service, this::hide)
            : content();
        root.setClipChildren(false); root.setClipToPadding(false);
        root.addView(content);
        int width = Ui.ipx("dock".equals(which) ? 1920 : 640);
        root.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        attach(root, width, Math.min(root.getMeasuredHeight(), Ui.ipx(760)), true, fromDock);
        QuickMenus.focusFirst(content);
        update(new Date(), asleep());
    }

    private View content() {
        Text t = Screen.loadText(service);
        LinearLayout panel = QuickMenus.panel(service, null);
        panel.setPadding(Ui.ipx(24), Ui.ipx(10), Ui.ipx(24), Ui.ipx(10));
        panel.setOrientation(LinearLayout.HORIZONTAL); panel.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout prayers = new LinearLayout(service);

        for (int i = 0; i < 6; i++) {
            LinearLayout prayer = new LinearLayout(service); prayer.setOrientation(LinearLayout.VERTICAL);
            prayer.setPadding(Ui.ipx(12), Ui.ipx(6), Ui.ipx(12), Ui.ipx(6));
            cells[i] = prayer;
            tags[i] = QuickMenus.text(service, "", 14, Palette.PLASTER_100); prayer.addView(tags[i]);
            names[i] = QuickMenus.text(service, "", 18, Palette.PLASTER_100); prayer.addView(names[i]);
            times[i] = QuickMenus.text(service, "", 24, Palette.PLASTER_100); prayer.addView(times[i]);
            for (TextView text : new TextView[] {tags[i], names[i], times[i]}) text.setIncludeFontPadding(false);
            names[i].setSingleLine(true); names[i].setEllipsize(android.text.TextUtils.TruncateAt.END);
            times[i].setSingleLine(true);
            LinearLayout.LayoutParams cell = new LinearLayout.LayoutParams(0, -2, 1); cell.setMarginEnd(Ui.ipx(8));
            prayers.addView(prayer, cell);
        }
        panel.addView(prayers, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout buttons = new LinearLayout(service); buttons.setClipChildren(false); buttons.setClipToPadding(false);
        buttons.setGravity(Gravity.END);
        buttons.setPaddingRelative(Ui.ipx(12), 0, 0, 0);
        String[] labels = {t.home(), t.inputsTitle(), "Wi-Fi", t.settings()};
        int[] icons = {Icon.HOME, Icon.INPUT, Icon.WIFI, Icon.GEAR};
        Runnable[] actions = {
            () -> { hide(); InputSources.launch(service, new Intent(service, MainActivity.class)); },
            () -> open("inputs", true), () -> { hide(); SystemScreens.wifi(service); },
            () -> { hide(); InputSources.launch(service, new Intent(service, SettingsActivity.class)); }
        };
        for (int i = 0; i < labels.length; i++) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, Ui.ipx(60)); lp.setMarginEnd(Ui.ipx(8));
            buttons.addView(QuickMenus.iconButton(service, labels[i], icons[i], Prefs.quickLabels(service), actions[i]), lp);
        }
        FocusTile minimize = new FocusTile(service, 22);
        minimize.setRest(Palette.LAPIS_800, Palette.LINE_SOFT, 1);
        minimize.setContentDescription(t.collapseDock());
        Icon minus = new Icon(service, Icon.MINUS, 2f);
        minimize.addView(minus, new FrameLayout.LayoutParams(Ui.ipx(30), Ui.ipx(30), Gravity.CENTER));
        minimize.setLook(f -> minus.setColor(f ? Palette.LAPIS_900 : Palette.PLASTER_100));
        minimize.setOnClickListener(v -> collapse());
        buttons.addView(minimize, new LinearLayout.LayoutParams(Ui.ipx(60), Ui.ipx(60)));
        panel.addView(buttons, new LinearLayout.LayoutParams(-2, -2));
        return panel;
    }

    void update(Date now, boolean asleep) {
        if (asleep) { hide(); return; }
        if (page.isEmpty()) {
            if (!DockPolicy.tab(mode(), foreground, screen, false)) { remove(); return; }
            if (window == null) {
                Ui.init(service);
                LinearLayout tab = QuickMenus.panel(service, null);
                tab.setPadding(Ui.ipx(24), Ui.ipx(6), Ui.ipx(24), Ui.ipx(6));
                next = QuickMenus.text(service, "", 18, Palette.PLASTER_100); next.setGravity(Gravity.CENTER); tab.addView(next);
                attach(tab, Ui.ipx(1920), WindowManager.LayoutParams.WRAP_CONTENT, false, true);
            }
        }
        if (next == null && times[0] == null) return;
        String prefs = Prefs.key(service);
        if (shownMinute == now.getTime()/60000 && shownPrefs.equals(prefs)) return;
        shownMinute = now.getTime()/60000; shownPrefs = prefs;
        PrayerDay day = new PrayerDay(service, now);
        Text t = Screen.loadText(service);
        String[] names = t.prayerNames(); if (day.friday) names[PrayerDay.DHUHR] = t.jumuah();
        if (next != null) PrayerIcon.set(next, day.nextIndex, names[day.nextIndex] + t.sep() + Screen.clock(t, day.nextTime, Prefs.clock24(service), 1f));
        for (int i = 0; i < times.length; i++) if (times[i] != null) {
            PrayerIcon.set(this.names[i], i, names[i]);
            times[i].setText(Screen.clock(t, day.times[i], Prefs.clock24(service), 1f));
            boolean current = i == day.nowIndex, upcoming = i == day.nextIndex;
            boolean past = !upcoming && !day.times[i].after(now);
            int color = current ? Palette.LAPIS_900 : upcoming ? Palette.GOLD_400 : past ? Palette.MIST_400 : Palette.PLASTER_100;
            GradientDrawable cell = new GradientDrawable(); cell.setCornerRadius(Ui.px(14));
            cell.setColor(current ? Palette.PLASTER_100 : past ? Palette.LAPIS_800_60 : Palette.LAPIS_800);
            cell.setStroke(Ui.ipx(upcoming ? 2 : 1), upcoming ? Palette.GOLD_400 : Palette.LINE_SOFT);
            cells[i].setBackground(cell);
            tags[i].setText(current ? t.nowTag() : upcoming ? t.nextTag() : "");
            tags[i].setTextColor(color); this.names[i].setTextColor(color); times[i].setTextColor(color);
        }
    }

    private void attach(View view, int width, int height, boolean interactive, boolean dockPosition) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(width, height,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | (interactive ? 0 : WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE),
            PixelFormat.TRANSLUCENT);
        boolean bottom = dockPosition && !"top".equals(Prefs.of(service).getString("dock_position", "bottom"));
        lp.gravity = (bottom ? Gravity.BOTTOM : Gravity.TOP) | (Screen.loadText(service).ar ? Gravity.LEFT : Gravity.RIGHT);
        lp.x = Ui.ipx(dockPosition ? 0 : 96); lp.y = Ui.ipx(dockPosition ? 0 : 152);
        lp.setTitle("Afnon prayer bar");
        try { wm.addView(view, lp); window = view; }
        catch (RuntimeException unavailable) {
            android.util.Log.w("AfnonDock", "Prayer bar window unavailable", unavailable);
            page = ""; next = null;
        }
    }
}
