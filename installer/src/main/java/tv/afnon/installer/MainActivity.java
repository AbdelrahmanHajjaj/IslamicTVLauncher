package tv.afnon.installer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.github.muntashirakon.adb.AdbPairingRequiredException;

/**
 * The installer, one screen at a time in three stages: Prepare (turning on debugging on the TV), Connect (finding
 * the TV, then "Allow debugging?" or a pairing code) and Install (download, install, the setup from
 * tools/install.sh). Every screen is the same frame: stage bar, arch, title, lead, content, and the actions pinned
 * to the bottom.
 */
public final class MainActivity extends Activity {
    private static final int PREPARE = 0, CONNECT = 1, INSTALL = 2;

    private final ExecutorService work = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private Finder finder, pairFinder;
    private Adb adb;

    private LinearLayout stages, content, footer;
    private ScrollView scroll;
    private Hero hero;
    private int stage;
    /** What Back does on this screen; null while it must not be interrupted. */
    private Runnable back;
    /**
     * Bumped whenever the user moves to another screen, so a worker that finishes afterwards (a connect that
     * succeeds after the user went Back) leaves the screen alone.
     */
    private int gen;
    /** The TV list on the find screen, refreshed as the finder hears from TVs. */
    private LinearLayout tvList;
    private View help;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.init(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Ui.LAPIS_900);
        getWindow().setNavigationBarColor(Ui.LAPIS_900);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(Ui.LAPIS_900);
        stages = Ui.row(this);
        stages.setPadding(dp(20), dp(18), dp(20), dp(6));
        root.addView(stages, Ui.wide(this, 0));
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        content = Ui.column(this);
        content.setPadding(dp(20), dp(4), dp(20), dp(24));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        footer = Ui.column(this);
        footer.setPadding(dp(20), dp(8), dp(20), dp(20));
        root.addView(footer, Ui.wide(this, 0));
        setContentView(root);

        hero = new Hero(this);
        finder = new Finder(this, tvs -> runOnUiThread(() -> showTvs(tvs)));

