package tv.afnon.home;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inspector.WindowInspector;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** API 29+ owned-emulator checks of actual windows; no player, stream or remote-key assignment. */
@android.annotation.TargetApi(29)
final class UiScaleWindowDeviceCheck {
    static void run(Instrumentation test) {
        SharedPreferences prefs = Prefs.of(test.getTargetContext());
        Map<String, ?> original = prefs.getAll();
        String services = shell(test, "settings get secure enabled_accessibility_services");
        String enabled = shell(test, "settings get secure accessibility_enabled");
        String dreams = shell(test, "settings get secure screensaver_components");
        String dreamEnabled = shell(test, "settings get secure screensaver_enabled");
        Bundle result = new Bundle(); int code = Activity.RESULT_CANCELED;
        Activity[] active = new Activity[1]; PopupWindow[] popup = new PopupWindow[1];
        PrayerDock[] passive = new PrayerDock[1];
        boolean wanted = (Boolean) staticField(Radio.class, "wanted");
        try {
            check(android.os.Build.VERSION.SDK_INT >= 29, "Window inspection requires API 29+");
            test.setInTouchMode(false);
            prefs.edit().putBoolean("adhan", false).putBoolean("announce", false).putBoolean("remind_voice", false)
                .putInt("remind", 0).putBoolean("remote_enabled", false).putInt("appearance_cycle", 0)
                .putString("dock_mode", "hidden").putBoolean("quick_labels", false).commit();
            for (String language : new String[] {"ar", "en"}) for (int size : new int[] {150, 80, 90, 100, 110, 120, 130, 140}) {
                prefs.edit().putString("locale", language).putInt("ui_scale", size).commit();
                MainActivity home = launch(test, MainActivity.class); active[0] = home;
                onMain(test, () -> popup[0] = QuickMenus.show(home, home.getCurrentFocus()));
                settle(test);
                onMain(test, () -> panel(popup[0].getContentView(), size, 640, false));
                shot(test, language, size, "quick-inputs");
                onMain(test, () -> { popup[0].dismiss(); popup[0] = null; });
                test.waitForIdleSync(); check(home.getCurrentFocus() != null, "Popup returns focus");
                if (RemoteKeys.watch == null) {
                    InputsActivity inputs = launch(test, InputsActivity.class); active[0] = inputs;
                    onMain(test, () -> panel(((ViewGroup) inputs.findViewById(android.R.id.content)).getChildAt(0), size, 640, false));
                    finish(test, inputs); active[0] = home;
                }
                onMain(test, () -> SystemScreens.dreams(home)); settle(test);
                onMain(test, () -> dialog(size)); shot(test, language, size, "saver-dialog");
                onMain(test, () -> dialogRoot().findViewById(android.R.id.button2).performClick());
                RemoteButtonsActivity remote = launch(test, RemoteButtonsActivity.class); active[0] = remote;
                onMain(test, () -> dialog(size));
                onMain(test, () -> {
                    ListView list = find(dialogRoot(), ListView.class);
                    check(list != null, "Actual remote-key list is visible");
                    list.performItemClick(list.getChildAt(0), 0, 0);
                }); settle(test);
                onMain(test, () -> dialog(size)); shot(test, language, size, "remote-dialog");
                onMain(test, () -> {
                    android.app.AlertDialog capture = (android.app.AlertDialog) UiContractDeviceCheck.field(remote, "captureDialog");
                    check(capture != null && capture.isShowing(), "Actual key-capture dialog is open"); capture.dismiss();
                }); finish(test, remote); active[0] = home;

                if (RemoteKeys.watch == null) {
                    String own = "tv.afnon.home/tv.afnon.home.RemoteKeys";
                    put(test, "enabled_accessibility_services", "null".equals(services) || services.isEmpty() ? own : services + ":" + own);
                    put(test, "accessibility_enabled", "1");
                    until(test, () -> RemoteKeys.watch != null, "Accessibility service connects");
                }
                Object service = staticField(RemoteKeys.class, "running");
                PrayerDock dock = (PrayerDock) UiContractDeviceCheck.field(service, "dock");
                for (String position : new String[] {"top", "bottom"}) for (boolean labels : new boolean[] {false, true}) {
                    prefs.edit().putString("dock_position", position).putBoolean("quick_labels", labels).commit();
                    onMain(test, () -> { dock.hide(); dock.toggle(); }); settle(test);
                    onMain(test, () -> {
                        View window = (View) UiContractDeviceCheck.field(dock, "window");
                        check(window != null, "Actual prayer bar attaches"); panel(((ViewGroup) window).getChildAt(0), size, 1920, true);
                    });
                    shot(test, language, size, position + "-bar" + (labels ? "-labels" : ""));
                }
                View previous = (View) UiContractDeviceCheck.field(dock, "window");
                int changedSize = size == 110 ? 100 : 110;
                prefs.edit().putInt("ui_scale", changedSize).commit(); settle(test);
                onMain(test, () -> {
                    View changed = (View) UiContractDeviceCheck.field(dock, "window");
                    check(changed != previous, "Changing size rebuilds an already open bar");
                    panel(((ViewGroup) changed).getChildAt(0), changedSize, 1920, true);
                });
                prefs.edit().putInt("ui_scale", size).commit(); settle(test);
                onMain(test, () -> { dock.hide(); dock.inputs(); }); settle(test);
                onMain(test, () -> panel(((ViewGroup) UiContractDeviceCheck.field(dock, "window")).getChildAt(0), size, 640, false));
                onMain(test, dock::hide);
                prefs.edit().putString("dock_mode", "tab").commit();
                onMain(test, () -> {
                    passive[0] = new PrayerDock((RemoteKeys) service);
                    android.view.accessibility.AccessibilityEvent event = android.view.accessibility.AccessibilityEvent.obtain(android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
                    event.setPackageName("tv.afnon.fixture"); event.setClassName("FixtureActivity");
                    passive[0].onWindow(event); event.recycle();
                }); settle(test);
                onMain(test, () -> panel((View) UiContractDeviceCheck.field(passive[0], "window"), size, 1920, true));
                shot(test, language, size, "tab");
                onMain(test, () -> { passive[0].stop(); passive[0] = null; });
                prefs.edit().putString("dock_mode", "hidden").commit();
                onMain(test, () -> RemoteKeys.watch.showBanner(new PrayerDay(home, new Date()), new Date())); settle(test);
                onMain(test, () -> panel((View) UiContractDeviceCheck.field(RemoteKeys.watch, "banner"), size, 640, false));
                shot(test, language, size, "reminder"); onMain(test, () -> RemoteKeys.watch.hideBanner());

                onMain(test, () -> wanted(true));
                ScreenOffActivity off = launch(test, ScreenOffActivity.class); active[0] = off;
                onMain(test, () -> {
                    ViewGroup root = (ViewGroup) ((ViewGroup) off.findViewById(android.R.id.content)).getChildAt(0);
                    check(Math.abs(root.getChildAt(0).getScaleX() - size / 100f) < .001, "Screen-off content scales");
                    check(root.getScaleX() == 1f, "Black background stays full size");
                }); shot(test, language, size, "screen-off"); finish(test, off); active[0] = home;
                onMain(test, () -> wanted(wanted));

                put(test, "screensaver_components", "tv.afnon.home/.AfnonDream"); put(test, "screensaver_enabled", "1");
                shell(test, "am start -W -a android.settings.DREAM_SETTINGS"); settle(test);
                String app = test.getTargetContext().getString(R.string.app_name);
                String preview = "Preview " + app;
                until(test, () -> preview(test, preview) != null || named(test.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).getRootInActiveWindow(), app) != null,
                    "System Settings loads the installed launcher dream");
                if (preview(test, preview) == null) {
                    AccessibilityNodeInfo choice = named(test.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).getRootInActiveWindow(), app);
                    check(choice != null, "System Settings lists the installed launcher dream");
                    while (!choice.isClickable() && choice.getParent() != null) choice = choice.getParent();
                    check(choice.performAction(AccessibilityNodeInfo.ACTION_CLICK), "Select the installed launcher dream"); settle(test);
                }
                AccessibilityNodeInfo node = preview(test, preview);
                check(node != null && node.performAction(AccessibilityNodeInfo.ACTION_CLICK), "System Settings previews the configured launcher dream");
                until(test, () -> AfnonDream.running, "Actual dream starts"); settle(test);
                onMain(test, () -> {
                    View decor = dreamRoot(); AfnonDream dream = (AfnonDream) owner(decor.getContext(), AfnonDream.class);
                    FrameLayout group = (FrameLayout) UiContractDeviceCheck.field(dream, "group");
                    check(Math.abs(group.getScaleX() - size / 100f) < .001, "Dream content scales");
                    for (int i = 0; i < 40; i++) {
                        try { java.lang.reflect.Method drift = AfnonDream.class.getDeclaredMethod("drift", boolean.class); drift.setAccessible(true); drift.invoke(dream, false); }
                        catch (ReflectiveOperationException error) { throw new AssertionError(error); }
                        check(group.getTranslationX() >= Ui.px(96) && group.getTranslationY() >= Ui.px(56)
                            && group.getTranslationX() + group.getWidth() * group.getScaleX() <= Ui.px(1824) + 1
                            && group.getTranslationY() + group.getHeight() * group.getScaleY() <= Ui.px(1024) + 1, "Scaled dream drift stays in safe area");
                    }
                }); SystemClock.sleep(1200); test.waitForIdleSync(); shot(test, language, size, "dream");
                stopDream(test); until(test, () -> !AfnonDream.running, "Dream stops");
                put(test, "enabled_accessibility_services", services); put(test, "accessibility_enabled", enabled);
                until(test, () -> RemoteKeys.watch == null, "Owned accessibility service disconnects");
                finish(test, home); active[0] = null;
                Bundle progress = new Bundle(); progress.putString("stream", "PASS: native windows " + language + " " + size + "% popup/dialogs/bar/banner/dream/screen-off.\n"); test.sendStatus(0, progress);
            }
            result.putString("stream", "Native size windows passed; media stayed disabled.\n"); code = Activity.RESULT_OK;
        } catch (Throwable error) { result.putString("stream", android.util.Log.getStackTraceString(error)); }
        finally {
            stopDream(test);
            onMain(test, () -> { RemoteKeys.hideDock(); if (RemoteKeys.watch != null) RemoteKeys.watch.hideBanner();
                if (passive[0] != null) passive[0].stop();
                if (popup[0] != null) popup[0].dismiss(); if (active[0] != null) active[0].finish();
                wanted(wanted);
                for (View root : WindowInspector.getGlobalWindowViews()) {
                    Context owner = owner(root.getContext(), Activity.class); if (owner != null) ((Activity) owner).finish();
                }
            });
            put(test, "enabled_accessibility_services", services); put(test, "accessibility_enabled", enabled);
            put(test, "screensaver_components", dreams); put(test, "screensaver_enabled", dreamEnabled);
            UiScaleDeviceCheck.restore(prefs, original);
            if (!prefs.getAll().equals(original)) { code = Activity.RESULT_CANCELED; result.putString("stream", "Preference restoration failed"); }
        }
        test.finish(code, result);
    }

    private static Object staticField(Class<?> type, String name) {
        try { java.lang.reflect.Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(null); }
        catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }
    private static void wanted(boolean value) {
        try { java.lang.reflect.Field field = Radio.class.getDeclaredField("wanted"); field.setAccessible(true); field.setBoolean(null, value); }
        catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }
    private static void panel(View content, int size, int width, boolean reflow) {
        check(content != null && content.isAttachedToWindow(), "Sized content is a real attached window");
        View logical = size == 100 ? content : ((ViewGroup) content).getChildAt(0);
        check(Math.abs(logical.getScaleX() - size / 100f) < .001, "Window content scales");
        check(Math.abs(content.getWidth() - Ui.px(width) * (reflow ? 1 : size / 100f)) <= 1, "Window has scaled width");
        int[] at = new int[2]; content.getLocationOnScreen(at);
        Rect viewport = new Rect(at[0], at[1], at[0] + content.getWidth(), at[1] + content.getHeight());
        check(viewport.left >= 0 && viewport.top >= 0 && viewport.right <= Ui.ipx(1920) && viewport.bottom <= Ui.ipx(1080), "Window fits display");
        UiScaleDeviceCheck.bounds(content, false, viewport);
        check(content.getHeight() >= logical.getHeight() * size / 100f - 1, "Window height contains scaled content");
    }
    private static View dialogRoot() {
        for (View root : WindowInspector.getGlobalWindowViews()) if (root.isShown() && root.findViewById(android.R.id.button2) != null) return root;
        throw new AssertionError("Actual dialog is missing");
    }
    private static <T extends View> T find(View root, Class<T> type) {
        if (type.isInstance(root)) return type.cast(root);
        if (root instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) root).getChildCount(); i++) {
            T child = find(((ViewGroup) root).getChildAt(i), type); if (child != null) return child;
        }
        return null;
    }
    private static void dialog(int size) {
        View root = dialogRoot(); View button = root.findViewById(android.R.id.button2);
        check(Math.abs(button.getResources().getDisplayMetrics().density - size / 100f) < .01, "Dialog uses local sized resources");
        check(Math.abs(button.getContext().getResources().getConfiguration().fontScale - root.getContext().getResources().getConfiguration().fontScale) < .001, "System font scale is retained");
        Rect visible = new Rect(); check(button.getGlobalVisibleRect(visible) && visible.width() > 0 && visible.height() > 0, "Dialog action is visible");
    }
    private static Context owner(Context context, Class<?> type) {
        while (!type.isInstance(context) && context instanceof ContextWrapper) {
            Context next = ((ContextWrapper) context).getBaseContext(); if (next == context) break; context = next;
        }
        return type.isInstance(context) ? context : null;
    }
    private static View dreamRoot() {
        for (View root : WindowInspector.getGlobalWindowViews()) {
            View dream = dreamView(root); if (dream != null) return dream;
        }
        throw new AssertionError("Actual dream window is missing");
    }
    private static View dreamView(View view) {
        if (owner(view.getContext(), AfnonDream.class) != null) return view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            View dream = dreamView(((ViewGroup) view).getChildAt(i)); if (dream != null) return dream;
        }
        return null;
    }
    private static void stopDream(Instrumentation test) {
        if (AfnonDream.running) shell(test, "input keyevent KEYCODE_BACK");
    }
    private static AccessibilityNodeInfo preview(Instrumentation test, String label) {
        AccessibilityNodeInfo root = test.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).getRootInActiveWindow();
        return preview(root, label);
    }
    private static AccessibilityNodeInfo preview(AccessibilityNodeInfo root, String label) {
        if (root == null) return null;
        if (root.getContentDescription() != null && label.contentEquals(root.getContentDescription())) return root;
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = preview(root.getChild(i), label); if (child != null) return child;
        }
        return null;
    }
    private static AccessibilityNodeInfo named(AccessibilityNodeInfo root, String label) {
        if (root == null) return null;
        if (root.getText() != null && label.contentEquals(root.getText())) return root;
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = named(root.getChild(i), label); if (child != null) return child;
        }
        return null;
    }
    private static <T extends Activity> T launch(Instrumentation test, Class<T> type) {
        T activity = type.cast(test.startActivitySync(new Intent(test.getTargetContext(), type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        settle(test); return activity;
    }
    private static void finish(Instrumentation test, Activity activity) { test.runOnMainSync(activity::finish); settle(test); }
    private static void settle(Instrumentation test) { SystemClock.sleep(200); test.waitForIdleSync(); }
    private static void onMain(Instrumentation test, Runnable action) { UiContractDeviceCheck.onMain(test, action); }
    private static void until(Instrumentation test, BooleanSupplier ready, String message) {
        long end = SystemClock.uptimeMillis() + 7000;
        while (!ready.getAsBoolean() && SystemClock.uptimeMillis() < end) SystemClock.sleep(50);
        check(ready.getAsBoolean(), message); settle(test);
    }
    private static void shot(Instrumentation test, String language, int size, String name) throws Exception {
        if (size != 80 && size != 110 && size != 150) return;
        Bitmap image = test.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).takeScreenshot();
        check(image != null, "Window screenshot is available");
        try (FileOutputStream output = new FileOutputStream(test.getTargetContext().getCacheDir() + "/size-native-" + language + "-" + size + "-" + name + ".png")) { image.compress(Bitmap.CompressFormat.PNG, 100, output); }
        finally { image.recycle(); }
    }
    private static String shell(Instrumentation test, String command) {
        try (ParcelFileDescriptor.AutoCloseInputStream input = new ParcelFileDescriptor.AutoCloseInputStream(test.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES).executeShellCommand(command));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[1024]; int count;
            while ((count = input.read(bytes)) != -1) output.write(bytes, 0, count);
            return output.toString("UTF-8").trim();
        } catch (Exception error) { throw new AssertionError(error); }
    }
    private static void put(Instrumentation test, String key, String value) {
        check(value.matches("[A-Za-z0-9_./:]*"), "Only component/settings tokens are passed to the shell command API");
        shell(test, "settings " + ("null".equals(value) ? "delete secure " + key : "put secure " + key + " " + value));
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
