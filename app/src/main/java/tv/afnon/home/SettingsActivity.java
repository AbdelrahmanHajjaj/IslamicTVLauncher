package tv.afnon.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.batoulapps.adhan.Madhab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

/**
 * Afnon Settings (design/Settings.dc.html): sections down the start side, the section's rows beside them, and a
 * preview of today's Asr under the current choices. Appearance waits for Apply; other changes save at once.
 * Other screens rebuild when they come back to the front.
 *
 * <p>Up/Down move between sections or rows. Left/Right preview carousel choices directly; other rows use OK
 * to open a list, flip a switch, or start editing a choice or stepper. While editing, ◀▶ move the choice,
 * OK saves it and Back cancels. Back on a row returns to the sections, Back again goes Home. Network and Device
 * settings open the TV's own screens. Every save shows ✓ Saved at the end of the hint line for a moment.
 */
public class SettingsActivity extends Screen {

    private static final int PRAYER = 0, REMINDERS = 1, RADIO = 2, DISPLAY = 3, APPEARANCE = 4, NETWORK = 5, DEVICE = 6, SECTIONS = 7;
    private static final int VALUE = 0, SEG = 1, STEP = 2, TOGGLE = 3, CAROUSEL = 4;
    private static final float ROWS_TOP = 112, ROW_H = 88, ROW_GAP = 10;
    private static final float NAV_TOP = 152, NAV_H = 76, NAV_GAP = 10;

    /** One row: what it shows and what OK (and ◀▶ while editing) do. */
    private abstract class Row {
        final int kind;
        FocusTile tile;
        TextView label, help;
        LinearLayout control;

        Row(int kind) {
            this.kind = kind;
        }

        abstract String label();

        String help() { return ""; }

        /** VALUE rows. */
        String value() { return ""; }

        /** SEG rows: the choices (already in the screen's digits) and the chosen one. */
        String[] segs() { return new String[0]; }

        int selected() { return 0; }

        void select(int i) {}

        /** STEP rows: the saved amount, its range, and how an amount reads. */
        int amount() { return 0; }

        void setAmount(int n) {}

        int lowest() { return 0; }

        int highest() { return 0; }

        String valueOf(int n) { return ""; }

        boolean on() { return false; }

        void set(boolean on) {}

        /** False for actions or appearance drafts that have not been applied yet. */
        boolean saves() { return true; }

        /** VALUE rows: OK. */
        void open() {}
    }

    private int section = PRAYER;
    private boolean onNav = true;
    private int rowIndex;
    private boolean prayerOffsetsPage;
    private int offsetsParentRow;
    private FrameLayout appearancePreview;
    private TextView appearanceName;
    private LinearLayout appearanceSwatches;
    private Appearance.Style previewStyle;
    private String previewKey;
    private int themeChoice, backgroundChoice, lightChoice;
    private boolean focusChoice, motionChoice;
    private static final String[] BACKGROUNDS = java.util.stream.Stream.concat(java.util.stream.Stream.of("theme"), Arrays.stream(ThemeSpec.BACKGROUNDS)).toArray(String[]::new);
    private final FocusTile[] nav = new FocusTile[SECTIONS];
    private final TextView[] navLabel = new TextView[SECTIONS];
    private final List<Row> rows = new ArrayList<>();
    private TextView sectionLabel, previewValue;
    private TextView saved;
    private final Runnable hideSaved = () -> saved.animate().alpha(0).setDuration(Appearance.motion(Tokens.MOTION_OVERLAY_FADE_MS));
    /** The SEG or STEP row being edited (null: none) and its unsaved choice or amount. */
    private Row editing;
    private int draft;
    private FrameLayout chooser;
    /** What Back in the open chooser goes back to (the city list returns to the countries); null closes it. */
    private Runnable chooserBack;
    /** The voice being previewed in the adhan list. */
    private android.media.MediaPlayer previewPlayer;
    private PrayerDay day;

    @Override
    void build() {
        // A rebuild (back with settings changed elsewhere, or a language change) drops an open chooser with the
        // old root; left set, it would swallow every key while nothing shows.
        stopPreview();
        chooser = null;
        chooserBack = null;
        editing = null;
        newRoot();
        root.setBackgroundColor(Palette.LAPIS_900);
        readClockFormat();

        View side = new View(this);
        side.setBackgroundColor(Palette.LAPIS_950);
        place(side, 0, 0, Tokens.LAYOUT_SETTINGS_SIDEBAR_WIDTH, 1080);

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        Icon mihrab = new Icon(this, Icon.MIHRAB, 1.3f);
        mihrab.setColor(Palette.GOLD_400);
        head.addView(mihrab, new LinearLayout.LayoutParams(Ui.ipx(36), Ui.ipx(46)));
        TextView title = text(displayBold, 52, Palette.PLASTER_100);
        title.setText(t.settings());
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-2, -2);
        tlp.setMarginStart(Ui.ipx(16));
        head.addView(title, tlp);
        lineHeight(title, 1.1f);
        head.setClipChildren(false);
        place(head, 96, 56, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < SECTIONS; i++) nav[i] = navItem(i);

        LinearLayout preview = new LinearLayout(this);
        preview.setOrientation(LinearLayout.VERTICAL);
        preview.setPadding(Ui.ipx(24), Ui.ipx(20), Ui.ipx(24), Ui.ipx(20));
        preview.setBackground(round(Palette.LAPIS_800, 16));
        TextView pl = text(ui400, 20, Palette.MIST_400);
        pl.setText(t.preview());
        previewValue = text(display, Tokens.FONT_SIZE_DISPLAY_XS, Palette.GOLD_400);
        preview.addView(pl);
        preview.addView(previewValue);
        lineHeight(previewValue, 1.2f);
        ((LinearLayout.LayoutParams) previewValue.getLayoutParams()).topMargin += Ui.ipx(6);
        place(preview, 96, 1080 - 56 - 124, 424, 124);