        String demo = getIntent().getStringExtra("screen");
        boolean debuggable = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (demo != null && debuggable) showDemo(demo);
        else showGuide();
    }

    @Override
    public void onBackPressed() {
        if (back != null) back.run();
    }

    @Override
    protected void onDestroy() {
        stopFinders();
        work.shutdownNow();
        if (adb != null) {
            Adb a = adb;
            new Thread(() -> { try { a.disconnect(); } catch (IOException ignored) { } }).start();
        }
        super.onDestroy();
    }

    // Prepare

    private void showGuide() {
        stopFinders();
        frame(PREPARE, Hero.Mode.TV, getString(R.string.guide_title), getString(R.string.guide_lead));
        add(step(1, R.string.g1_title, R.string.g1_path, 0), 20);
        add(step(2, R.string.g2_title, R.string.g2_path, R.string.g2_body), 10);
        add(step(3, R.string.g3_title, R.string.g3_path, R.string.g3_body), 10);
        add(step(4, R.string.g4_title, 0, R.string.g4_body), 10);
        foot(Ui.primary(this, getString(R.string.guide_ready), v -> go(this::showFind)));
        back = this::finish;
    }

    /** A numbered card: title, the TV's menu path as a chip, and a line of detail. */
    private View step(int n, int title, int path, int body) {
        LinearLayout card = Ui.card(this);
        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.TOP);
        row.addView(Ui.badge(this, number(n)));
        LinearLayout words = Ui.column(this);
        words.setPaddingRelative(dp(14), dp(3), 0, 0);
        words.addView(Ui.label(this, getString(title)));
        if (path != 0) {
            TextView p = Ui.path(this, getString(path));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(8);
            words.addView(p, lp);
        }
        if (body != 0) words.addView(Ui.small(this, getString(body)), Ui.wide(this, 8));
        row.addView(words, Ui.fill(this));
        card.addView(row);
        return card;
    }

    // Connect: finding the TV

    private void showFind() {
        showFind(true);
    }

    private void showFind(boolean search) {
        stopFinders();
        back = () -> go(this::showGuide);
        if (Finder.phoneAddress(this) == null) {
            frame(CONNECT, Hero.Mode.PROBLEM, getString(R.string.not_wifi_title), getString(R.string.not_wifi));
            foot(Ui.primary(this, getString(R.string.try_again), v -> go(this::showFind)));
            foot(Ui.secondary(this, getString(R.string.back), v -> go(this::showGuide)));
            return;
        }
        frame(CONNECT, Hero.Mode.SEARCH, getString(R.string.find_title), getString(R.string.find_lead));
        tvList = Ui.column(this);
        add(tvList, 12);
        LinearLayout h = Ui.card(this);
        h.addView(Ui.label(this, getString(R.string.help_title)));
        h.addView(Ui.small(this, getString(R.string.help_body)), Ui.wide(this, 8));
        h.setVisibility(View.GONE);
        help = h;
        add(h, 12);
        int g = gen;
        // Help only once a search has had time to turn something up.
        main.postDelayed(() -> {
            if (g == gen && help != null && tvList != null && tvList.getChildCount() == 0) {
                help.setAlpha(0f);
                help.setVisibility(View.VISIBLE);
                help.animate().alpha(1f).setDuration(300).start();
            }
        }, 8000);
        foot(Ui.secondary(this, getString(R.string.manual_button), v -> askAddress()));
        if (search) finder.start();
    }

    private void showTvs(List<Finder.Tv> tvs) {
        if (tvList == null || tvList.getParent() == null) return;
        tvList.removeAllViews();
        for (Finder.Tv t : tvs) {
            if (t.classic == 0 && t.tls == 0 && t.pairing == 0) continue;
            tvList.addView(tvCard(t), Ui.wide(this, tvList.getChildCount() == 0 ? 8 : 10));
        }
        if (help != null && tvList.getChildCount() > 0) help.setVisibility(View.GONE);
    }

    private View tvCard(Finder.Tv t) {
        LinearLayout card = Ui.row(this);
        GradientDrawable bg = Ui.round(Ui.LAPIS_800, 18, this);
        bg.setStroke(dp(1), 0x66d8b46a);
        Ui.pressable(card, bg, 0x33d8b46a);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.addView(new TvGlyph(this), new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout words = Ui.column(this);
        words.setPaddingRelative(dp(14), 0, dp(8), 0);
        words.addView(Ui.small(this, getString(R.string.tv_found)));
        TextView host = Ui.latin(Ui.text(this, t.host, Ui.PLASTER, 18, Ui.semibold()));
        host.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        words.addView(host, Ui.wide(this, 2));
        if (t.tls > 0) words.addView(door(R.string.badge_wireless, t.tls, true), Ui.wide(this, 6));
        if (t.classic > 0) words.addView(door(R.string.badge_network, t.classic, false), Ui.wide(this, 4));
        if (t.pairing > 0) words.addView(door(R.string.badge_pair, t.pairing, true), Ui.wide(this, 4));
        card.addView(words, Ui.fill(this));

        TextView chevron = Ui.text(this, "›", Ui.GOLD_400, 26, Ui.medium());
        card.addView(chevron);
        card.setOnClickListener(v -> go(() -> connect(t.host, t.tls, t.classic)));
        return card;
    }

    /** "● Wireless debugging · 41234": a way into the TV and its port. */
    private View door(int name, int port, boolean accent) {
        LinearLayout row = Ui.row(this);
        View dot = new View(this);
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(accent ? Ui.GOLD_400 : Ui.MIST);
        dot.setBackground(d);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(6), dp(6));
        lp.setMarginEnd(dp(8));
        row.addView(dot, lp);
        row.addView(Ui.text(this, getString(name), accent ? Ui.GOLD_200 : Ui.MIST, 13, Ui.medium()));
        TextView p = Ui.latin(Ui.text(this, "·  " + number(port), Ui.MIST, 13, Ui.medium()));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        pp.setMarginStart(dp(8));
        row.addView(p, pp);
        return row;
    }

    private void askAddress() {
        EditText field = new EditText(this);
        field.setHint(R.string.manual_hint);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        Ui.latin(field);
        LinearLayout box = Ui.column(this);
        box.setPadding(dp(22), dp(4), dp(22), 0);
        box.addView(field);
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(R.string.manual_title)
                .setMessage(R.string.manual_body)
                .setView(box)
                .setPositiveButton(R.string.connect, (d, w) -> {
                    String s = field.getText().toString().trim();
                    if (s.isEmpty()) return;
                    int colon = s.lastIndexOf(':');
                    if (colon > 0) {
                        int port;
                        try {
                            port = Integer.parseInt(s.substring(colon + 1));
                        } catch (NumberFormatException e) {
                            return;
                        }
                        String host = s.substring(0, colon);
                        if (port == Finder.CLASSIC_PORT) go(() -> connect(host, 0, port));
                        else go(() -> connect(host, port, 0));
                    } else {
                        go(() -> scan(s));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** No port given: try 5555 and every port Wireless debugging may use. */
    private void scan(String host) {
        stopFinders();
        frame(CONNECT, Hero.Mode.SEARCH, getString(R.string.scanning_title), getString(R.string.scanning, host));
        foot(Ui.secondary(this, getString(R.string.back), v -> go(this::showFind)));
        back = () -> go(this::showFind);
        int g = gen;
        work.execute(() -> {
            try {
                List<Integer> open = Finder.scanPorts(host);
                if (g != gen) return;
                Adb a = adb();
                for (int port : open) {
                    if (port == Finder.CLASSIC_PORT) continue;
                    // Wireless debugging answers a stranger with "pairing required"; anything else isn't ADB.
                    a.setTimeout(5, TimeUnit.SECONDS);
                    try {
                        a.disconnect();
                        if (a.connect(host, port)) {
                            post(g, this::install);
                            return;
                        }
                    } catch (AdbPairingRequiredException e) {
                        post(g, () -> showPair(host, port));
                        return;
                    } catch (Exception notAdb) {
                        // Next port.
                    } finally {
                        a.setTimeout(90, TimeUnit.SECONDS);
                    }
                }
                if (open.contains(Finder.CLASSIC_PORT)) {
                    post(g, () -> connect(host, 0, Finder.CLASSIC_PORT));
                    return;
                }
                fail(g, getString(R.string.scan_none, host));
            } catch (Exception e) {
                fail(g, getString(R.string.error_connect, message(e)));
            }
        });
    }

    /** Wireless debugging first (no prompt on the TV once paired), then classic with "Allow debugging?". */
    private void connect(String host, int tls, int classic) {
        stopFinders();
        frame(CONNECT, Hero.Mode.SEARCH, getString(R.string.connecting_title), getString(R.string.connecting, host));
        foot(Ui.secondary(this, getString(R.string.back), v -> go(this::showFind)));
        back = () -> go(this::showFind);
        int g = gen;
        work.execute(() -> {
            Adb a;
            try {
                a = adb();
                a.disconnect();
            } catch (Exception e) {
                fail(g, getString(R.string.error_connect, message(e)));
                return;
            }
            if (tls > 0) {
                try {
                    if (a.connect(host, tls)) {
                        post(g, this::install);
                        return;
                    }
                } catch (AdbPairingRequiredException e) {
                    post(g, () -> showPair(host, tls));
                    return;
                } catch (Exception e) {
                    if (classic == 0) {
                        fail(g, getString(R.string.error_connect, message(e)));
                        return;
                    }
                }
            }
            if (classic > 0) {
                post(g, this::showAllow);
                try {
                    if (a.connect(host, classic)) {
                        post(g, this::install);
                        return;
                    }
                    fail(g, getString(R.string.not_accepted));
                } catch (Exception e) {
                    fail(g, getString(R.string.error_connect, message(e)));
                }
                return;
            }
            fail(g, getString(R.string.error_connect, host));
        });
    }

    /** Classic network debugging: the TV asks to allow this phone. */
    private void showAllow() {
        frame(CONNECT, Hero.Mode.ALLOW, getString(R.string.allow_title), getString(R.string.allow_lead));
        LinearLayout card = Ui.card(this);
        card.addView(numbered(1, getString(R.string.allow_1), null));
        card.addView(numbered(2, getString(R.string.allow_2), null), Ui.wide(this, 14));
        add(card, 20);
        add(waiting(getString(R.string.waiting)), 18);
        foot(Ui.secondary(this, getString(R.string.back), v -> go(this::showFind)));
    }

    // Connect: pairing

    /** Pairing for Wireless debugging: the TV shows a six-digit code and its own pairing port. */
    private void showPair(String host, int tlsPort) {
        stopFinders();
        frame(CONNECT, Hero.Mode.PAIR, getString(R.string.pair_title), getString(R.string.pair_lead));
        LinearLayout card = Ui.card(this);
        card.addView(numbered(1, getString(R.string.pair_1), getString(R.string.pair_1_path)));
        card.addView(numbered(2, getString(R.string.pair_2), null), Ui.wide(this, 14));
        card.addView(numbered(3, getString(R.string.pair_3), null), Ui.wide(this, 14));
        add(card, 20);

        TextView pairButton = Ui.primary(this, getString(R.string.pair_button), null);
        EditText port = new EditText(this);
        CodeInput code = new CodeInput(this, c -> {
            Ui.enabled(pairButton, c.length() == 6 && port.length() > 0);
            if (c.length() == 6 && port.length() > 0) pairButton.performClick();
        });
        add(code, 20);

        // The pairing port: found by mDNS while the TV's dialog is open, or typed.
        TextView found = Ui.latin(Ui.chip(this, "", true));
        found.setVisibility(View.GONE);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        fp.gravity = Gravity.CENTER_HORIZONTAL;
        fp.topMargin = dp(16);
        content.addView(found, fp);
        View wait = waiting(getString(R.string.pair_port_waiting));
        add(wait, 14);
        port.setHint(R.string.pair_port_hint);
        port.setInputType(InputType.TYPE_CLASS_NUMBER);
        port.setTextColor(Ui.PLASTER);
        port.setHintTextColor(Ui.MIST);
        port.setTextSize(15);
        port.setTypeface(Ui.medium());
        port.setGravity(Gravity.CENTER);
        port.setBackground(Ui.round(Ui.LAPIS_800, 12, this));
        port.setPadding(dp(12), dp(12), dp(12), dp(12));
        Ui.latin(port);
        add(port, 10);
        port.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void afterTextChanged(Editable e) {
                Ui.enabled(pairButton, code.code().length() == 6 && e.length() > 0);
            }
        });
        pairFinder = new Finder(this, tvs -> runOnUiThread(() -> {
            for (Finder.Tv t : tvs) {
                if (t.host.equals(host) && t.pairing > 0) {
                    port.setText(number(t.pairing));
                    port.setVisibility(View.GONE);
                    wait.setVisibility(View.GONE);
                    found.setText("✓  " + getString(R.string.pair_port_found, number(t.pairing)));
                    found.setVisibility(View.VISIBLE);
                }
            }
        }));
        pairFinder.start();

        Ui.enabled(pairButton, false);
        pairButton.setOnClickListener(v -> {
            String c = code.code();
            String p = port.getText().toString().trim();
            if (c.length() != 6 || p.isEmpty()) return;
            int pp = Integer.parseInt(p);
            go(() -> pair(host, pp, c, tlsPort));
        });
        foot(pairButton);
        foot(Ui.secondary(this, getString(R.string.back), v -> go(this::showFind)));
        back = () -> go(this::showFind);
        code.post(code::focus);
    }

    private void pair(String host, int pairPort, String code, int tlsPort) {
        stopFinders();
        frame(CONNECT, Hero.Mode.PAIR, getString(R.string.pair_title), getString(R.string.pairing));
        back = null;
        int g = gen;
        work.execute(() -> {
            try {
                Adb a = adb();
                a.pair(host, pairPort, code);
                a.disconnect();
                if (a.connect(host, tlsPort)) {
                    post(g, this::install);
                } else {
                    fail(g, getString(R.string.error_connect, host));
                }
            } catch (Exception e) {
                fail(g, getString(R.string.pair_failed, message(e)));
            }
        });
    }

    // Install

    private Timeline timeline;
    private TextView titleView, leadView;

    private void install() {
        stopFinders();
        frame(INSTALL, Hero.Mode.PROGRESS, getString(R.string.install_title), getString(R.string.install_lead));
        back = null;
        timeline = timeline();
        add(timeline, 20);
        int g = gen;
        work.execute(() -> {
            try {
                Adb a = adb();
                Adb.Result sdk = a.shell("getprop ro.build.version.sdk");
                int api = 0;
                try { api = Integer.parseInt(sdk.out.trim()); } catch (NumberFormatException ignored) { }
                if (api > 0 && api < 26) {
                    fail(g, getString(R.string.tv_too_old));
                    return;
                }
                String tag = Setup.latestTag();
                String version = tag == null ? "" : tag.replaceFirst("^v", "");
                post(g, () -> {
                    if (!version.isEmpty()) leadView.setText(getString(R.string.install_lead_version, version));
                    timeline.set(0, Timeline.State.WORKING);
                });
                File apk = Setup.download(getCacheDir(), (done, total) -> post(g, () -> {
                    if (total <= 0) return;
                    hero.progress(0.45f * done / total);
                    timeline.note(0, getString(R.string.mb_of, mb(done), mb(total)));
                }));
                send(g, a, apk, version, false);
            } catch (Exception e) {
                fail(g, getString(R.string.error_install, message(e)));
            }
        });
    }

    private Timeline timeline() {
        List<String> steps = new ArrayList<>();
        steps.add(getString(R.string.t_download));
        steps.add(getString(R.string.t_send));
        for (Setup.Step s : Setup.STEPS) steps.add(getString(s.label));
        return new Timeline(this, steps.toArray(new String[0]));
    }

    /** On the worker thread: the APK into the TV, then the setup steps, then the done screen. */
    private void send(int g, Adb a, File apk, String version, boolean replacing)
            throws IOException, InterruptedException {
        post(g, () -> {
            timeline.set(0, Timeline.State.DONE);
            timeline.set(1, Timeline.State.WORKING);
        });
        if (replacing) a.shell("pm uninstall " + Setup.PACKAGE);
        long size = apk.length();
        String result = a.install(apk, f -> post(g, () -> {
            hero.progress(0.45f + 0.45f * f);
            timeline.note(1, getString(R.string.mb_of, mb((long) (f * size)), mb(size)));
        }));
        if (result.contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE") && !replacing) {
            // A copy of Afnon signed with another key is on the TV; only removing it lets this one in.
            post(g, () -> askReplace(g, a, apk, version));
            return;
        }
        if (!result.startsWith("Success")) {
            fail(g, getString(R.string.install_failed, result));
            return;
        }
        post(g, () -> timeline.set(1, Timeline.State.DONE));
        boolean allRequired = true;
        for (int i = 0; i < Setup.STEPS.length; i++) {
            Setup.Step s = Setup.STEPS[i];
            int row = i + 2;
            float at = 0.9f + 0.1f * (i + 1) / Setup.STEPS.length;
            post(g, () -> timeline.set(row, Timeline.State.WORKING));
            boolean ok = a.shell(s.command).ok();
            if (!ok && s.required) allRequired = false;
            post(g, () -> {
                timeline.set(row, ok ? Timeline.State.DONE
                        : s.required ? Timeline.State.FAILED : Timeline.State.SKIPPED);
                hero.progress(at);
            });
        }
        boolean done = allRequired;
        post(g, () -> showDone(done, version));
    }

    private void askReplace(int g, Adb a, File apk, String version) {
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(R.string.old_signature_title)
                .setMessage(R.string.old_signature_body)
                .setPositiveButton(R.string.remove_and_install, (d, w) -> work.execute(() -> {
                    try {
                        send(g, a, apk, version, true);
                    } catch (Exception e) {
                        fail(g, getString(R.string.error_install, message(e)));
                    }
                }))
                .setNegativeButton(R.string.cancel, (d, w) -> go(this::showGuide))
                .setCancelable(false)
                .show();
    }

    private void showDone(boolean ok, String version) {
        Timeline steps = timeline;
        if (steps.getParent() != null) ((ViewGroup) steps.getParent()).removeView(steps);
        String lead = !ok ? getString(R.string.done_partly_lead)
                : version.isEmpty() ? getString(R.string.done_lead_plain) : getString(R.string.done_lead, version);
        frame(INSTALL, ok ? Hero.Mode.DONE : Hero.Mode.PROBLEM,
                getString(ok ? R.string.done_title : R.string.done_partly_title), lead);
        if (!ok) add(steps, 20);
        LinearLayout tip = Ui.card(this);
        tip.addView(Ui.label(this, getString(R.string.tip_title)));
        tip.addView(Ui.small(this, getString(R.string.tip_body)), Ui.wide(this, 8));
        add(tip, ok ? 20 : 12);
        foot(Ui.primary(this, getString(R.string.finish), v -> finish()));
        foot(Ui.secondary(this, getString(R.string.another), v -> go(this::showGuide)));
        back = this::finish;
    }

    // Problems

    private void fail(int g, String message) {
        post(g, () -> {
            stopFinders();
            frame(stage, Hero.Mode.PROBLEM, getString(R.string.problem_title), message);
            LinearLayout card = Ui.card(this);
            card.addView(Ui.label(this, getString(R.string.check_title)));
            card.addView(Ui.small(this, getString(R.string.check_body)), Ui.wide(this, 8));
            add(card, 20);
            foot(Ui.primary(this, getString(R.string.try_again), v -> go(this::showFind)));
            foot(Ui.secondary(this, getString(R.string.back), v -> go(this::showGuide)));
            back = () -> go(this::showGuide);
        });
    }

    private static String message(Exception e) {
        String m = e.getMessage();
        return m == null || m.isEmpty() ? e.getClass().getSimpleName() : m;
    }

    // Debug builds only: one screen with sample data, for screenshots and checking layouts without a TV.
    // adb shell am start -n tv.afnon.installer/.MainActivity --es screen find|allow|pair|install|done|partly|problem

    private void showDemo(String screen) {
        switch (screen) {
            case "find": {
                showFind(false);
                Finder.Tv wireless = new Finder.Tv("192.168.1.50");
                wireless.tls = 41234;
                wireless.pairing = 37019;
                Finder.Tv classic = new Finder.Tv("192.168.1.62");
                classic.classic = Finder.CLASSIC_PORT;
                if (tvList == null) {
                    tvList = Ui.column(this);
                    content.addView(tvList);
                }
                showTvs(Arrays.asList(wireless, classic));
                break;
            }
            case "allow":
                showAllow();
                back = () -> go(this::showFind);
                break;
            case "pair":
                showPair("192.168.1.50", 41234);
                break;
            case "install": {
                frame(INSTALL, Hero.Mode.PROGRESS, getString(R.string.install_title),
                        getString(R.string.install_lead_version, "0.5"));
                timeline = timeline();
                add(timeline, 20);
                timeline.set(0, Timeline.State.DONE);
                timeline.set(1, Timeline.State.WORKING);
                timeline.note(1, getString(R.string.mb_of, "12.4", "19.7"));
                hero.progress(0.62f);
                break;
            }
            case "done":
            case "partly": {
                boolean ok = screen.equals("done");
                timeline = timeline();
                for (int i = 0; i < Setup.STEPS.length + 2; i++) timeline.set(i, Timeline.State.DONE);
                if (!ok) timeline.set(3, Timeline.State.FAILED);
                showDone(ok, "0.5");
                break;
            }
            case "problem":
                fail(gen, getString(R.string.error_connect, "192.168.1.50"));
                break;
            default:
                showGuide();
        }
    }

    // The frame

    /** Moves to another screen on the user's word: workers still running for the old one are ignored. */
    private void go(Runnable screen) {
        gen++;
        screen.run();
    }

    /** Runs on the main thread if the user is still on the screen that started the work. */
    private void post(int g, Runnable r) {
        runOnUiThread(() -> {
            if (g == gen) r.run();
        });
    }

    private void frame(int stageNow, Hero.Mode mode, String title, String lead) {
        stage = stageNow;
        tvList = null;
        help = null;
        content.removeAllViews();
        footer.removeAllViews();
        footer.setVisibility(View.GONE);
        stages(stageNow);
        hero.set(mode);
        content.addView(hero, Ui.wide(this, 8));
        titleView = Ui.title(this, title);
        content.addView(titleView, Ui.wide(this, 18));
        leadView = Ui.lead(this, lead);
        content.addView(leadView, Ui.wide(this, 10));
        scroll.scrollTo(0, 0);
        content.setAlpha(0f);
        content.setTranslationY(dp(8));
        content.animate().alpha(1f).translationY(0).setDuration(220).start();
    }

    private void add(View v, int topDp) {
        content.addView(v, Ui.wide(this, topDp));
    }

    private void foot(View v) {
        footer.setVisibility(View.VISIBLE);
        footer.addView(v, Ui.wide(this, footer.getChildCount() == 0 ? 0 : 10));
    }

    /** Prepare · Connect · Install: done in plaster, now in gold, to come in lapis. */
    private void stages(int now) {
        stages.removeAllViews();
        int[] names = {R.string.stage_prepare, R.string.stage_connect, R.string.stage_install};
        for (int i = 0; i < names.length; i++) {
            LinearLayout s = Ui.column(this);
            View bar = new View(this);
            bar.setBackground(Ui.round(i < now ? Ui.PLASTER : i == now ? Ui.GOLD_400 : Ui.LAPIS_700, 2, this));
            s.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(4)));
            TextView t = Ui.text(this, number(i + 1) + "  " + getString(names[i]),
                    i < now ? Ui.PLASTER : i == now ? Ui.GOLD_400 : Ui.MIST, 12, i == now ? Ui.semibold() : Ui.medium());
            s.addView(t, Ui.wide(this, 8));
            LinearLayout.LayoutParams lp = Ui.fill(this);
            if (i > 0) lp.setMarginStart(dp(8));
            stages.addView(s, lp);
        }
    }

    private View numbered(int n, String text, String path) {
        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.TOP);
        TextView b = Ui.badge(this, number(n));
        row.addView(b);
        LinearLayout words = Ui.column(this);
        words.setPaddingRelative(dp(12), dp(4), 0, 0);
        words.addView(Ui.text(this, text, Ui.PLASTER, 15, Ui.medium()));
        if (path != null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(8);
            words.addView(Ui.path(this, path), lp);
        }
        row.addView(words, Ui.fill(this));
        return row;
    }

    /** A small spinning mark and a line of status, centred. */
    private View waiting(String text) {
        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(18), dp(18));
        lp.setMarginEnd(dp(10));
        row.addView(Timeline.spinner(this), lp);
        row.addView(Ui.small(this, text));
        return row;
    }

    private void stopFinders() {
        finder.stop();
        if (pairFinder != null) pairFinder.stop();
        pairFinder = null;
    }

    private synchronized Adb adb() throws Exception {
        if (adb == null) adb = new Adb(this);
        return adb;
    }

    /** Latin digits whatever the language, to match what the TV shows. */
    private static String number(long n) {
        return String.format(Locale.US, "%d", n);
    }

    private static String mb(long bytes) {
        return String.format(Locale.US, "%.1f", bytes / 1_048_576f);
    }

    private int dp(float v) {
        return Ui.dp(this, v);
    }

    /** A TV outline with Afnon's small arch on it, for the found-TV cards. */
    private static final class TvGlyph extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF r = new RectF();

        TvGlyph(Context c) {
            super(c);
            setBackground(Ui.round(Ui.LAPIS_700, 12, c));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            p.setStrokeWidth(w * 0.05f);
            p.setColor(Ui.PLASTER);
            r.set(w * 0.2f, h * 0.24f, w * 0.8f, h * 0.66f);
            c.drawRoundRect(r, w * 0.05f, w * 0.05f, p);
            c.drawLine(w * 0.38f, h * 0.78f, w * 0.62f, h * 0.78f, p);
            p.setColor(Ui.GOLD_400);
            android.graphics.Path a = new android.graphics.Path();
            a.moveTo(w * 0.44f, h * 0.58f);
            a.lineTo(w * 0.44f, h * 0.42f);
            a.quadTo(w * 0.44f, h * 0.33f, w * 0.5f, h * 0.31f);
            a.quadTo(w * 0.56f, h * 0.33f, w * 0.56f, h * 0.42f);
            a.lineTo(w * 0.56f, h * 0.58f);
            c.drawPath(a, p);
        }
    }
}
