package tv.afnon.installer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.github.muntashirakon.adb.AdbPairingRequiredException;

/**
 * The whole installer, one screen at a time: how to turn on debugging on the TV, finding the TV, connecting (classic
 * "Allow debugging?" or Wireless debugging with a pairing code), then downloading the latest Afnon, installing it and
 * running the setup from tools/install.sh.
 */
public final class MainActivity extends Activity {
    private static final int LAPIS = 0xff0c1b33, LAPIS_CARD = 0xff12264a, GOLD = 0xffd8b46a, PLASTER = 0xffefe7d6,
            DIM = 0xffa69f90, RED = 0xffe08a7a;

    private final ExecutorService work = Executors.newSingleThreadExecutor();
    private Finder finder;
    private Adb adb;
    private LinearLayout page;
    /** The TV list on the find screen, refreshed as the finder hears from TVs. */
    private LinearLayout tvList;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(LAPIS);
        getWindow().setNavigationBarColor(LAPIS);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(LAPIS);
        scroll.setFillViewport(true);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        page.setPadding(pad, dp(28), pad, pad);
        scroll.addView(page);
        setContentView(scroll);
        finder = new Finder(this, tvs -> runOnUiThread(() -> showTvs(tvs)));
        String demo = getIntent().getStringExtra("screen");
        boolean debuggable = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (demo != null && debuggable) showDemo(demo);
        else showGuide();
    }

    /**
     * Debug builds only: one screen with sample data, for screenshots and checking layouts without a TV.
     * {@code adb shell am start -n tv.afnon.installer/.MainActivity --es screen find|pair|install|done|problem}
     */
    private void showDemo(String screen) {
        switch (screen) {
            case "find": {
                showFind();
                finder.stop();
                Finder.Tv wireless = new Finder.Tv("192.168.1.50");
                wireless.tls = 41234;
                wireless.pairing = 37019;
                Finder.Tv classic = new Finder.Tv("192.168.1.62");
                classic.classic = Finder.CLASSIC_PORT;
                if (tvList == null) {
                    tvList = new LinearLayout(this);
                    tvList.setOrientation(LinearLayout.VERTICAL);
                    page.addView(tvList, 2);
                }
                showTvs(java.util.Arrays.asList(wireless, classic));
                break;
            }
            case "pair":
                showPair("192.168.1.50", 41234);
                break;
            case "install": {
                clear(R.string.install_title);
                text(getString(R.string.downloading, "0.5"), PLASTER, 15);
                ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
                bar.setMax(1000);
                bar.setProgress(620);
                page.addView(bar);
                break;
            }
            case "done": {
                LinearLayout steps = new LinearLayout(this);
                steps.setOrientation(LinearLayout.VERTICAL);
                for (Setup.Step st : Setup.STEPS) stepLine(steps, getString(st.label), true, st.required);
                showDone(true, "0.5", steps);
                break;
            }
            case "problem":
                fail(getString(R.string.error_connect, "192.168.1.50"));
                break;
            default:
                showGuide();
        }
    }

    @Override
    protected void onDestroy() {
        finder.stop();
        work.shutdownNow();
        if (adb != null) {
            Adb a = adb;
            new Thread(() -> { try { a.disconnect(); } catch (IOException ignored) { } }).start();
        }
        super.onDestroy();
    }

    // Screens

    private void showGuide() {
        finder.stop();
        clear(R.string.guide_title);
        text(getString(R.string.guide_body), PLASTER, 16);
        button(R.string.find_button, v -> showFind(), true);
    }

    private void showFind() {
        clear(R.string.find_title);
        if (Finder.phoneAddress(this) == null) {
            text(getString(R.string.not_wifi), RED, 15);
            button(R.string.try_again, v -> showFind(), true);
            button(R.string.back, v -> showGuide(), false);
            return;
        }
        text(getString(R.string.finding), DIM, 14);
        ProgressBar looking = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        looking.setIndeterminate(true);
        page.addView(looking);
        tvList = new LinearLayout(this);
        tvList.setOrientation(LinearLayout.VERTICAL);
        page.addView(tvList);
        button(R.string.manual_button, v -> askAddress(), false);
        button(R.string.back, v -> showGuide(), false);
        finder.start();
    }

    private void showTvs(List<Finder.Tv> tvs) {
        if (tvList == null || tvList.getParent() == null) return;
        tvList.removeAllViews();
        for (Finder.Tv t : tvs) {
            if (t.classic == 0 && t.tls == 0 && t.pairing == 0) continue;
            StringBuilder s = new StringBuilder(t.host);
            if (t.tls > 0) s.append('\n').append(getString(R.string.tv_tls, t.tls));
            if (t.classic > 0) s.append('\n').append(getString(R.string.tv_classic, t.classic));
            if (t.pairing > 0) s.append('\n').append(getString(R.string.tv_pairing, t.pairing));
            Button b = button(s.toString(), v -> connect(t.host, t.tls, t.classic), false);
            page.removeView(b);
            tvList.addView(b);
        }
    }

    private void askAddress() {
        EditText field = new EditText(this);
        field.setHint(R.string.manual_hint);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        field.setTextDirection(View.TEXT_DIRECTION_LTR);
        new AlertDialog.Builder(this)
                .setTitle(R.string.manual_title)
                .setMessage(R.string.manual_body)
                .setView(field)
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
                        if (port == Finder.CLASSIC_PORT) connect(host, 0, port);
                        else connect(host, port, 0);
                    } else {
                        scan(s);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** No port given: try 5555 and every port Wireless debugging may use. */
    private void scan(String host) {
        clear(R.string.connecting_title);
        text(getString(R.string.scanning, host), DIM, 15);
        spinner();
        work.execute(() -> {
            try {
                List<Integer> open = Finder.scanPorts(host);
                Adb a = adb();
                for (int port : open) {
                    if (port == Finder.CLASSIC_PORT) continue;
                    // Wireless debugging answers a stranger with "pairing required"; anything else isn't ADB.
                    a.setTimeout(5, TimeUnit.SECONDS);
                    try {
                        if (a.connect(host, port)) {
                            a.setTimeout(90, TimeUnit.SECONDS);
                            runOnUiThread(this::install);
                            return;
                        }
                    } catch (AdbPairingRequiredException e) {
                        a.setTimeout(90, TimeUnit.SECONDS);
                        runOnUiThread(() -> showPair(host, port));
                        return;
                    } catch (Exception notAdb) {
                        // Next port.
                    } finally {
                        a.setTimeout(90, TimeUnit.SECONDS);
                    }
                }
                if (open.contains(Finder.CLASSIC_PORT)) {
                    runOnUiThread(() -> connect(host, 0, Finder.CLASSIC_PORT));
                    return;
                }
                fail(getString(R.string.scan_none, host));
            } catch (Exception e) {
                fail(getString(R.string.error_connect, message(e)));
            }
        });
    }

    /** Wireless debugging first (no prompt on the TV once paired), then classic with "Allow debugging?". */
    private void connect(String host, int tls, int classic) {
        finder.stop();
        clear(R.string.connecting_title);
        TextView status = text(getString(R.string.connecting, host), DIM, 15);
        spinner();
        work.execute(() -> {
            Adb a;
            try {
                a = adb();
                a.disconnect();
            } catch (Exception e) {
                fail(getString(R.string.error_connect, message(e)));
                return;
            }
            if (tls > 0) {
                try {
                    if (a.connect(host, tls)) {
                        runOnUiThread(this::install);
                        return;
                    }
                } catch (AdbPairingRequiredException e) {
                    runOnUiThread(() -> showPair(host, tls));
                    return;
                } catch (Exception e) {
                    if (classic == 0) {
                        fail(getString(R.string.error_connect, message(e)));
                        return;
                    }
                }
            }
            if (classic > 0) {
                runOnUiThread(() -> status.setText(R.string.accept_on_tv));
                try {
                    if (a.connect(host, classic)) {
                        runOnUiThread(this::install);
                        return;
                    }
                    fail(getString(R.string.not_accepted));
                } catch (Exception e) {
                    fail(getString(R.string.error_connect, message(e)));
                }
                return;
            }
            fail(getString(R.string.error_connect, host));
        });
    }

    /** Pairing for Wireless debugging: the TV shows a six-digit code and its own pairing port. */
    private void showPair(String host, int tlsPort) {
        clear(R.string.pair_title);
        text(getString(R.string.pair_body), PLASTER, 15);
        EditText code = field(R.string.pair_code_hint, InputType.TYPE_CLASS_NUMBER);
        EditText port = field(R.string.pair_port_hint, InputType.TYPE_CLASS_NUMBER);
        TextView note = text("", DIM, 13);
        // The pairing port comes and goes with the TV's dialog; listen for it and fill it in.
        finder = new Finder(this, tvs -> runOnUiThread(() -> {
            for (Finder.Tv t : tvs) {
                if (t.host.equals(host) && t.pairing > 0 && port.getText().length() == 0) {
                    port.setText(String.valueOf(t.pairing));
                    note.setText(R.string.pair_port_found);
                }
            }
        }));
        finder.start();
        button(R.string.pair_button, v -> {
            String c = code.getText().toString().trim();
            String p = port.getText().toString().trim();
            if (c.length() != 6 || p.isEmpty()) {
                note.setText(R.string.pair_missing);
                return;
            }
            pair(host, Integer.parseInt(p), c, tlsPort);
        }, true);
        button(R.string.back, v -> {
            finder.stop();
            resetFinder();
            showFind();
        }, false);
    }

    private void pair(String host, int pairPort, String code, int tlsPort) {
        finder.stop();
        resetFinder();
        clear(R.string.pair_title);
        text(getString(R.string.pairing), DIM, 15);
        spinner();
        work.execute(() -> {
            try {
                Adb a = adb();
                a.pair(host, pairPort, code);
                a.disconnect();
                if (a.connect(host, tlsPort)) {
                    runOnUiThread(this::install);
                } else {
                    fail(getString(R.string.error_connect, "?"));
                }
            } catch (Exception e) {
                fail(getString(R.string.pair_failed, message(e)));
            }
        });
    }

    private void install() {
        clear(R.string.install_title);
        TextView status = text(getString(R.string.checking_tv), PLASTER, 15);
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(1000);
        page.addView(bar);
        LinearLayout steps = new LinearLayout(this);
        steps.setOrientation(LinearLayout.VERTICAL);
        page.addView(steps);
        work.execute(() -> {
            try {
                Adb a = adb();
                Adb.Result sdk = a.shell("getprop ro.build.version.sdk");
                int api = 0;
                try { api = Integer.parseInt(sdk.out.trim()); } catch (NumberFormatException ignored) { }
                if (api > 0 && api < 26) {
                    fail(getString(R.string.tv_too_old));
                    return;
                }
                String tag = Setup.latestTag();
                String version = tag == null ? "" : tag.replaceFirst("^v", "");
                runOnUiThread(() -> status.setText(getString(R.string.downloading, version)));
                File apk = Setup.download(getCacheDir(), (done, total) -> runOnUiThread(() -> {
                    if (total > 0) bar.setProgress((int) (1000 * done / total));
                }));
                runOnUiThread(() -> status.setText(R.string.sending));
                String result = a.install(apk, f -> runOnUiThread(() -> bar.setProgress((int) (1000 * f))));
                if (result.contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE")) {
                    // A copy of Afnon signed with another key is on the TV; only removing it lets this one in.
                    runOnUiThread(() -> askReplace(apk, status, bar, steps));
                    return;
                }
                if (!result.startsWith("Success")) {
                    fail(getString(R.string.install_failed, result));
                    return;
                }
                runSteps(a, steps, version);
            } catch (Exception e) {
                fail(getString(R.string.error_install, message(e)));
            }
        });
    }

    private void askReplace(File apk, TextView status, ProgressBar bar, LinearLayout steps) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.old_signature_title)
                .setMessage(R.string.old_signature_body)
                .setPositiveButton(R.string.remove_and_install, (d, w) -> work.execute(() -> {
                    try {
                        Adb a = adb();
                        runOnUiThread(() -> status.setText(R.string.sending));
                        a.shell("pm uninstall " + Setup.PACKAGE);
                        String result = a.install(apk, f -> runOnUiThread(() -> bar.setProgress((int) (1000 * f))));
                        if (!result.startsWith("Success")) {
                            fail(getString(R.string.install_failed, result));
                            return;
                        }
                        runSteps(a, steps, "");
                    } catch (Exception e) {
                        fail(getString(R.string.error_install, message(e)));
                    }
                }))
                .setNegativeButton(R.string.cancel, (d, w) -> showGuide())
                .setCancelable(false)
                .show();
    }

    /** On the worker thread: each setup step with a ✓ or ✗, then the done screen. */
    private void runSteps(Adb a, LinearLayout steps, String version) throws IOException, InterruptedException {
        boolean allRequired = true;
        for (Setup.Step s : Setup.STEPS) {
            Adb.Result r = a.shell(s.command);
            boolean ok = r.ok();
            if (!ok && s.required) allRequired = false;
            String label = getString(s.label);
            runOnUiThread(() -> stepLine(steps, label, ok, s.required));
        }
        boolean done = allRequired;
        runOnUiThread(() -> showDone(done, version, steps));
    }

    private void showDone(boolean ok, String version, LinearLayout steps) {
        page.removeView(steps);
        clear(ok ? R.string.done_title : R.string.done_partly_title);
        page.addView(steps);
        text(version.isEmpty() ? getString(R.string.done_body_plain)
                : getString(R.string.done_body, version), PLASTER, 15);
        button(R.string.again, v -> showGuide(), false);
    }

    // Helpers

    private void stepLine(LinearLayout steps, String label, boolean ok, boolean required) {
        TextView t = text((ok ? "✓  " : "✗  ") + label, ok ? GOLD : (required ? RED : DIM), 15);
        page.removeView(t);
        steps.addView(t);
    }

    private synchronized Adb adb() throws Exception {
        if (adb == null) adb = new Adb(this);
        return adb;
    }

    /** A fresh finder for the find screen after the pair screen swapped in its own. */
    private void resetFinder() {
        finder = new Finder(this, tvs -> runOnUiThread(() -> showTvs(tvs)));
    }

    private void fail(String message) {
        runOnUiThread(() -> {
            finder.stop();
            resetFinder();
            clear(R.string.problem_title);
            text(message, RED, 15);
            button(R.string.try_again, v -> showFind(), true);
            button(R.string.back, v -> showGuide(), false);
        });
    }

    private static String message(Exception e) {
        String m = e.getMessage();
        return m == null || m.isEmpty() ? e.getClass().getSimpleName() : m;
    }

    private void clear(int title) {
        page.removeAllViews();
        tvList = null;
        TextView t = text(getString(title), GOLD, 26);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        ((LinearLayout.LayoutParams) t.getLayoutParams()).bottomMargin = dp(16);
    }

    private TextView text(String s, int color, int sp) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setLineSpacing(0, 1.25f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        page.addView(t, lp);
        return t;
    }

    private EditText field(int hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(type);
        e.setTextColor(PLASTER);
        e.setHintTextColor(DIM);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        e.setTextDirection(View.TEXT_DIRECTION_LTR);
        e.setGravity(Gravity.CENTER);
        page.addView(e, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return e;
    }

    private void spinner() {
        ProgressBar p = new ProgressBar(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.topMargin = dp(16);
        page.addView(p, lp);
    }

    private Button button(int label, View.OnClickListener click, boolean primary) {
        return button(getString(label), click, primary);
    }

    private Button button(String label, View.OnClickListener click, boolean primary) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        b.setTextColor(primary ? LAPIS : PLASTER);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(14));
        bg.setColor(primary ? GOLD : LAPIS_CARD);
        if (!primary) bg.setStroke(dp(1), 0x40efe7d6);
        b.setBackground(bg);
        b.setOnClickListener(click);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        page.addView(b, lp);
        return b;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
