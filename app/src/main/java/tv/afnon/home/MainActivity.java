package tv.afnon.home;

import static tv.afnon.home.FavouriteChoice.*;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.icu.util.IslamicCalendar;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Madhab;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Afnon Home: independently arranged countdown, prayer information and app rows. */
public class MainActivity extends Screen {

    private final View[] nodes = new View[NODES];
    private final TextView[] cellTag = new TextView[6], cellName = new TextView[6], cellTime = new TextView[6];
    private final View[] cell = new View[6];
    private final String[] nodePackage = new String[NODES];
    private FocusGlow focusGlow;
    private String replacementPackage;

    private TextView clock, gdate, hdate, wifiName;
    private int favouriteCount, recentCount;
    private boolean classicHome;
    private HomeLayout home;
    private PrayerDay day;
    private long lastMinute = -1;
    private int lastFav = F0;
    /** What Home was built with, so it rebuilds when All apps changes favourites or apps come and go. */
    private String builtApps = "";
    /** The reminder card (design/Home.dc.html, mode "approach") and the prayer it is for. */
    private LinearLayout card;
    private FocusTile cardOk, cardDay;
    private Date cardFor;
    /** The radio's header pill and, while open, its card. */
    private Icon radioIcon, playIcon;
    private RadioBars pillBars, cardBars;
    private TextView radioLabel, cardStatus, playLabel;
    private LinearLayout radioCard;
    private android.widget.PopupWindow quickMenu;
    private FocusTile cardPlay, cardDark;
    private final Radio.Listener onRadio = state -> styleRadio();
    /** Test only (shiftTo): how far Home's clock is moved. */
    private long shift;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (readReplacement(getIntent())) build();
        runTest(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean changed = loadSettings(intent);
        boolean replacementChanged = readReplacement(intent);
        if (changed || replacementChanged) build();
        // Home starts at the first app. Posted, because the
        // window puts back its previously focused view when it regains focus after onNewIntent.
        root.post(() -> firstApp().requestFocus());
        runTest(intent);
    }

    /**
     * Checking reminders on the TV without waiting for a prayer: `--es test reminder` shows Home's card now, with
     * the spoken reminder if it is on; `banner`, `call` and `announce` come from the remote buttons service 8 s
     * later, time enough to open another app.
     * `--es station <id>` (ids in Radio.STATIONS) picks a radio station as Settings does, before any test runs.
     */
    private void runTest(Intent i) {
        String station = i == null ? null : i.getStringExtra("station");
        if (station != null) {
            i.removeExtra("station");
            for (int k = 0; k < Radio.STATIONS.length; k++) if (Radio.STATIONS[k].id.equals(station)) Radio.switchTo(this, k);
        }
        String kind = i == null ? null : i.getStringExtra("test");
        if (kind == null) return;
        i.removeExtra("test");
        if ("reminder".equals(kind)) root.post(() -> {
            showReminder();
            if (Prefs.remindVoice(this)) Announce.before(this, day.nextIndex, day.friday);
        });
        else if ("friday".equals(kind) || "ramadan".equals(kind) || "now".equals(kind)) shiftTo(kind);
        else if ("radio".equals(kind)) Radio.toggle(this);
        else if (RemoteKeys.watch != null) RemoteKeys.watch.test(kind, 8000);
    }