        sectionLabel = caps(text(ui400, 22, Palette.MIST_400));
        sectionLabel.setAllCaps(!t.ar);
        place(sectionLabel, 640, 56, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Save feedback sits below the settings rows.
        saved = text(ui500, 22, Palette.GOLD_400);
        saved.setText(t.saved());
        saved.setAlpha(0);
        placeEnd(saved, 1920 - 640 - Tokens.LAYOUT_SETTINGS_ROW_WIDTH, 1016,
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setContentView(root);
        showSection();
        if (onNav || rows.isEmpty()) focusNav();
        else focusRow(Math.min(rowIndex, rows.size() - 1));
    }

    @Override
    void onTick() {
        // Nothing moves on this screen; the preview changes only with the settings.
    }

    // ---------------------------------------------------------------- sections

    private String sectionName(int i) {
        switch (i) {
            case PRAYER: return t.sectionPrayer();
            case REMINDERS: return t.sectionReminders();
            case RADIO: return t.sectionRadio();
            case DISPLAY: return t.sectionDisplay();
            case APPEARANCE: return t.sectionAppearance();
            case NETWORK: return t.sectionNetwork(ssid());
            default: return t.sectionDevice();
        }
    }

    private FocusTile navItem(int i) {
        FocusTile b = new FocusTile(this, 14);
        b.setFocusScale(1.04f);
        TextView v = text(ui400, 26, Palette.MIST_400);
        v.setText(sectionName(i));
        v.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        v.setPadding(Ui.ipx(24), 0, Ui.ipx(24), 0);
        v.setSingleLine(true);
        b.addView(v, new FrameLayout.LayoutParams(-1, -1));
        navLabel[i] = v;
        b.setLook(f -> styleNav(i));
        placeTile(b, 96, NAV_TOP + i * (NAV_H + NAV_GAP), 424, NAV_H);
        return b;
    }

    /** The open section keeps a gold inset ring when focus is in its rows; focused, it is the gold tile. */
    private void styleNav(int i) {
        boolean focused = nav[i] != null && nav[i].isFocused();
        boolean open = i == section;
        if (nav[i] != null) nav[i].setRest(open ? Palette.LAPIS_800 : 0, Palette.GOLD_400, open ? 2 : 0);
        navLabel[i].setTextColor(focused ? Palette.LAPIS_900 : (open ? Palette.PLASTER_100 : Palette.MIST_400));
    }

    private void showSection() {
        for (Row r : rows) root.removeView(r.tile);
        rows.clear();
        sectionLabel.setText(prayerOffsetsPage ? t.prayerOffsets() : sectionName(section));
        if (section == NETWORK) sectionLabel.setText(t.sectionNetwork(null));
        for (int i = 0; i < SECTIONS; i++) styleNav(i);
        if (appearancePreview != null) root.removeView(appearancePreview);
        appearancePreview = null;
        if (section == APPEARANCE) {
            themeChoice = Math.max(0, Arrays.asList(Appearance.PRESETS).indexOf(prefs().getString("theme", "mihrab")));
            backgroundChoice = Math.max(0, Arrays.asList(BACKGROUNDS).indexOf(prefs().getString("background", "theme")));
            lightChoice = prefs().getInt("lighting", -1);
            focusChoice = prefs().getBoolean("focus_lighting", false);
            motionChoice = Appearance.reduced(this);
            showAppearancePreview();
        }
        rows.addAll(prayerOffsetsPage ? offsetRows() : rowsFor(section));
        for (int i = 0; i < rows.size(); i++) addRow(rows.get(i), i);
        refresh();
    }

    private List<Row> rowsFor(int s) {
        Context c = this;
        switch (s) {
            case PRAYER: return Arrays.asList(
                new Row(VALUE) {
                    String label() { return t.location(); }
                    String help() { Prefs.City city = Prefs.city(c); return t.locationHelp(city.lat, city.lng); }
                    String value() { Prefs.City city = Prefs.city(c); return t.ar ? t.place(city.ar, city.countryAr) : t.place(city.en, city.countryEn); }
                    void open() {
                        // The next list opens once this chooser has closed (closeChooser runs after the pick).
                        openChooser(t.region(), t.regions(), Prefs.region(Prefs.city(c)),
                            i -> root.post(() -> openCountries(i, this::open)));
                    }
                },
                new Row(VALUE) {
                    String label() { return t.method(); }
                    String help() { return t.methodAngles(Prefs.method(c)); }
                    String value() { return t.methodName(Prefs.method(c)); }
                    void open() {
                        String[] names = new String[Prefs.METHODS.length];
                        int sel = 0;
                        for (int i = 0; i < names.length; i++) {
                            names[i] = t.methodName(Prefs.METHODS[i]);
                            if (Prefs.METHODS[i] == Prefs.method(c)) sel = i;
                        }
                        openChooser(t.method(), names, sel, i -> { Prefs.setMethod(c, Prefs.METHODS[i]); showSaved(); });
                    }
                },
                new Row(SEG) {
                    String label() { return t.asrSchool(); }
                    String help() { return t.asrSchoolHelp(); }
                    String[] segs() { return t.asrSchools(); }
                    int selected() { return Prefs.madhab(c) == Madhab.HANAFI ? 1 : 0; }
                    void select(int i) { Prefs.setMadhab(c, i == 1 ? Madhab.HANAFI : Madhab.SHAFI); }
                },
                new Row(VALUE) {
                    String label() { return t.prayerOffsets(); }
                    String help() { return t.prayerOffsetsHelp(); }
                    String value() {
                        int changed = 0;
                        for (int n : Prefs.prayerOffsets(c)) if (n != 0) changed++;
                        return t.offsetCount(changed);
                    }
                    void open() { openPrayerOffsets(); }
                },
                new Row(STEP) {
                    String label() { return t.hijriAdjust(); }
                    String help() { return t.hijriToday(hijri(day)); }
                    int amount() { return Prefs.hijriOffset(c); }
                    void setAmount(int n) { Prefs.setHijriOffset(c, n); }
                    int lowest() { return -2; }
                    int highest() { return 2; }
                    String valueOf(int n) { return t.days(n); }
                },
                new Row(VALUE) {
                    String label() { return t.roomRow(); }
                    String help() { return t.roomHelp(); }
                    String value() { int f = facing(); return f < 0 ? t.notSet() : t.bearingLabel(f); }
                    void open() { startSafely(new Intent(c, PrayerDayActivity.class).putExtra("room", true)); }
                });
            case REMINDERS: return Arrays.asList(
                new Row(VALUE) {
                    // Six choices are too many for a segmented control: a list, like the adhan voices.
                    final int[] minutes = {5, 10, 15, 20, 30, 0};
                    String label() { return t.remind(); }
                    String help() { return t.remindHelp(); }
                    int selected() {
                        int m = Prefs.remindMinutes(c);
                        for (int i = 0; i < minutes.length; i++) if (minutes[i] == m) return i;
                        return 2;
                    }
                    String value() { return digits(t.remindChoices())[selected()]; }
                    void open() {
                        openChooser(t.remind(), digits(t.remindChoices()), selected(),
                            i -> { Prefs.setRemindMinutes(c, minutes[i]); showSaved(); });
                    }
                },
                new Row(TOGGLE) {
                    String label() { return t.remindVoiceRow(); }
                    String help() { return t.remindVoiceHelp(); }
                    boolean on() { return Prefs.remindVoice(c); }
                    void set(boolean on) { Prefs.setRemindVoice(c, on); }
                },
                new Row(TOGGLE) {
                    String label() { return t.adhanRow(); }
                    String help() { return t.adhanHelp(); }
                    boolean on() { return Prefs.adhan(c); }
                    void set(boolean on) { Prefs.setAdhan(c, on); }
                },
                new Row(TOGGLE) {
                    String label() { return t.announceRow(); }
                    String help() { return t.announceHelp(); }
                    boolean on() { return Prefs.announce(c); }
                    void set(boolean on) { Prefs.setAnnounce(c, on); }
                },
                new Row(SEG) {
                    final int[] minutes = {0, 1, 5, 10};
                    String label() { return t.callCloseRow(); }
                    String help() { return t.callCloseHelp(); }
                    String[] segs() { return digits(t.callCloseChoices()); }
                    int selected() {
                        int m = Prefs.callCloseMinutes(c);
                        for (int i = 0; i < minutes.length; i++) if (minutes[i] == m) return i;
                        return minutes.length - 1;
                    }
                    void select(int i) { Prefs.setCallCloseMinutes(c, minutes[i]); }
                },
                voiceRow(false),
                voiceRow(true),
                new Row(VALUE) {
                    String label() { return t.testPrayerAlerts(); }
                    String help() { return RemoteKeys.watch == null ? t.serviceHelp() : t.testPrayerHelp(); }
                    void open() {
                        if (RemoteKeys.watch == null) {
                            SystemScreens.open(SettingsActivity.this, Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            return;
                        }
                        openChooser(t.testPrayerAlerts(), t.testPrayerChoices(), 0,
                            i -> { if (RemoteKeys.watch != null) RemoteKeys.watch.test(i == 0 ? "banner" : "call", 0); });
                    }
                });
            case RADIO: return Arrays.asList(
                new Row(TOGGLE) {
                    String label() { return t.station(Radio.station(c)); }
                    String help() { return t.radioRowHelp(Radio.station(c)); }
                    boolean on() { return Radio.on(); }
                    void set(boolean on) { if (on) Radio.start(c); else Radio.stop(); }
                    boolean saves() { return false; }
                },
                new Row(VALUE) {
                    String label() { return t.stationRow(); }
                    String help() { return t.stationGroups()[Radio.station(c).reciter ? 1 : 0]; }
                    String value() { return t.stationChoice(Radio.station(c)); }
                    void open() {
                        int sel = Radio.station(c).reciter ? 1 : 0;
                        // The station list opens once this chooser has closed (closeChooser runs after the pick).
                        openChooser(t.stationRow(), t.stationGroups(), sel, i -> root.post(() -> openStations(i == 1, this::open)));
                    }
                },
                new Row(SEG) {
                    String label() { return t.afterAdhanRow(); }
                    String help() { return t.afterAdhanHelp(); }
                    String[] segs() { return t.afterAdhanChoices(); }
                    int selected() { return Prefs.radioResume(c) ? 0 : 1; }
                    void select(int i) { Prefs.setRadioResume(c, i == 0); }
                },
                new Row(SEG) {
                    final int[] minutes = {0, 15, -1};
                    String label() { return t.radioDarkRow(); }
                    String help() { return t.radioDarkHelp(); }
                    String[] segs() { return digits(t.radioDarkChoices()); }
                    int selected() {
                        int m = Prefs.radioScreenOff(c);
                        for (int i = 0; i < minutes.length; i++) if (minutes[i] == m) return i;
                        return 0;
                    }
                    void select(int i) { Prefs.setRadioScreenOff(c, minutes[i]); }
                });
            case DISPLAY: return Arrays.asList(
                new Row(SEG) {
                    final String[] values = {"system", "ar", "en"};
                    String label() { return t.language(); }
                    String help() { return t.languageHelp(); }
                    String[] segs() { return t.languages(); }
                    int selected() { return Math.max(0, Arrays.asList(values).indexOf(prefs().getString("locale", "system"))); }
                    void select(int i) { prefs().edit().putString("locale", values[i]).apply(); relaunch(); }
                },
                new Row(SEG) {
                    String label() { return t.digits(); }
                    String[] segs() { return new String[] {"0123", "٠١٢٣"}; }
                    int selected() { return t.eastern ? 1 : 0; }
                    void select(int i) { prefs().edit().putString("digits", i == 1 ? "eastern" : "western").apply(); relaunch(); }
                },
                new Row(SEG) {
                    final String[] values = {"system", "12", "24"};
                    String label() { return t.clockRow(); }
                    String help() { return t.clockHelp(); }
                    String[] segs() { return digits(t.clocks()); }
                    int selected() { return Math.max(0, Arrays.asList(values).indexOf(Prefs.clock(c))); }
                    void select(int i) { prefs().edit().putString("clock", values[i]).apply(); readClockFormat(); }
                },
                new Row(TOGGLE) {
                    String label() { return t.hideSystemApps(); }
                    String help() { return t.hideSystemAppsHelp(); }
                    boolean on() { return prefs().getBoolean("hide_system_apps", false); }
                    void set(boolean on) { prefs().edit().putBoolean("hide_system_apps", on).apply(); }
                },
                new Row(TOGGLE) {
                    String label() { return t.quickLabels(); }
                    boolean on() { return Prefs.quickLabels(c); }
                    void set(boolean on) { prefs().edit().putBoolean("quick_labels", on).apply(); }
                },
                new Row(VALUE) {
                    final String[] modes = {"hidden", "tab", "off"};
                    String label() { return t.prayerDock(); }
                    int selected() { return Math.max(0, Arrays.asList(modes).indexOf(prefs().getString("dock_mode", "hidden"))); }
                    String value() { return t.dockModes()[selected()]; }
                    void open() { openChooser(label(), t.dockModes(), selected(), i -> { prefs().edit().putString("dock_mode", modes[i]).apply(); showSaved(); }); }
                },
                new Row(SEG) {
                    String label() { return t.dockPosition(); }
                    String[] segs() { return t.dockPositions(); }
                    int selected() { return "top".equals(prefs().getString("dock_position", "bottom")) ? 0 : 1; }
                    void select(int i) { prefs().edit().putString("dock_position", i == 0 ? "top" : "bottom").apply(); }
                },
                new Row(VALUE) {
                    String label() { return t.openDock(); }
                    void open() { if (!RemoteKeys.toggleDock()) SystemScreens.open(c, Settings.ACTION_ACCESSIBILITY_SETTINGS); }
                });
            case APPEARANCE: return Arrays.asList(
                new Row(CAROUSEL) {
                    String label() { return t.themeRow(); }
                    String[] segs() { return Arrays.copyOf(t.themeNames(), Appearance.PRESETS.length - (prefs().contains("theme_json") ? 0 : 1)); }
                    int selected() { return themeChoice; }
                    void select(int i) { themeChoice = i; }
                },
                new Row(CAROUSEL) {
                    String label() { return t.backgroundRow(); }
                    String[] segs() { return t.backgroundNames(); }
                    int selected() { return backgroundChoice; }
                    void select(int i) { backgroundChoice = i; }
                },
                new Row(STEP) {
                    String label() { return t.lightingRow(); }
                    int amount() { return previewStyle.lighting; }
                    int lowest() { return 0; } int highest() { return 100; }
                    String valueOf(int n) { return t.d(n + "%"); }
                    void setAmount(int n) { lightChoice = n; }
                    boolean saves() { return false; }
                },
                new Row(TOGGLE) {
                    String label() { return t.focusLighting(); }
                    String help() { return t.focusLightingHelp(); }
                    boolean on() { return focusChoice; }
                    void set(boolean on) { focusChoice = on; }
                    boolean saves() { return false; }
                },
                new Row(TOGGLE) {
                    String label() { return t.reduceMotion(); }
                    boolean on() { return motionChoice; }
                    void set(boolean on) { motionChoice = on; }
                    boolean saves() { return false; }
                },
                new Row(VALUE) {
                    String label() { return t.applyAppearance(); }
                    void open() {
                        android.content.SharedPreferences.Editor save = prefs().edit()
                            .putString("theme", Appearance.PRESETS[themeChoice]).putString("background", BACKGROUNDS[backgroundChoice])
                            .putBoolean("focus_lighting", focusChoice).putBoolean("reduce_motion", motionChoice);
                        if (lightChoice < 0) save.remove("lighting"); else save.putInt("lighting", lightChoice);
                        save.apply(); relaunch(); showSaved();
                    }
                });
            case NETWORK: return Arrays.asList(
                new Row(VALUE) {
                    String label() { return t.networkRow(); }
                    String help() { return t.networkHelp(); }
                    String value() { String s = ssid(); return s == null ? "" : s; }
                    void open() { SystemScreens.open(SettingsActivity.this, Settings.ACTION_WIFI_SETTINGS); }
                });
            default: return Arrays.asList(
                new Row(VALUE) {
                    String label() { return t.deviceRow(); }
                    String help() { return t.deviceHelp(); }
                    void open() { SystemScreens.open(SettingsActivity.this, Settings.ACTION_SETTINGS); }
                },
                new Row(VALUE) {
                    String label() { return t.homeSettings(); }
                    void open() { SystemScreens.home(SettingsActivity.this); }
                },
                new Row(VALUE) {
                    String label() { return t.inputsTitle(); }
                    void open() { startSafely(new Intent(c, InputsActivity.class)); }
                },
                new Row(VALUE) {
                    String label() { return t.screensaver(); }
                    void open() { SystemScreens.dreams(SettingsActivity.this); }
                },
                new Row(VALUE) {
                    String label() { return t.remoteButtons(); }
                    void open() { startSafely(new Intent(c, RemoteButtonsActivity.class)); }
                },
                new Row(VALUE) {
                    String label() { return t.serviceHealth(); }
                    String help() { return t.serviceHelp(); }
                    String value() { return RemoteKeys.watch == null ? t.serviceStopped() : t.serviceRunning(); }
                    void open() { SystemScreens.open(SettingsActivity.this, Settings.ACTION_ACCESSIBILITY_SETTINGS); }
                },
                new Row(TOGGLE) {
                    String label() { return t.repairService(); }
                    String help() { return t.repairHelp(); }
                    boolean on() { return prefs().getBoolean("repair_service", false); }
                    void set(boolean on) { prefs().edit().putBoolean("repair_service", on).apply(); }
                });
        }
    }

    /** The adhan recording for every prayer but Fajr, or for Fajr: a list that plays each voice as focus reaches it. */
    private Row voiceRow(boolean fajr) {
        return new Row(VALUE) {
            String label() { return fajr ? t.fajrVoiceRow() : t.voiceRow(); }
            String help() { return fajr ? t.fajrVoiceHelp() : t.voiceHelp(); }
            String value() { return Adhans.name(t, Prefs.voice(SettingsActivity.this, fajr)); }
            void open() {
                String[] names = new String[Adhans.VOICES.length];
                for (int i = 0; i < names.length; i++) names[i] = Adhans.name(t, Adhans.VOICES[i][0]);
                openChooser(label(), names, Adhans.index(Prefs.voice(SettingsActivity.this, fajr)),
                    i -> { Prefs.setVoice(SettingsActivity.this, fajr, Adhans.VOICES[i][0]); showSaved(); },
                    i -> preview(Adhans.VOICES[i][0]));
            }
        };
    }

    private void preview(String id) {
        Adhans.stop(previewPlayer);
        previewPlayer = Adhans.play(this, id);
    }

    private void stopPreview() {
        Adhans.stop(previewPlayer);
        previewPlayer = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Settings has its own task; returning from Home discards an unapplied appearance draft.
        if (section == APPEARANCE) {
            editing = null;
            showSection();
            if (onNav) focusNav(); else focusRow(Math.min(rowIndex, rows.size() - 1));
        }
    }

    @Override
    protected void onPause() {
        stopPreview();
        super.onPause();
    }

    private String[] digits(String[] s) {
        String[] out = new String[s.length];
        for (int i = 0; i < s.length; i++) out[i] = t.d(s[i]);
        return out;
    }

    /** Language or digits changed: every string on the screen changes, so rebuild it where it was. */
    private void relaunch() {
        loadSettings(null);
        build();
    }

    // ---------------------------------------------------------------- rows

    private void addRow(Row r, int i) {
        r.tile = new FocusTile(this, 16);
        r.tile.setFocusScale(Tokens.FOCUS_SCALE_SETTINGS_ROW);
        r.tile.setRest(Palette.LAPIS_800, 0, 0);
        LinearLayout line = new LinearLayout(this);
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.setPadding(Ui.ipx(28), 0, Ui.ipx(28), 0);
        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        r.label = text(ui500, 26, Palette.PLASTER_100);
        r.help = text(ui400, 20, Palette.PLASTER_100);
        r.help.setSingleLine(true);
        words.addView(r.label);
        words.addView(r.help);
        lineHeight(r.label, 1.25f);
        lineHeight(r.help, 1.35f);
        line.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
        r.control = new LinearLayout(this);
        r.control.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-2, -2);
        clp.setMarginStart(Ui.ipx(24));
        line.addView(r.control, clp);
        r.tile.addView(line, new FrameLayout.LayoutParams(-1, -1));
        r.tile.setLook(f -> render(r, f));
        placeTile(r.tile, 640, (section == APPEARANCE ? 410 : ROWS_TOP) + i * (ROW_H + ROW_GAP), Tokens.LAYOUT_SETTINGS_ROW_WIDTH, ROW_H);
    }

    /** Refresh rows and previews from saved settings or the appearance draft. */
    private void refresh() {
        if (appearancePreview != null) updateAppearancePreview();
        day = new PrayerDay(this, new Date());
        for (Row r : rows) render(r, r.tile.isFocused());
        previewValue.setText(time(day.times[PrayerDay.ASR], 1f));
    }

    private void render(Row r, boolean focused) {
        if (day == null) return;
        int ink = focused ? Palette.LAPIS_900 : Palette.PLASTER_100;
        r.label.setText(r.label());
        r.label.setTextColor(ink);
        String help = r.help();
        r.help.setText(help);
        r.help.setVisibility(help.isEmpty() ? View.GONE : View.VISIBLE);
        r.help.setTextColor(ink);
        r.help.setAlpha(0.75f);
        r.control.removeAllViews();
        boolean edit = r == editing;
        if (edit) {
            // Editing: a lapis ring around the control on the gold row.
            GradientDrawable ring = round(0, 34);
            ring.setStroke(Ui.ipx(3), Palette.LAPIS_900);
            r.control.setBackground(ring);
            int pad = Ui.ipx(6);
            r.control.setPadding(pad, pad, pad, pad);
        } else {
            r.control.setBackground(null);
            r.control.setPadding(0, 0, 0, 0);
        }
        switch (r.kind) {
            case CAROUSEL: {
                String[] names = r.segs();
                int selected = r.selected();
                for (int i = selected - 1; i <= selected + 1; i++) {
                    TextView chip = text(ui400, 22, i == selected ? Palette.PLASTER_100 : ink);
                    chip.setText(i >= 0 && i < names.length ? names[i] : "");
                    chip.setGravity(Gravity.CENTER);
                    chip.setSingleLine(true);
                    chip.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    chip.setPadding(Ui.ipx(12), 0, Ui.ipx(12), 0);
                    if (i == selected) chip.setBackground(round(Palette.LAPIS_700, 26));
                    else chip.setAlpha(.65f);
                    r.control.addView(chip, new LinearLayout.LayoutParams(Ui.ipx(190), Ui.ipx(52)));
                }
                break;
            }
            case VALUE: {
                TextView v = text(ui400, 24, ink);
                v.setText(r.value());
                v.setSingleLine(true);
                r.control.addView(v);
                Chevron ch = new Chevron(this, ink);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.ipx(22), Ui.ipx(22));
                lp.setMarginStart(Ui.ipx(12));
                r.control.addView(ch, lp);
                break;
            }
            case SEG: {
                LinearLayout track = new LinearLayout(this);
                int pad = Ui.ipx(6);
                track.setPadding(pad, pad, pad, pad);
                track.setBackground(round(focused ? Palette.SEG_TRACK_FOCUSED : Palette.LAPIS_900, 30));
                String[] segs = r.segs();
                for (int i = 0; i < segs.length; i++) {
                    boolean chosen = i == (edit ? draft : r.selected());
                    TextView s = text(ui400, 22, chosen ? (focused ? Palette.PLASTER_100 : Palette.LAPIS_900) : (focused ? Palette.LAPIS_900 : Palette.MIST_400));
                    s.setText(segs[i]);
                    s.setGravity(Gravity.CENTER);
                    s.setPadding(Ui.ipx(24), 0, Ui.ipx(24), 0);
                    if (chosen) {
                        GradientDrawable bg = round(focused ? Palette.LAPIS_900 : Palette.PLASTER_100, 26);
                        if (focused) bg.setStroke(Ui.ipx(3), Palette.PLASTER_100);
                        s.setBackground(bg);
                    }
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, Ui.ipx(52));
                    if (i > 0) lp.setMarginStart(Ui.ipx(6));
                    track.addView(s, lp);
                }
                r.control.addView(track);
                break;
            }
            case STEP: {
                r.control.addView(stepButton("−"), new LinearLayout.LayoutParams(Ui.ipx(48), Ui.ipx(48)));
                TextView v = text(ui400, 26, ink);
                v.setText(r.valueOf(edit ? draft : r.amount()));
                v.setGravity(Gravity.CENTER);
                v.setMinWidth(Ui.ipx(150));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
                lp.setMarginStart(Ui.ipx(18));
                lp.setMarginEnd(Ui.ipx(18));
                r.control.addView(v, lp);
                r.control.addView(stepButton("+"), new LinearLayout.LayoutParams(Ui.ipx(48), Ui.ipx(48)));
                break;
            }
            default:
                r.control.addView(new Switch(this, r.on(), focused), new LinearLayout.LayoutParams(Ui.ipx(88), Ui.ipx(48)));
        }
    }

    private TextView stepButton(String sign) {
        TextView b = text(ui400, 26, Palette.PLASTER_100);
        b.setText(sign);
        b.setGravity(Gravity.CENTER);
        b.setIncludeFontPadding(false);
        b.setBackground(round(Palette.LAPIS_700, 24));
        return b;
    }

    private static GradientDrawable round(int color, float radiusDesignPx) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(Ui.px(radiusDesignPx));
        return d;
    }

    // ---------------------------------------------------------------- focus and keys

    private void focusNav() {
        onNav = true;
        nav[section].requestFocus();
    }

    private void focusRow(int i) {
        onNav = false;
        rowIndex = i;
        Row r = rows.get(i);
        r.tile.requestFocus();

        styleNav(section);
    }

    /** OK on a SEG or STEP row: ◀▶ now move an unsaved choice, OK saves it, Back cancels. */
    private void startEditing(Row r) {
        editing = r;
        draft = r.kind == SEG ? r.selected() : r.amount();
        render(r, true);
    }

    /** Leaves editing; save keeps the draft if it differs from what is saved. */
    private void stopEditing(boolean save) {
        Row r = editing;
        editing = null;
        if (save && draft != (r.kind == SEG ? r.selected() : r.amount())) {
            if (r.kind == SEG) r.select(draft);
            else r.setAmount(draft);
            if (r.saves()) showSaved();
        }
        // A language or digits change rebuilt the screen and its rows already.
        if (rows.contains(r)) {
            refresh();
            focusRow(rowIndex);
        }
    }

    /** Brief feedback after a settings change. */
    private void showSaved() {
        saved.removeCallbacks(hideSaved);
        saved.animate().alpha(1).setDuration(Appearance.motion(Tokens.MOTION_OVERLAY_FADE_MS));
        saved.postDelayed(hideSaved, Tokens.MOTION_SAVED_LIFETIME_MS);
    }

    private void navTo(int i) {
        if (i < 0 || i >= SECTIONS || i == section) return;
        section = i;
        prayerOffsetsPage = false;
        showSection();
        focusNav();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int k = e.getKeyCode();
        boolean down = e.getAction() == KeyEvent.ACTION_DOWN;
        if (chooser != null) {
            if (k == KeyEvent.KEYCODE_BACK) {
                if (!down) {
                    Runnable back = chooserBack;
                    closeChooser();
                    if (back != null) back.run();
                }
                return true;
            }
            if (k == KeyEvent.KEYCODE_DPAD_LEFT || k == KeyEvent.KEYCODE_DPAD_RIGHT) return true;
            return super.dispatchKeyEvent(e);
        }
        if (k == KeyEvent.KEYCODE_BACK) {
            if (!down) {
                if (editing != null) stopEditing(false);
                else if (prayerOffsetsPage) {
                    prayerOffsetsPage = false;
                    showSection();
                    focusRow(offsetsParentRow);
                }
                else if (onNav) finish();
                else focusNav();
            }
            return true;
        }
        boolean ok = k == KeyEvent.KEYCODE_DPAD_CENTER || k == KeyEvent.KEYCODE_ENTER || k == KeyEvent.KEYCODE_NUMPAD_ENTER;
        boolean horizontal = k == KeyEvent.KEYCODE_DPAD_LEFT || k == KeyEvent.KEYCODE_DPAD_RIGHT;
        boolean vertical = k == KeyEvent.KEYCODE_DPAD_UP || k == KeyEvent.KEYCODE_DPAD_DOWN;
        if (!ok && !horizontal && !vertical) return super.dispatchKeyEvent(e);
        if (!down || (ok && e.getRepeatCount() > 0)) return true;
        // "Toward the end" is Right in English and Left in Arabic; choices and steppers run the same way.
        boolean toEnd = (k == KeyEvent.KEYCODE_DPAD_RIGHT) != rtl;
        int up = k == KeyEvent.KEYCODE_DPAD_UP ? -1 : 1;

        if (onNav) {
            if (vertical) navTo(section + up);
            else if (ok && section == NETWORK) startSafely(new Intent(Settings.ACTION_WIFI_SETTINGS));
            else if ((ok || toEnd) && !rows.isEmpty()) focusRow(0);
            return true;
        }

        if (editing != null) {
            if (ok) stopEditing(true);
            else if (horizontal) {
                Row r = editing;
                int lo = r.kind == SEG ? 0 : r.lowest();
                int hi = r.kind == SEG ? r.segs().length - 1 : r.highest();
                int to = Math.max(lo, Math.min(hi, draft + (toEnd ? 1 : -1)));
                if (to != draft) {
                    draft = to;
                    render(r, true);
                }
            }
            // Up/Down stay on the row being edited.
            return true;
        }

        Row r = rows.get(rowIndex);
        if (vertical) {
            int to = rowIndex + up;
            if (to >= 0 && to < rows.size()) focusRow(to);
            return true;
        }
        if (horizontal) {
            if (r.kind == CAROUSEL) {
                r.select(Math.max(0, Math.min(r.segs().length - 1, r.selected() + (toEnd ? 1 : -1))));
                refresh();
            } else if (!toEnd) focusNav();
            return true;
        }
        switch (r.kind) {
            case CAROUSEL: return true;
            case SEG:
            case STEP:
                startEditing(r);
                return true;
            case TOGGLE:
                r.set(!r.on());
                if (r.saves()) showSaved();
                refresh();
                return true;
            default:
                r.open();
                return true;
        }
    }

    // ---------------------------------------------------------------- list chooser

    /** Location's second step: one region's countries, A to Z in the app's language ("ال" not counted). Back runs back. */
    private void openCountries(int region, Runnable back) {
        List<String> countries = new ArrayList<>();
        for (String en : Prefs.REGIONS[region]) {
            for (Prefs.City city : Prefs.CITIES) {
                if (city.countryEn.equals(en)) { countries.add(t.ar ? city.countryAr : city.countryEn); break; }
            }
        }
        java.text.Collator order = java.text.Collator.getInstance(t.ar ? new Locale("ar") : Locale.ENGLISH);
        Collections.sort(countries, (a, b) -> order.compare(a.replaceFirst("^ال", ""), b.replaceFirst("^ال", "")));
        Prefs.City now = Prefs.city(this);
        int sel = countries.indexOf(t.ar ? now.countryAr : now.countryEn);
        Runnable self = () -> openCountries(region, back);
        openChooser(t.regions()[region], countries.toArray(new String[0]), sel,
            i -> root.post(() -> openCities(countries.get(i), self)));
        chooserBack = back;
    }

    /** Location's third step: one country's cities. A country with a single city takes it at once. Back runs back. */
    private void openCities(String country, Runnable back) {
        List<Integer> found = new ArrayList<>();
        for (int i = 0; i < Prefs.CITIES.length; i++) {
            Prefs.City city = Prefs.CITIES[i];
            if ((t.ar ? city.countryAr : city.countryEn).equals(country)) found.add(i);
        }
        if (found.size() == 1) {
            Prefs.setCity(this, found.get(0));
            refresh();
            focusRow(rowIndex);
            showSaved();
            return;
        }
        String[] names = new String[found.size()];
        int current = Prefs.cityIndex(this);
        // -1 when the saved city is in another country: nothing is marked, focus starts on the first city.
        int sel = -1;
        for (int k = 0; k < names.length; k++) {
            Prefs.City city = Prefs.CITIES[found.get(k)];
            names[k] = t.ar ? city.ar : city.en;
            if (found.get(k) == current) sel = k;
        }
        openChooser(country, names, sel, k -> { Prefs.setCity(this, found.get(k)); showSaved(); });
        chooserBack = back;
    }

    private void openPrayerOffsets() {
        offsetsParentRow = rowIndex;
        prayerOffsetsPage = true;
        showSection();
        focusRow(0);
    }

    private List<Row> offsetRows() {
        List<Row> result = new ArrayList<>();
        for (int i = 0; i < t.prayerNames().length; i++) {
            final int prayer = i;
            result.add(new Row(STEP) {
                String label() { return t.prayerNames()[prayer]; }
                int amount() { return Prefs.prayerOffset(SettingsActivity.this, prayer); }
                void setAmount(int n) { Prefs.setPrayerOffset(SettingsActivity.this, prayer, n); }
                int lowest() { return -Prefs.MAX_PRAYER_OFFSET; }
                int highest() { return Prefs.MAX_PRAYER_OFFSET; }
                String valueOf(int n) { return t.offsetMinutes(n); }
            });
        }
        return result;
    }

    private void showAppearancePreview() {
        appearancePreview = new FrameLayout(this);
        previewKey = null;
        previewStyle = Appearance.style(this, Appearance.PRESETS[themeChoice], BACKGROUNDS[backgroundChoice], lightChoice);
        View home = new View(this) {
            final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override protected void onDraw(Canvas canvas) {
                canvas.save(); canvas.scale(getWidth() / 480f, getHeight() / 270f);
                if (rtl) { canvas.translate(480, 0); canvas.scale(-1, 1); }
                p.setColor(previewStyle.colors[2]);
                canvas.drawRoundRect(22, 35, 158, 248, 68, 68, p);
                p.setColor(previewStyle.colors[4]); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1);
                canvas.drawRoundRect(22, 35, 158, 248, 68, 68, p); p.setStyle(Paint.Style.FILL);
                // Text stays readable in Arabic; only the miniature layout is mirrored.
                canvas.save(); if (rtl) canvas.scale(-1, 1, 90, 0);
                p.setTypeface(display); p.setTextSize(24); p.setColor(previewStyle.colors[6]);
                canvas.drawText(t.d("12:34"), 60, 136, p); canvas.restore();
                p.setColor(previewStyle.colors[7]); canvas.drawRoundRect(190, 52, 330, 56, 2, 2, p);
                p.setColor(previewStyle.colors[4]); canvas.drawRoundRect(190, 66, 260, 70, 2, 2, p);
                for (int i = 0; i < 9; i++) {
                    float x = 190 + i % 3 * 87, y = 96 + i / 3 * 50;
                    p.setColor(i == 0 ? previewStyle.colors[4] : previewStyle.colors[2]);
                    canvas.drawRoundRect(x, y, x + 77, y + 40, 5, 5, p);
                    p.setColor(i == 0 ? previewStyle.colors[1] : previewStyle.colors[7]);
                    canvas.drawRoundRect(x + 12, y + 13, x + 26, y + 27, 3, 3, p);
                    canvas.drawRoundRect(x + 34, y + 18, x + 65, y + 21, 1, 1, p);
                }
                canvas.restore();
            }
        };
        home.setContentDescription(t.appearancePreview());
        appearancePreview.addView(home, new FrameLayout.LayoutParams(Ui.ipx(480), Ui.ipx(270), Gravity.START));
        LinearLayout caption = new LinearLayout(this);
        caption.setOrientation(LinearLayout.VERTICAL);
        TextView label = text(ui400, 22, Palette.MIST_400); label.setText(t.appearancePreview()); caption.addView(label);
        appearanceName = text(displayBold, 42, Palette.PLASTER_100);
        caption.addView(appearanceName);
        appearanceSwatches = new LinearLayout(this);
        caption.addView(appearanceSwatches);
        appearancePreview.addView(caption, new FrameLayout.LayoutParams(Ui.ipx(500), -2, Gravity.END | Gravity.CENTER_VERTICAL));
        place(appearancePreview, 640, 112, Tokens.LAYOUT_SETTINGS_ROW_WIDTH, 270);
        updateAppearancePreview();
    }

    private void updateAppearancePreview() {
        String key = themeChoice + ":" + backgroundChoice + ":" + lightChoice;
        if (key.equals(previewKey)) return;
        previewKey = key;
        previewStyle = Appearance.style(this, Appearance.PRESETS[themeChoice], BACKGROUNDS[backgroundChoice], lightChoice);
        View home = appearancePreview.getChildAt(0);
        home.setBackground(Appearance.preview(this, previewStyle));
        home.invalidate();
        appearanceName.setText(t.themeNames()[themeChoice]);
        appearanceSwatches.removeAllViews();
        for (int i : new int[]{0, 2, 4, 6}) {
            View swatch = new View(this); swatch.setBackground(round(previewStyle.colors[i], 24));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.ipx(48), Ui.ipx(48)); lp.setMarginEnd(Ui.ipx(14));
            appearanceSwatches.addView(swatch, lp);
        }
    }

    /** Station's second step: the broadcast stations or the reciters. Playing radio moves to the pick. Back runs back. */
    private void openStations(boolean reciters, Runnable back) {
        List<Integer> found = new ArrayList<>();
        for (int i = 0; i < Radio.STATIONS.length; i++) if (Radio.STATIONS[i].reciter == reciters) found.add(i);
        String[] names = new String[found.size()];
        int current = Radio.stationIndex(this);
        // -1 when the station is in the other group: nothing is marked, focus starts on the first one.
        int sel = -1;
        for (int k = 0; k < names.length; k++) {
            names[k] = t.stationChoice(Radio.STATIONS[found.get(k)]);
            if (found.get(k) == current) sel = k;
        }
        openChooser(t.stationGroups()[reciters ? 1 : 0], names, sel, k -> { Radio.switchTo(this, found.get(k)); showSaved(); });
        chooserBack = back;
    }

    /** A list over the screen (cities, methods): OK picks, Back cancels. Up/Down are the system's own. */
    private void openChooser(String title, String[] items, int selected, IntConsumer pick) {
        openChooser(title, items, selected, pick, null);
    }

    /** onFocus, if given, hears each item as focus reaches it (the adhan list plays that voice). */
    private void openChooser(String title, String[] items, int selected, IntConsumer pick, IntConsumer onFocus) {
        chooser = new FrameLayout(this);
        chooser.setBackgroundColor(Palette.SCRIM_MENU);
        chooser.setClickable(true);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.ipx(48), Ui.ipx(40), Ui.ipx(48), Ui.ipx(32));
        GradientDrawable bg = round(Palette.LAPIS_800, 24);
        bg.setStroke(Ui.ipx(2), Palette.GOLD_400_60);
        card.setBackground(bg);
        card.setClipChildren(true);

        TextView head = text(displayBold, Tokens.FONT_SIZE_DISPLAY_S, Palette.GOLD_400);
        head.setText(title);
        card.addView(head);
        lineHeight(head, 1.2f);

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        int reach = Ui.ipx(FocusTile.REACH);
        scroll.setPadding(reach, reach, reach, reach);
        scroll.setClipToPadding(true);
        scroll.setClipChildren(true);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setClipChildren(false);
        FocusTile first = null;
        for (int i = 0; i < items.length; i++) {
            final int index = i;
            boolean chosen = i == selected;
            FocusTile item = new FocusTile(this, 14);
            item.setFocusScale(1f);   // the list scrolls, so a grown tile would be clipped at its edges
            item.setRest(chosen ? Palette.LAPIS_700 : 0, 0, 0);
            TextView v = text(chosen ? ui500 : ui400, 26, Palette.PLASTER_100);
            v.setText(items[i]);
            v.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            v.setPadding(Ui.ipx(24), 0, Ui.ipx(24), 0);
            item.addView(v, new FrameLayout.LayoutParams(-1, -1));
            item.setLook(f -> {
                v.setTextColor(f ? Palette.LAPIS_900 : (chosen ? Palette.GOLD_400 : Palette.PLASTER_100));
                if (f && onFocus != null) onFocus.accept(index);
            });
            item.setOnClickListener(x -> {
                pick.accept(index);
                closeChooser();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Ui.ipx(68 + 2 * FocusTile.REACH));
            if (i > 0) lp.topMargin = Ui.ipx(6 - 2 * FocusTile.REACH);
            list.addView(item, lp);
            if (chosen || first == null) first = item;
        }
        scroll.addView(list);
        float listH = Math.min(items.length, 8) * 74 + 2 * FocusTile.REACH;
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, Ui.ipx(listH));
        slp.topMargin = Ui.ipx(20 - FocusTile.REACH);
        slp.setMarginStart(-reach);
        slp.setMarginEnd(-reach);
        card.addView(scroll, slp);

        chooser.addView(card, new FrameLayout.LayoutParams(Ui.ipx(900), -2, Gravity.CENTER));
        setRowsFocusable(false);
        root.addView(chooser, new FrameLayout.LayoutParams(-1, -1));
        FocusTile start = first;
        if (start != null) root.post(start::requestFocus);
    }

    private void closeChooser() {
        stopPreview();
        root.removeView(chooser);
        chooser = null;
        chooserBack = null;
        setRowsFocusable(true);
        refresh();
        focusRow(rowIndex);
    }

    private void setRowsFocusable(boolean on) {
        for (FocusTile n : nav) n.setFocusable(on);
        for (Row r : rows) r.tile.setFocusable(on);
    }

    // ---------------------------------------------------------------- small views

    /** The value row's chevron toward the end side (design: M9 5l7 7-7 7); mirrored in Arabic. */
    private static final class Chevron extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        Chevron(Context c, int color) {
            super(c);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(color);
        }

        @Override
        protected void onDraw(Canvas c) {
            float k = getWidth() / 24f;
            p.setStrokeWidth(2.2f * k);
            if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) c.scale(-1, 1, getWidth() / 2f, 0);
            path.reset();
            path.moveTo(9 * k, 5 * k);
            path.lineTo(16 * k, 12 * k);
            path.lineTo(9 * k, 19 * k);
            c.drawPath(path, p);
        }
    }

    /** On: gold track, lapis knob at the end. Off: lapis-700 track, mist knob at the start. On a gold row the on track turns lapis so it still shows. */
    private static final class Switch extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final boolean on, focused;

        Switch(Context c, boolean on, boolean focused) {
            super(c);
            this.on = on;
            this.focused = focused;
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight(), r = h / 2;
            p.setColor(on ? (focused ? Palette.LAPIS_900 : Palette.GOLD_400) : Palette.LAPIS_700);
            c.drawRoundRect(0, 0, w, h, r, r, p);
            float knob = h * 18 / 48, inset = h * 6 / 48;
            boolean atEnd = on;
            boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
            float cx = (atEnd != rtl) ? w - inset - knob : inset + knob;
            p.setColor(on ? (focused ? Palette.GOLD_400 : Palette.LAPIS_900) : Palette.MIST_400);
            c.drawCircle(cx, r, knob, p);
        }
    }
}
