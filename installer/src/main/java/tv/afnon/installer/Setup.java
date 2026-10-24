package tv.afnon.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Downloading the latest Afnon release and what {@code tools/install.sh} does after {@code adb install}. Keep the
 * two in step: a new grant or setting there belongs in {@link #STEPS} here too.
 */
final class Setup {
    private Setup() {}

    static final String PACKAGE = "tv.afnon.home";
    private static final String REPO = "https://github.com/egyjs/afnon-android-tv-launcher";
    private static final String LATEST = REPO + "/releases/latest";
    private static final String APK = LATEST + "/download/afnon.apk";
    private static final String A11Y = PACKAGE + "/.RemoteKeys";

    /** One setup command, with whether its failure is worth stopping for. */
    static final class Step {
        final int label;
        final String command;
        final boolean required;
        Step(int label, String command, boolean required) {
            this.label = label;
            this.command = command;
            this.required = required;
        }
    }

    static final Step[] STEPS = {
            new Step(R.string.step_home, "cmd package set-home-activity " + PACKAGE + "/.MainActivity", true),
            // Added to the TV's other accessibility services rather than replacing them.
            new Step(R.string.step_buttons,
                    "s=$(settings get secure enabled_accessibility_services); "
                            + "case \"$s\" in *" + A11Y + "*) ;; ''|null) s=" + A11Y + " ;; *) s=\"$s:" + A11Y + "\" ;; esac; "
                            + "settings put secure enabled_accessibility_services \"$s\" && "
                            + "settings put secure accessibility_enabled 1", true),
            new Step(R.string.step_screensaver,
                    "settings put secure screensaver_components " + PACKAGE + "/.AfnonDream", false),
            // TCL blocks auto-start for sideloaded apps; other brands have no such appop.
            new Step(R.string.step_autostart,
                    "appops set " + PACKAGE + " APP_AUTO_START allow 2>/dev/null; "
                            + "appops set " + PACKAGE + " APP_ASSOC_START allow 2>/dev/null; true", false),
            new Step(R.string.step_radio,
                    "pm grant " + PACKAGE + " android.permission.RECORD_AUDIO && "
                            + "pm grant " + PACKAGE + " android.permission.WRITE_SECURE_SETTINGS", false),
            new Step(R.string.step_wifi, "pm grant " + PACKAGE + " android.permission.ACCESS_FINE_LOCATION", false),
            new Step(R.string.step_open,
                    "am start -a android.intent.action.MAIN -c android.intent.category.HOME", false),
    };

    interface Progress { void on(long done, long total); }

    /** The latest release's tag ("v0.5"), from where releases/latest redirects; null if GitHub can't be reached. */
    static String latestTag() {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(LATEST).openConnection();
            c.setInstanceFollowRedirects(false);
            c.setRequestMethod("HEAD");
            c.setConnectTimeout(15_000);
            c.setReadTimeout(15_000);
            String to = c.getHeaderField("Location");
            c.disconnect();
            if (to == null || !to.contains("/tag/")) return null;
            return to.substring(to.lastIndexOf('/') + 1);
        } catch (IOException e) {
            return null;
        }
    }

    /** Downloads the latest afnon.apk into {@code dir} and checks it is a whole APK (a zip). */
    static File download(File dir, Progress progress) throws IOException {
        File part = new File(dir, "afnon.apk.part");
        File apk = new File(dir, "afnon.apk");
        HttpURLConnection c = (HttpURLConnection) new URL(APK).openConnection();
        c.setConnectTimeout(20_000);
        c.setReadTimeout(30_000);
        try {
            if (c.getResponseCode() != 200) throw new IOException("GitHub answered " + c.getResponseCode());
            long total = c.getContentLengthLong();
            long done = 0;
            try (InputStream in = c.getInputStream(); OutputStream out = new FileOutputStream(part)) {
                byte[] buf = new byte[64 * 1024];
                int n;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    done += n;
                    progress.on(done, total);
                }
            }
            if (total > 0 && done != total) throw new IOException("Download cut short");
        } finally {
            c.disconnect();
        }
        try (RandomAccessFile f = new RandomAccessFile(part, "r")) {
            if (f.length() < 1024 || f.readInt() != 0x504b0304) throw new IOException("Not an APK");
        }
        if (apk.exists() && !apk.delete()) throw new IOException("Can't replace the old download");
        if (!part.renameTo(apk)) throw new IOException("Can't save the download");
        return apk;
    }
}