    /**
     * Test only: Home's clock jumps to the coming Friday at 10:30 (Jumu'ah next) or to 15 Ramadan at 16:30
     * (iftar next) and keeps ticking from there; "now" puts it back. Nothing else in Afnon is shifted.
     */
    private void shiftTo(String kind) {
        Calendar c = Calendar.getInstance();
        if ("friday".equals(kind)) {
            while (c.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) c.add(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 10);
            c.set(Calendar.MINUTE, 30);
        } else if ("ramadan".equals(kind)) {
            IslamicCalendar h = new IslamicCalendar();
            h.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC_UMALQURA);
            if (h.get(IslamicCalendar.MONTH) > IslamicCalendar.RAMADAN) h.add(IslamicCalendar.YEAR, 1);
            h.set(IslamicCalendar.MONTH, IslamicCalendar.RAMADAN);
            h.set(IslamicCalendar.DAY_OF_MONTH, 15);
            c.setTime(h.getTime());
            c.set(Calendar.HOUR_OF_DAY, 16);
            c.set(Calendar.MINUTE, 30);
        }
        shift = "now".equals(kind) ? 0 : c.getTimeInMillis() - System.currentTimeMillis();
        lastMinute = -1;
        day = null;
    }

    private Date now() {
        return new Date(System.currentTimeMillis() + shift);
    }

    @Override
    protected void onResume() {
        Apps.invalidate();
        ServiceRepair.restore(this);
        if (!builtApps.equals(appsKey())) build();
        lastMinute = -1;
        updateWifi();
        super.onResume();
        Reminders.home = this::showReminder;
        Radio.listen(onRadio);
        styleRadio();
    }

    @Override
    protected void onPause() {
        if (quickMenu != null) { quickMenu.dismiss(); quickMenu = null; }
        Reminders.home = null;
        Radio.unlisten(onRadio);
        if (focusGlow != null) focusGlow.stop();
        super.onPause();
    }

    @Override
    void onTick() {
        render(false);
        // The prayer the card was about has arrived (the call takes over) or the card outlived its prayer.
        if (card != null && day != null && !day.nextTime.equals(cardFor)) dismissReminder();
    }

    // ---------------------------------------------------------------- layout

    @Override
    void build() {
        int previousNode = currentNode();
        String previousPackage = previousNode < 0 ? null : nodePackage[previousNode];
        newRoot();
        java.util.Arrays.fill(nodePackage, null);
        java.util.Arrays.fill(nodes, null);
        focusGlow = Prefs.of(this).getBoolean("focus_lighting", false) ? new FocusGlow(this) : null;
        if (focusGlow != null) root.addView(focusGlow, new FrameLayout.LayoutParams(-1, -1));
        card = null;   // a reminder card went with the old root
        radioCard = null;
        List<String> favs = Apps.slots(this, t);
        classicHome = Prefs.classicHome(this);
        home = new HomeLayout(Prefs.homeCountdown(this), Prefs.homeSide(this), Prefs.homeBar(this),
            Prefs.homePrayers(this), favs.size(), classicHome, rtl);
        favouriteCount = home.count;
        buildCountdown();
        nodes[QIBLA] = quickPill(t.today(), Icon.COMPASS, () -> startActivity(new Intent(this, PrayerDayActivity.class)));

        // The header stays available in every Home layout.
        LinearLayout when = new LinearLayout(this);
        when.setOrientation(LinearLayout.VERTICAL);
        clock = text(display, 96, Palette.PLASTER_100);
        clock.setIncludeFontPadding(false);
        clock.setTextDirection(rtl ? View.TEXT_DIRECTION_RTL : View.TEXT_DIRECTION_LTR);
        clock.setFontFeatureSettings("tnum");
        // Under the clock the Hijri date (with the city) first, then the Gregorian, smaller and in mist.
        hdate = text(ui400, 26, Palette.PLASTER_100);
        gdate = text(ui400, 24, Palette.MIST_400);
        when.addView(clock);
        when.addView(hdate);
        when.addView(gdate);
        ((LinearLayout.LayoutParams) hdate.getLayoutParams()).topMargin = Ui.ipx(6);
        ((LinearLayout.LayoutParams) gdate.getLayoutParams()).topMargin = Ui.ipx(6);
        lineHeight(clock, 1f);
        when.setClipChildren(false);
        place(when, Tokens.SPACE_SAFE_INLINE, 56, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Quick controls end beside Settings; the clock keeps its own space.
        nodes[INPUTS] = inputPill();
        nodes[RADIO] = radioPill();
        nodes[WIFI] = wifiPill();
        LinearLayout pills = new LinearLayout(this);
        pills.setClipChildren(false);
        float reach = FocusTile.REACH;
        pills.addView(nodes[INPUTS], new LinearLayout.LayoutParams(-2, Ui.ipx(72 + 2 * reach)));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-2, Ui.ipx(72 + 2 * reach));
        rlp.setMarginStart(Ui.ipx(16 - 2 * reach));
        pills.addView(nodes[RADIO], rlp);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(-2, Ui.ipx(72 + 2 * reach));
        wlp.setMarginStart(Ui.ipx(20 - 2 * reach));
        pills.addView(nodes[QIBLA], wlp);
        LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(-2, Ui.ipx(72 + 2 * reach));
        qlp.setMarginStart(Ui.ipx(20 - 2 * reach));
        pills.addView(nodes[WIFI], qlp);
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.END);
        plp.setMarginEnd(Ui.ipx(1920 - (Tokens.COURT_X + Tokens.COURT_W) + 72 + 20 - reach));
        plp.topMargin = Ui.ipx(56 - reach);
        root.addView(pills, plp);
        nodes[SETTINGS] = settingsButton();

        // The prayer strip: information only, never focusable.
        java.util.Arrays.fill(cell, null);
        int slots = HomeLayout.prayers(home.table, -1, 0).length;
        float cw = Math.min(Tokens.LAYOUT_HOME_TILE_MAX_WIDTH, (home.width - (slots - 1) * 12) / Math.max(1, slots));
        for (int i = 0; i < slots; i++) {
            LinearLayout v = new LinearLayout(this);
            v.setOrientation(LinearLayout.VERTICAL);
            v.setPadding(Ui.ipx(20), Ui.ipx(Tokens.LAYOUT_HOME_CELL_PADDING), Ui.ipx(20), Ui.ipx(Tokens.LAYOUT_HOME_CELL_PADDING));
            cellTag[i] = text(ui400, 20, Palette.PLASTER_100);
            cellName[i] = text(ui400, 26, Palette.PLASTER_100);
            cellTime[i] = text(ui400, Tokens.FONT_SIZE_BODY_L, Palette.PLASTER_100);
            cellTag[i].setIncludeFontPadding(false);
            cellName[i].setSingleLine(true);
            cellName[i].setEllipsize(TextUtils.TruncateAt.END);
            cellTime[i].setTextDirection(rtl ? View.TEXT_DIRECTION_RTL : View.TEXT_DIRECTION_LTR);
            cellTime[i].setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            cellTime[i].setFontFeatureSettings("tnum");
            cellTime[i].setSingleLine(true);
            v.addView(cellTag[i], new LinearLayout.LayoutParams(-1, 0, 1));
            v.addView(cellName[i], new LinearLayout.LayoutParams(-1, -2));
            v.addView(cellTime[i], new LinearLayout.LayoutParams(-1, -2));
            lineHeight(cellName[i], 1.1f);
            lineHeight(cellTime[i], 1.1f);
            v.setClipChildren(false);
            cell[i] = v;
            place(v, home.x + i * (cw + 12), home.tableY, cw, Tokens.LAYOUT_HOME_TABLE_HEIGHT);
        }

        TextView fav = caps(text(ui400, 22, Palette.MIST_400));
        fav.setText(replacementPackage == null ? t.fav() : t.replaceWith(Apps.name(this, t, replacementPackage)));
        fav.setSingleLine(true);
        fav.setMaxWidth(Ui.ipx(home.width));
        fav.setEllipsize(TextUtils.TruncateAt.END);
        if (favouriteCount > 0) place(fav, home.x, home.labelY, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int columns = home.columns;
        float fw = home.tileWidth;
        for (int i = 0; i < favouriteCount; i++) {
            nodes[F0 + i] = favouriteTile(favs.get(i), F0 + i,
                home.x + (i % columns) * (fw + Tokens.LAYOUT_HOME_GAP), home.favouritesY + (i / columns) * (home.tileHeight + Tokens.LAYOUT_HOME_GAP), fw);
        }

        TextView recent = caps(text(ui400, 22, Palette.MIST_400));
        recent.setText(t.recentApps());
        place(recent, home.x, home.recentLabelY, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        float mw = (home.width - 5 * 20) / 6f;
        List<String> recentApps = Apps.recent(this);
        recentCount = recentApps.size();
        for (int i = 0; i < recentCount; i++) {
            nodes[M0 + i] = moreTile(recentApps.get(i), M0 + i, home.x + i * (mw + 20), home.recentY, mw);
        }
        nodes[ALL] = allAppsTile(home.x + 5 * (mw + 20), home.recentY, mw);

        for (int i = 0; i < NODES; i++) {
            final int n = i;
            if (nodes[i] == null) continue;
            nodes[i].setOnFocusChangeListener((v, has) -> { if (has) onNodeFocused(n); });
        }

        if (replacementPackage != null) {
            for (int i = 0; i < NODES; i++) {
                if (nodes[i] == null) continue;
                boolean candidate = i >= F0 && i < M0;
                nodes[i].setFocusable(candidate);
                nodes[i].setEnabled(candidate);
                if (!candidate) nodes[i].setAlpha(.4f);
            }
        }
        setContentView(root);
        builtApps = appsKey();
        lastMinute = -1;
        render(true);
        View restore = previousNode >= 0 ? nodes[previousNode] : null;
        if (previousPackage != null) {
            restore = null;
            for (int i = 0; i < NODES; i++) if (previousPackage.equals(nodePackage[i])) { restore = nodes[i]; break; }
        }
        if (restore == null || !restore.isFocusable()) restore = firstApp();
        restore.requestFocus();
    }

    private void buildCountdown() {
        arch = null;
        archLabel = archName = archCount = archSub = archSub2 = null;
        if (home.mode == HomeLayout.HIDDEN) return;
        boolean compact = home.mode == HomeLayout.COMPACT;
        float inset = Tokens.LAYOUT_HOME_PANEL_INSET;
        if (!compact) {
            arch = new ArchView(this);
            place(arch, home.panelX, home.panelY, Tokens.LAYOUT_HOME_MIHRAB_WIDTH, Tokens.LAYOUT_HOME_MIHRAB_HEIGHT);
            TextView verse = text(sacred, Tokens.FONT_SIZE_SACRED, Palette.GOLD_400);
            verse.setText("إِنَّ الصَّلَاةَ كَانَتْ\nعَلَى الْمُؤْمِنِينَ\nكِتَابًا مَّوْقُوتًا");
            verse.setTextDirection(View.TEXT_DIRECTION_RTL);
            verse.setGravity(Gravity.CENTER);
            lineHeightPixels(verse, Ui.ipx(Tokens.FONT_SIZE_SACRED * 1.55f));
            place(verse, home.panelX + inset, home.panelY + Tokens.LAYOUT_HOME_VERSE_TOP,
                Tokens.LAYOUT_HOME_MIHRAB_WIDTH - 2 * inset, -2);
            TextView ref = text(ui400, Tokens.FONT_SIZE_CAPTION, Palette.MIST_400);
            ref.setText(t.ref()); ref.setGravity(Gravity.CENTER);
            place(ref, home.panelX + inset, home.panelY + Tokens.LAYOUT_HOME_REFERENCE_TOP,
                Tokens.LAYOUT_HOME_MIHRAB_WIDTH - 2 * inset, -2);
        }
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(compact ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        info.setGravity(Gravity.CENTER);
        if (compact) {
            info.setBackground(round(Palette.LAPIS_800, Tokens.RADIUS_CELL));
            info.setPadding(Ui.ipx(inset), 0, Ui.ipx(inset), 0);
        }
        archLabel = text(ui400, Tokens.FONT_SIZE_CAPTION, Palette.MIST_400);
        archName = text(displayBold, compact ? Tokens.FONT_SIZE_HEADING : Tokens.LAYOUT_HOME_PRAYER_NAME_SIZE, Palette.GOLD_400);
        archCount = text(display, compact ? Tokens.LAYOUT_HOME_COMPACT_COUNTDOWN_SIZE : Tokens.LAYOUT_HOME_COUNTDOWN_SIZE, Palette.PLASTER_100);
        archCount.setTextDirection(View.TEXT_DIRECTION_LTR);
        archCount.setFontFeatureSettings("tnum");
        archSub = text(ui400, Tokens.FONT_SIZE_CAPTION, Palette.MIST_400);
        archSub2 = text(ui400, Tokens.FONT_SIZE_CAPTION, Palette.GOLD_200);
        LinearLayout name = new LinearLayout(this), detail = new LinearLayout(this);
        name.setOrientation(LinearLayout.VERTICAL); detail.setOrientation(LinearLayout.VERTICAL);
        name.setGravity(Gravity.CENTER); detail.setGravity(Gravity.CENTER);
        name.addView(archLabel); name.addView(archName);
        detail.addView(archSub); detail.addView(archSub2);
        for (TextView v : new TextView[] {archLabel, archName, archCount, archSub, archSub2}) {
            v.setGravity(Gravity.CENTER); v.setIncludeFontPadding(false);
        }
        info.addView(name, new LinearLayout.LayoutParams(compact ? 0 : -1, -2, compact ? 1 : 0));
        info.addView(archCount, new LinearLayout.LayoutParams(compact ? 0 : -1, -2, compact ? 1 : 0));
        info.addView(detail, new LinearLayout.LayoutParams(compact ? 0 : -1, -2, compact ? 1 : 0));
        lineHeight(archName, 1.15f); lineHeight(archCount, 1f);
        info.setClipChildren(false);
        place(info, compact ? home.x : home.panelX + inset,
            compact ? home.panelY : home.panelY + Tokens.LAYOUT_HOME_INFO_TOP,
            compact ? home.width : Tokens.LAYOUT_HOME_MIHRAB_WIDTH - 2 * inset,
            compact ? Tokens.LAYOUT_HOME_BAR_HEIGHT : -2);
    }

    private View inputPill() {
        return quickPill(t.inputsTitle(), Icon.INPUT, () -> openQuick(true));
    }

    private View quickPill(String name, int kind, Runnable action) {
        return QuickMenus.iconButton(this, name, kind, Prefs.quickLabels(this), action);
    }

    private void openQuick(boolean inputs) {
        if (quickMenu != null) quickMenu.dismiss();
        closeRadioCard(false);
        if (card != null) dismissReminder();
        if (inputs) quickMenu = QuickMenus.show(this, nodes[INPUTS]);
        else SystemScreens.wifi(this);
    }

    private View wifiPill() {
        FocusTile b = new FocusTile(this, 36);
        b.setRest(Palette.LAPIS_800, Palette.LINE_SOFT, 2);
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.ipx(28), 0, Ui.ipx(28), 0);
        Icon icon = new Icon(this, Icon.WIFI, 2f);
        wifiName = text(ui400, 24, Palette.PLASTER_100);
        wifiName.setTextDirection(View.TEXT_DIRECTION_LTR);
        wifiName.setSingleLine(true); wifiName.setMaxWidth(Ui.ipx(180));
        wifiName.setEllipsize(TextUtils.TruncateAt.END);
        row.addView(icon, new LinearLayout.LayoutParams(Ui.ipx(30), Ui.ipx(30)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMarginStart(Ui.ipx(14));
        wifiName.setVisibility(Prefs.quickLabels(this) ? View.VISIBLE : View.GONE);
        b.setContentDescription("Wi-Fi");
        row.addView(wifiName, lp);
        b.addView(row, new FrameLayout.LayoutParams(-2, -1));
        b.setLook(f -> {
            int c = f ? Palette.LAPIS_900 : Palette.PLASTER_100;
            wifiName.setTextColor(c);
            icon.setColor(c);
        });
        b.setOnClickListener(v -> openQuick(false));
        return b;
    }

    private View settingsButton() {
        FocusTile b = new FocusTile(this, 36);
        b.setRest(Palette.LAPIS_800, Palette.LINE_SOFT, 2);
        Icon gear = new Icon(this, Icon.GEAR, 1.8f);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.ipx(34), Ui.ipx(34), Gravity.CENTER);
        b.addView(gear, lp);
        b.setContentDescription(t.settings());
        b.setLook(f -> gear.setColor(f ? Palette.LAPIS_900 : Palette.PLASTER_100));
        b.setOnClickListener(v -> startSafely(new Intent(this, SettingsActivity.class)));
        placeEnd(b, 1920 - (Tokens.COURT_X + Tokens.COURT_W), 56, 72, 72);
        return b;
    }

    private View favouriteTile(String pkg, int node, float x, float y, float w) {
        FocusTile b = new FocusTile(this, Tokens.RADIUS_TILE);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(Ui.ipx(20), Ui.ipx(18), Ui.ipx(20), Ui.ipx(18));
        ImageView icon = new ImageView(this);
        icon.setScaleType(ImageView.ScaleType.FIT_START);
        Drawable d = Apps.icon(this, pkg);
        if (d != null) icon.setImageDrawable(d);
        String name = pkg == null ? "" : Apps.name(this, t, pkg);
        TextView label = text(ui500, Apps.isLatin(name) ? (classicHome ? 28 : 24) : (classicHome ? 30 : 26), Palette.PLASTER_100);
        label.setText(name);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        int iconSize = Ui.ipx(classicHome ? 56 : 40);
        col.addView(icon, new LinearLayout.LayoutParams(iconSize, iconSize));
        col.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 1));
        col.addView(label, new LinearLayout.LayoutParams(-1, -2));
        b.addView(col, new FrameLayout.LayoutParams(-1, -1));
        b.setLook(f -> label.setTextColor(f ? Palette.LAPIS_900 : Palette.PLASTER_100));
        b.setOnClickListener(v -> activateApp(pkg));
        nodePackage[node] = pkg;
        placeTile(b, x, y, w, home.tileHeight);
        return b;
    }

    private View moreTile(String pkg, int node, float x, float y, float w) {
        FocusTile b = new FocusTile(this, Tokens.RADIUS_CELL);
        String name = pkg == null ? "" : Apps.name(this, t, pkg);
        TextView label = text(ui400, 22, Palette.PLASTER_100);
        label.setText(name);
        label.setMaxLines(2);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setPadding(Ui.ipx(18), Ui.ipx(18), Ui.ipx(18), Ui.ipx(18));
        label.setGravity(Gravity.BOTTOM | Gravity.START);
        b.addView(label, new FrameLayout.LayoutParams(-1, -1));
        b.setLook(f -> label.setTextColor(f ? Palette.LAPIS_900 : Palette.PLASTER_100));
        b.setOnClickListener(v -> activateApp(pkg));
        nodePackage[node] = pkg;
        placeTile(b, x, y, w, 94);
        return b;
    }

    private View allAppsTile(float x, float y, float w) {
        FocusTile b = new FocusTile(this, Tokens.RADIUS_CELL);
        b.setRest(0, Palette.LINE_DASH, 2).setDashed();
        TextView label = text(ui400, 22, Palette.PLASTER_100);
        // Two lines: the dashed tile is too narrow for "All apps · N" on one.
        label.setText(t.all(Apps.allApps(this, t).size()));
        label.setPadding(Ui.ipx(12), Ui.ipx(18), Ui.ipx(12), Ui.ipx(18));
        label.setGravity(Gravity.BOTTOM | Gravity.START);
        b.addView(label, new FrameLayout.LayoutParams(-1, -1));
        b.setLook(f -> label.setTextColor(f ? Palette.LAPIS_900 : Palette.PLASTER_100));
        b.setOnClickListener(v -> startActivity(new Intent(this, AllAppsActivity.class)));
        placeTile(b, x, y, w, 94);
        return b;
    }

    // ---------------------------------------------------------------- live content

    /** Called every second. Recomputes the prayer day once a minute; the countdown changes every call. */
    private void render(boolean force) {
        Date now = now();
        long minute = now.getTime() / 60000;
        if (force || minute != lastMinute || day == null) {
            lastMinute = minute;
            day = new PrayerDay(this, now);
            renderMinute(now);
        }
        if (secondsUntil(day.nextTime, now) == 0) {
            // The prayer time has arrived: move on to the next one at once.
            day = new PrayerDay(this, new Date(now.getTime() + 1000));
            renderMinute(now);
        }
        if (archCount != null) archCount.setText(t.d(duration(secondsUntil(day.nextTime, now), true)));
        if (arch != null) arch.setState(secondsUntil(day.nextTime, now) <= Tokens.APPROACH_MIN * 60L ? ArchView.APPROACHING : ArchView.REST);
    }

    private void renderMinute(Date now) {
        readClockFormat();
        clock.setText(time(now, 0.4f));
        gdate.setText(gregorian(now));
        hdate.setText(hijri(day) + t.sep() + t.city());

        String[] names = t.prayerNames();
        if (day.friday) names[PrayerDay.DHUHR] = t.jumuah();
        String nextTime = time(day.nextTime, 1f).toString();
        String label = t.nextPrayer(), name = names[day.nextIndex];
        String sub = t.adhanAt() + " " + nextTime + t.sep() + t.city(), sub2 = "";
        if (day.friday && day.nextIndex == PrayerDay.DHUHR) label = t.jumuahLabel();
        if (day.friday) sub2 = t.kahf();
        if (day.ramadan) {
            sub2 = t.suhoorEnds(time(day.suhoorEnd, 1f).toString());
            if (day.nextIndex == PrayerDay.MAGHRIB) {
                label = t.iftarIn();
                name = t.iftar();
                sub = t.prayerNames()[PrayerDay.MAGHRIB] + " " + nextTime + t.sep() + t.city();
            }
        }
        if (archLabel != null) {
            archLabel.setText(label);
            PrayerIcon.set(archName, day.nextIndex, name);
            archSub.setText(t.d(sub));
            archSub2.setText(t.d(sub2));
            archSub2.setVisibility(sub2.isEmpty() ? View.GONE : View.VISIBLE);
        }

        int[] prayers = HomeLayout.prayers(home.table, day.nowIndex, day.nextIndex);
        for (int slot = 0; slot < prayers.length; slot++) {
            int i = prayers[slot];
            if (i < 0) {
                cell[slot].setBackground(round(Palette.LAPIS_800, Tokens.RADIUS_CELL));
                cellTag[slot].setContentDescription(t.nowTag());
                cellTag[slot].setText(t.nowTag()); cellName[slot].setText("—"); cellTime[slot].setText("—");
                cell[slot].setContentDescription(t.noCurrentPrayer());
                for (TextView v : new TextView[] {cellTag[slot], cellName[slot], cellTime[slot]}) {
                    v.setTextColor(Palette.MIST_400); v.setTypeface(ui400);
                }
                continue;
            }
            cell[slot].setContentDescription(null);
            String tag = i == day.nowIndex ? t.nowTag() : (i == day.nextIndex ? t.nextTag() : "");
            if (day.ramadan && i == PrayerDay.FAJR) tag = t.suhoorTag();
            if (day.ramadan && i == PrayerDay.MAGHRIB) tag = t.iftarTag();
            boolean past = i != day.nextIndex && !day.times[i].after(now);
            int bg, fg, ring = 0;
            boolean bold;
            if (i == day.nowIndex) { bg = Palette.PLASTER_100; fg = Palette.LAPIS_900; bold = true; }
            else if (i == day.nextIndex) { bg = 0; fg = Palette.GOLD_400; ring = Palette.GOLD_400; bold = true; }
            else if (past) { bg = Palette.LAPIS_800_60; fg = Palette.MIST_400; bold = false; }
            else { bg = Palette.LAPIS_800; fg = Palette.PLASTER_100; bold = false; }
            GradientDrawable g = new GradientDrawable();
            g.setCornerRadius(Ui.px(Tokens.RADIUS_CELL));
            g.setColor(bg);
            if (ring != 0) g.setStroke(Ui.ipx(3), ring);
            cell[slot].setBackground(g);
            PrayerIcon.set(cellTag[slot], i, tag);
            cellName[slot].setText(names[i]);
            cellTime[slot].setText(time(day.times[i], 0.55f));
            Typeface tf = bold ? ui600 : ui400;
            for (TextView v : new TextView[] {cellTag[slot], cellName[slot], cellTime[slot]}) v.setTextColor(fg);
            cellTag[slot].setTypeface(ui400);
            cellName[slot].setTypeface(tf);
            cellTime[slot].setTypeface(tf);
            if (!t.ar) cellTag[slot].setLetterSpacing(0.2f);
        }
    }

    private void updateWifi() {
        if (wifiName == null) return;
        String ssid = ssid();
        wifiName.setText(ssid == null ? "Wi-Fi" : ssid);
    }

    // ---------------------------------------------------------------- focus

    private void onNodeFocused(int n) {
        if (focusGlow != null) focusGlow.show(nodePackage[n]);
        if (n >= F0 && n < M0) lastFav = n;
    }


    // ---------------------------------------------------------------- radio

    /** The header pill: "Radio" with a broadcast icon when off; level bars and "On air" (gold outline) when on. */
    private View radioPill() {
        FocusTile b = new FocusTile(this, 36);
        b.setRest(Palette.LAPIS_800, Palette.LINE_SOFT, 2);
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.ipx(28), 0, Ui.ipx(28), 0);
        FrameLayout glyph = new FrameLayout(this);
        radioIcon = new Icon(this, Icon.RADIO, 2f);
        glyph.addView(radioIcon, new FrameLayout.LayoutParams(-1, -1));
        pillBars = new RadioBars(this, 3);
        glyph.addView(pillBars, new FrameLayout.LayoutParams(Ui.ipx(22), Ui.ipx(22), Gravity.CENTER));
        radioLabel = text(ui400, 24, Palette.PLASTER_100);
        radioLabel.setSingleLine(true);
        row.addView(glyph, new LinearLayout.LayoutParams(Ui.ipx(30), Ui.ipx(30)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMarginStart(Ui.ipx(14));
        radioLabel.setVisibility(Prefs.quickLabels(this) ? View.VISIBLE : View.GONE);
        b.setContentDescription(t.radio());
        row.addView(radioLabel, lp);
        b.addView(row, new FrameLayout.LayoutParams(-2, -1));
        nodes[RADIO] = b;
        b.setLook(f -> styleRadio());
        b.setOnClickListener(v -> showRadioCard());
        return b;
    }

    /** Puts the pill and the open card in step with the radio. */
    private void styleRadio() {
        if (radioLabel == null || nodes[RADIO] == null) return;
        int state = Radio.state();
        boolean on = Radio.on(), playing = state == Radio.PLAYING;
        boolean focused = nodes[RADIO].isFocused();
        // The pill keeps to a short word; "retrying" reads as connecting there, the card says more.
        int shown = state == Radio.RETRYING ? Radio.CONNECTING : state;
        radioLabel.setText(on ? t.radioStatus(shown) : t.radio());
        radioIcon.setVisibility(on ? View.GONE : View.VISIBLE);
        pillBars.setVisibility(on ? View.VISIBLE : View.GONE);
        pillBars.setLive(playing);
        int fg = focused ? Palette.LAPIS_900 : Palette.PLASTER_100;
        radioLabel.setTextColor(fg);
        radioIcon.setColor(fg);
        pillBars.setColor(focused ? Palette.LAPIS_900 : Palette.GOLD_400);
        ((FocusTile) nodes[RADIO]).setRest(Palette.LAPIS_800, on ? Palette.GOLD_400_60 : Palette.LINE_SOFT, 2);
        if (radioCard != null) {
            cardStatus.setText(t.stationWhere(Radio.station(this)) + t.sep() + t.radioStatus(state));
            cardBars.setLive(playing);
            playLabel.setText(on ? t.radioStop() : t.radioPlay());
            playIcon.setKind(on ? Icon.STOP : Icon.PLAY);
        }
    }

    /**
     * The card under the pill: the station, its state and live bars; Play/Stop (focused) and Screen off. Back
     * closes it and the radio carries on. Built like the reminder card, in the lapis of the header.
     */
    private void showRadioCard() {
        if (radioCard != null || card != null) return;
        radioCard = new LinearLayout(this);
        radioCard.setOrientation(LinearLayout.VERTICAL);
        radioCard.setPadding(Ui.ipx(36), Ui.ipx(30), Ui.ipx(36), Ui.ipx(30));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Palette.LAPIS_700);
        bg.setCornerRadius(Ui.px(24));
        bg.setStroke(Ui.ipx(2), Palette.GOLD_400_60);
        radioCard.setBackground(bg);
        radioCard.setClipChildren(false);
        radioCard.setClipToPadding(false);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        cardBars = new RadioBars(this, 5);
        cardBars.setColor(Palette.GOLD_400);
        top.addView(cardBars, new LinearLayout.LayoutParams(Ui.ipx(72), Ui.ipx(64)));
        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        TextView label = caps(text(ui400, 20, Palette.MIST_400));
        label.setText(t.radio());
        TextView title = text(displayBold, 48, Palette.PLASTER_100);
        title.setText(t.station(Radio.station(this)));
        cardStatus = text(ui400, 24, Palette.GOLD_400);
        words.addView(label);
        words.addView(title);
        words.addView(cardStatus);
        lineHeight(title, 1.15f);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(0, -2, 1);
        wlp.setMarginStart(Ui.ipx(28));
        top.addView(words, wlp);
        radioCard.addView(top);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setClipChildren(false);
        playIcon = new Icon(this, Icon.PLAY, 2f);
        playLabel = text(ui400, 24, Palette.PLASTER_100);
        cardPlay = radioButton(playIcon, playLabel, t.radioPlay());
        cardPlay.setOnClickListener(v -> Radio.toggle(this));
        cardDark = radioButton(new Icon(this, Icon.MOON, 2f), text(ui400, 24, Palette.PLASTER_100), t.screenOff());
        cardDark.setOnClickListener(v -> {
            closeRadioCard(true);
            ScreenOffActivity.start(this);
        });
        float r = FocusTile.REACH;
        buttons.addView(cardPlay, new LinearLayout.LayoutParams(-2, Ui.ipx(68 + 2 * r)));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(-2, Ui.ipx(68 + 2 * r));
        dlp.setMarginStart(Ui.ipx(16 - 2 * r));
        buttons.addView(cardDark, dlp);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-2, -2);
        blp.topMargin = Ui.ipx(28 - r);
        blp.setMarginStart(-Ui.ipx(r));
        radioCard.addView(buttons, blp);

        // Hangs from the header, end-aligned with the Settings button, over the dates and the prayer strip.
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.ipx(720), -2, Gravity.TOP | Gravity.END);
        lp.setMarginEnd(Ui.ipx(1920 - (Tokens.COURT_X + Tokens.COURT_W)));
        lp.topMargin = Ui.ipx(56 + 72 + 24);
        root.addView(radioCard, lp);
        for (View n : nodes) if (n != null) n.setFocusable(false);
        styleRadio();
        cardPlay.requestFocus();
    }

    /** A 68px pill on the card with an icon: lapis at rest, gold when focused. */
    private FocusTile radioButton(Icon icon, TextView v, String label) {
        FocusTile b = new FocusTile(this, 34);
        b.setRest(Palette.LAPIS_900, Palette.LINE_SOFT, 2);
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.ipx(28), 0, Ui.ipx(32), 0);
        v.setText(label);
        row.addView(icon, new LinearLayout.LayoutParams(Ui.ipx(26), Ui.ipx(26)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMarginStart(Ui.ipx(12));
        row.addView(v, lp);
        b.addView(row, new FrameLayout.LayoutParams(-2, -1));
        b.setLook(f -> {
            int c = f ? Palette.LAPIS_900 : Palette.PLASTER_100;
            v.setTextColor(c);
            icon.setColor(c);
        });
        return b;
    }

    private void closeRadioCard(boolean focusPill) {
        if (radioCard == null) return;
        root.removeView(radioCard);
        radioCard = null;
        for (View n : nodes) if (n != null) n.setFocusable(true);
        if (focusPill) nodes[RADIO].requestFocus();
    }

    // ---------------------------------------------------------------- reminder card

    /** "Asr in 13 minutes" at the bottom end, over the apps; it takes focus until Got it, Today's prayers or Back. */
    private void showReminder() {
        if (quickMenu != null) { quickMenu.dismiss(); quickMenu = null; }
        if (card != null) dismissReminder();
        closeRadioCard(false);
        Appearance.load(this);
        Date now = new Date();
        day = new PrayerDay(this, now);
        cardFor = day.nextTime;
        readClockFormat();

        card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.ipx(36), Ui.ipx(32), Ui.ipx(36), Ui.ipx(32));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Palette.LAPIS_900);
        bg.setCornerRadius(Ui.px(24));
        bg.setStroke(Ui.ipx(2), Palette.GOLD_400);
        card.setBackground(bg);
        card.setClipChildren(false);
        card.setClipToPadding(false);

        String[] names = t.prayerNames();
        if (day.friday) names[PrayerDay.DHUHR] = t.jumuah();
        TextView label = caps(text(ui400, 20, Palette.GOLD_400));
        label.setText(t.apLabel());
        TextView title = text(displayBold, 52, Palette.PLASTER_100);
        PrayerIcon.set(title, day.nextIndex, t.apTitle(names[day.nextIndex], Reminders.minutesLeft(day, now), duration(secondsUntil(day.nextTime, now), false)));
        TextView sub = text(ui400, 24, Palette.MIST_400);
        sub.setText(t.apSub(time(day.nextTime, 1f)));
        card.addView(label);
        card.addView(title);
        card.addView(sub);
        lineHeight(title, 1.15f);
        ((LinearLayout.LayoutParams) sub.getLayoutParams()).topMargin += Ui.ipx(6);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setClipChildren(false);
        cardOk = cardButton(t.apOk(), true);
        cardOk.setOnClickListener(v -> dismissReminder());
        cardDay = cardButton(t.dayTitle(), false);
        cardDay.setOnClickListener(v -> {
            dismissReminder();
            startSafely(new Intent(this, PrayerDayActivity.class));
        });
        float r = FocusTile.REACH;
        buttons.addView(cardOk, new LinearLayout.LayoutParams(-2, Ui.ipx(68 + 2 * r)));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(-2, Ui.ipx(68 + 2 * r));
        dlp.setMarginStart(Ui.ipx(16 - 2 * r));
        buttons.addView(cardDay, dlp);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-2, -2);
        blp.topMargin = Ui.ipx(24 - r);
        blp.setMarginStart(-Ui.ipx(r));
        card.addView(buttons, blp);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.ipx(680), -2, Gravity.BOTTOM | Gravity.END);
        lp.setMarginEnd(Ui.ipx(96));
        lp.bottomMargin = Ui.ipx(56);
        root.addView(card, lp);
        for (View n : nodes) if (n != null) n.setFocusable(false);
        cardOk.requestFocus();
    }

    /** A 68px pill on the reminder card, using the same focus colours as Home. */
    private FocusTile cardButton(String label, boolean filled) {
        FocusTile b = new FocusTile(this, 34);
        b.setRest(filled ? Palette.LAPIS_800 : 0, Palette.LINE_SOFT, filled ? 0 : 2);
        TextView v = text(ui400, 24, Palette.PLASTER_100);
        v.setText(label);
        v.setGravity(Gravity.CENTER);
        v.setPadding(Ui.ipx(32), 0, Ui.ipx(32), 0);
        b.addView(v, new FrameLayout.LayoutParams(-2, -1));
        b.setLook(f -> v.setTextColor(f ? Palette.LAPIS_900 : Palette.PLASTER_100));
        return b;
    }

    private void dismissReminder() {
        if (card == null) return;
        root.removeView(card);
        card = null;
        for (View n : nodes) if (n != null) n.setFocusable(true);
        firstApp().requestFocus();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int k = e.getKeyCode();
        if (replacementPackage != null && k == KeyEvent.KEYCODE_BACK) {
            if (e.getAction() == KeyEvent.ACTION_UP) { replacementPackage = null; build(); }
            return true;
        }
        // Source, where the firmware leaves it to the app in front (CVTE) and nothing else opens the source menu.
        if (k == KeyEvent.KEYCODE_TV_INPUT) {
            Intent source = new Intent(this, InputsActivity.class);
            if (source != null) {
                if (e.getAction() == KeyEvent.ACTION_UP && !e.isCanceled()) {
                    try {
                        startActivity(source.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } catch (RuntimeException ignored) {
                        // Not exported or gone after all; the key just does nothing.
                    }
                }
                return true;
            }
        }
        if (radioCard != null) {
            if (k == KeyEvent.KEYCODE_BACK) {
                if (e.getAction() == KeyEvent.ACTION_UP) closeRadioCard(true);
                return true;
            }
            if (k == KeyEvent.KEYCODE_DPAD_LEFT || k == KeyEvent.KEYCODE_DPAD_RIGHT) {
                boolean toEnd = (k == KeyEvent.KEYCODE_DPAD_RIGHT) != rtl;
                if (e.getAction() == KeyEvent.ACTION_DOWN) (toEnd ? cardDark : cardPlay).requestFocus();
                return true;
            }
            if (k == KeyEvent.KEYCODE_DPAD_UP || k == KeyEvent.KEYCODE_DPAD_DOWN) return true;
            return super.dispatchKeyEvent(e);
        }
        if (card != null) {
            if (k == KeyEvent.KEYCODE_BACK) {
                if (e.getAction() == KeyEvent.ACTION_UP) dismissReminder();
                return true;
            }
            if (k == KeyEvent.KEYCODE_DPAD_LEFT || k == KeyEvent.KEYCODE_DPAD_RIGHT) {
                // Got it sits at the start; toward the end is Today's prayers (Left in Arabic).
                boolean toEnd = (k == KeyEvent.KEYCODE_DPAD_RIGHT) != rtl;
                if (e.getAction() == KeyEvent.ACTION_DOWN) (toEnd ? cardDay : cardOk).requestFocus();
                return true;
            }
            if (k == KeyEvent.KEYCODE_DPAD_UP || k == KeyEvent.KEYCODE_DPAD_DOWN) return true;
            return super.dispatchKeyEvent(e);
        }
        if (k == KeyEvent.KEYCODE_BACK) {
            // Back on Home returns to the first app and never leaves Home.
            if (e.getAction() == KeyEvent.ACTION_UP) firstApp().requestFocus();
            return true;
        }
        if (k == KeyEvent.KEYCODE_DPAD_LEFT || k == KeyEvent.KEYCODE_DPAD_RIGHT
                || k == KeyEvent.KEYCODE_DPAD_UP || k == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (e.getAction() == KeyEvent.ACTION_DOWN) move(k);
            return true;
        }
        return super.dispatchKeyEvent(e);
    }

    /** The focus map uses logical directions; Arabic swaps Left/Right. */
    private void move(int key) {
        if (rtl && key == KeyEvent.KEYCODE_DPAD_LEFT) key = KeyEvent.KEYCODE_DPAD_RIGHT;
        else if (rtl && key == KeyEvent.KEYCODE_DPAD_RIGHT) key = KeyEvent.KEYCODE_DPAD_LEFT;
        int from = currentNode();
        if (replacementPackage != null) {
            int to = FavouriteChoice.move(from - F0, favouriteCount,
                key == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : key == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : 0,
                key == KeyEvent.KEYCODE_DPAD_UP ? -1 : key == KeyEvent.KEYCODE_DPAD_DOWN ? 1 : 0, classicHome);
            if (to >= 0) nodes[F0 + to].requestFocus();
            return;
        }
        if (from < 0) { firstApp().requestFocus(); return; }
        int to = FavouriteChoice.homeMove(from, favouriteCount, recentCount, classicHome, lastFav,
            key == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : key == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : 0,
            key == KeyEvent.KEYCODE_DPAD_UP ? -1 : key == KeyEvent.KEYCODE_DPAD_DOWN ? 1 : 0);
        if (nodes[to] != null) nodes[to].requestFocus();
    }

    private View firstApp() {
        View first = nodes[favouriteCount > 0 ? F0 : recentCount > 0 ? M0 : ALL];
        // A mouse/touch app can leave Android in touch mode when Home is pressed.
        first.setFocusableInTouchMode(true);
        return first;
    }

    private int currentNode() {
        View f = getCurrentFocus();
        if (f == null) return -1;
        for (int i = 0; i < NODES; i++) if (nodes[i] == f) return i;
        return -1;
    }

    // ---------------------------------------------------------------- apps

    private boolean readReplacement(Intent intent) {
        String next = intent == null ? null : intent.getStringExtra("replace_favourite");
        if (intent != null) intent.removeExtra("replace_favourite");
        if (next != null && (!Apps.installed(this).contains(next) || Apps.slots(this, t).contains(next))) next = null;
        boolean changed = !java.util.Objects.equals(replacementPackage, next);
        replacementPackage = next;
        return changed;
    }

    private void activateApp(String pkg) {
        if (replacementPackage == null) { if (pkg != null) Apps.launch(this, pkg); return; }
        List<String> current = Apps.slots(this, t);
        int at = current.indexOf(pkg);
        if (at >= 0 && Apps.installed(this).contains(replacementPackage) && !current.contains(replacementPackage))
            Apps.saveFavourites(this, FavouriteChoice.replace(current, at, replacementPackage));
        replacementPackage = null;
        build();
    }

    private String appsKey() {
        return Apps.favourites(this) + "|" + Apps.recent(this) + "|" + Apps.installed(this).toString();
    }
}
