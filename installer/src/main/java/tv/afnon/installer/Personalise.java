package tv.afnon.installer;

import android.app.AlertDialog;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * "Make it yours", after the install: the city and prayer settings, the four favourites, the remote's app buttons
 * and the cleanup. Every choice is read from and written to Afnon on the TV (SetupProvider), so its cities and
 * settings stay the one source of truth; on an Afnon without the provider (0.5) only the cleanup runs.
 */
final class Personalise {
    private final MainActivity a;
    private final Tv tv;
    private final boolean ar;

    /** Afnon's own data, from the TV. */
    private JSONObject info;
    private JSONArray cities, apps;
    /** The choices so far. */
    private String city, locale;
    private boolean adhan, remind;
    private final List<String> favourites = new ArrayList<>();
    /** Button key (without KEYCODE_) to package; insertion order is the order shown. */
    private final Map<String, String> keys = new LinkedHashMap<>();
    private final Map<String, byte[]> icons = new HashMap<>();
    private int cleanedCount = -1;

    Personalise(MainActivity a, Tv tv) {
        this.a = a;
        this.tv = tv;
        this.ar = Ui.arabic(a);
    }

    void start() {
        a.frame(MainActivity.PERSONAL, Hero.Mode.CITY, a.getString(R.string.personal_title),
                a.getString(R.string.personal_loading));
        a.back = null;
        int g = a.gen;
        a.work.execute(() -> {
            try {
                info = tv.info();
                if (info != null) {
                    cities = tv.list("cities");
                    apps = tv.list("apps");
                    JSONArray k = tv.list("keys");
                    for (int i = 0; i < k.length(); i++) {
                        keys.put(strip(k.getJSONObject(i).getString("key")), k.getJSONObject(i).getString("p"));
                    }
                    city = info.optString("city", "cairo");
                    locale = info.optString("locale", "system");
                    adhan = info.optBoolean("adhan", true);
                    remind = info.optBoolean("remind_voice", true);
                    List<String[]> fav = new ArrayList<>();
                    for (int i = 0; i < apps.length(); i++) {
                        JSONObject app = apps.getJSONObject(i);
                        if (app.optInt("fav", -1) >= 0) fav.add(new String[] {String.valueOf(app.getInt("fav")), app.getString("p")});
                    }
                    fav.sort((x, y) -> Integer.compare(Integer.parseInt(x[0]), Integer.parseInt(y[0])));
                    for (String[] f : fav) favourites.add(f[1]);
                }
                a.post(g, () -> {
                    if (info == null) clean();
                    else prayer();
                });
            } catch (Exception e) {
                a.post(g, this::clean);
            }
        });
    }

    // Prayer times: city, language, adhan

    private void prayer() {
        a.frame(MainActivity.PERSONAL, Hero.Mode.CITY, a.getString(R.string.prayer_title),
                a.getString(R.string.prayer_lead));
        LinearLayout where = Ui.row(a);
        GradientDrawable bg = Ui.round(Ui.LAPIS_800, 18, a);
        bg.setStroke(Ui.dp(a, 1), 0x66d8b46a);
        Ui.pressable(where, bg, 0x33d8b46a);
        int p = Ui.dp(a, 16);
        where.setPadding(p, p, p, p);
        LinearLayout words = Ui.column(a);
        words.addView(Ui.small(a, a.getString(R.string.city_label)));
        JSONObject c = cityById(city);
        words.addView(Ui.text(a, c == null ? city : name(c) + "  ·  " + country(c), Ui.PLASTER, 18, Ui.semibold()),
                Ui.wide(a, 4));
        where.addView(words, Ui.fill(a));
        where.addView(Ui.text(a, "›", Ui.GOLD_400, 26, Ui.medium()));
        where.setOnClickListener(v -> a.go(this::cityPicker));
        a.add(where, 20);

        LinearLayout lang = Ui.card(a);
        lang.addView(Ui.label(a, a.getString(R.string.lang_label)));
        lang.addView(segmented(new String[] {"ar", "en", "system"},
                new int[] {R.string.lang_ar, R.string.lang_en, R.string.lang_tv}, locale, v -> locale = v), Ui.wide(a, 12));
        a.add(lang, 10);

        LinearLayout toggles = Ui.card(a);
        toggles.addView(toggle(R.string.adhan_label, adhan, on -> adhan = on));
        toggles.addView(toggle(R.string.remind_label, remind, on -> remind = on), Ui.wide(a, 12));
        a.add(toggles, 10);

        a.foot(Ui.primary(a, a.getString(R.string.continue_), v -> save(() -> {
            tv.set("city", city);
            tv.set("locale", locale);
            tv.set("adhan", adhan ? "on" : "off");
            tv.set("remind_voice", remind ? "on" : "off");
        }, this::favourites)));
        a.back = null;
    }

    private void cityPicker() {
        a.frame(MainActivity.PERSONAL, Hero.Mode.CITY, a.getString(R.string.city_title), a.getString(R.string.city_lead));
        EditText search = new EditText(a);
        search.setHint(R.string.city_search);
        search.setHintTextColor(0x809fb0cc);
        search.setTextColor(Ui.PLASTER);
        search.setTextSize(16);
        search.setTypeface(Ui.medium());
        search.setSingleLine();
        search.setInputType(InputType.TYPE_CLASS_TEXT);
        search.setBackground(Ui.round(Ui.LAPIS_800, 14, a));
        search.setPadding(Ui.dp(a, 16), Ui.dp(a, 14), Ui.dp(a, 16), Ui.dp(a, 14));
        a.add(search, 20);
        LinearLayout list = Ui.column(a);
        a.add(list, 8);
        Runnable fill = () -> fillCities(list, search.getText().toString());
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int x, int y, int z) {}
            @Override public void onTextChanged(CharSequence s, int x, int y, int z) {}
            @Override public void afterTextChanged(Editable e) { fill.run(); }
        });
        fill.run();
        a.foot(Ui.secondary(a, a.getString(R.string.back), v -> a.go(this::prayer)));
        a.back = () -> a.go(this::prayer);
    }

    private void fillCities(LinearLayout list, String query) {
        list.removeAllViews();
        String q = query.trim().toLowerCase(Locale.ROOT);
        String lastCountry = null;
        for (int i = 0; i < cities.length(); i++) {
            JSONObject c = cities.optJSONObject(i);
            if (c == null) continue;
            String hay = (c.optString("ar") + " " + c.optString("en") + " " + c.optString("countryAr") + " "
                    + c.optString("countryEn")).toLowerCase(Locale.ROOT);
            if (!q.isEmpty() && !hay.contains(q)) continue;
            String country = country(c);
            if (!country.equals(lastCountry)) {
                TextView h = Ui.text(a, country, Ui.MIST, 13, Ui.medium());
                h.setPadding(Ui.dp(a, 4), Ui.dp(a, lastCountry == null ? 4 : 18), 0, Ui.dp(a, 6));
                list.addView(h);
                lastCountry = country;
            }
            boolean chosen = c.optString("id").equals(city);
            LinearLayout row = Ui.row(a);
            GradientDrawable bg = Ui.round(chosen ? Ui.GOLD_SOFT : Ui.LAPIS_800, 12, a);
            if (chosen) bg.setStroke(Ui.dp(a, 1), Ui.GOLD_400);
            Ui.pressable(row, bg, 0x33d8b46a);
            row.setPadding(Ui.dp(a, 16), Ui.dp(a, 14), Ui.dp(a, 16), Ui.dp(a, 14));
            row.addView(Ui.text(a, name(c), chosen ? Ui.GOLD_400 : Ui.PLASTER, 16, Ui.medium()), Ui.fill(a));
            if (chosen) row.addView(Ui.text(a, "✓", Ui.GOLD_400, 16, Ui.semibold()));
            String id = c.optString("id");
            row.setOnClickListener(v -> {
                city = id;
                a.go(this::prayer);
            });
            LinearLayout.LayoutParams lp = Ui.wide(a, 6);
            list.addView(row, lp);
        }
        if (list.getChildCount() == 0) list.addView(Ui.small(a, a.getString(R.string.city_none)));
    }

    // Favourites

    private void favourites() {
        a.frame(MainActivity.PERSONAL, Hero.Mode.APPS, a.getString(R.string.fav_title), a.getString(R.string.fav_lead));
        TextView count = Ui.latin(Ui.chip(a, "", true));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.gravity = Gravity.CENTER_HORIZONTAL;
        cp.topMargin = Ui.dp(a, 14);
        a.content.addView(count, cp);
        LinearLayout grid = Ui.column(a);
        a.add(grid, 14);
        List<View> tiles = new ArrayList<>();
        Runnable paint = () -> {
            count.setText(a.getString(R.string.fav_count, favourites.size()));
            for (View t : tiles) paintTile(t);
        };
        LinearLayout row = null;
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.optJSONObject(i);
            if (app == null) continue;
            if (tiles.size() % 3 == 0) {
                row = Ui.row(a);
                row.setGravity(Gravity.TOP);
                grid.addView(row, Ui.wide(a, tiles.isEmpty() ? 0 : 10));
            }
            View tile = appTile(app, paint);
            LinearLayout.LayoutParams lp = Ui.fill(a);
            if (tiles.size() % 3 != 0) lp.setMarginStart(Ui.dp(a, 10));
            row.addView(tile, lp);
            tiles.add(tile);
        }
        while (row != null && tiles.size() % 3 != 0) {
            View spacer = new View(a);
            LinearLayout.LayoutParams lp = Ui.fill(a);
            lp.setMarginStart(Ui.dp(a, 10));
            row.addView(spacer, lp);
            tiles.add(spacer);
        }
        paint.run();
        loadIcons(tiles);
        a.foot(Ui.primary(a, a.getString(R.string.continue_), v -> save(() -> {
            if (!favourites.isEmpty()) tv.set("favourites", String.join(",", favourites));
        }, this::buttons)));
        a.foot(Ui.secondary(a, a.getString(R.string.back), v -> a.go(this::prayer)));
        a.back = () -> a.go(this::prayer);
    }

    private View appTile(JSONObject app, Runnable paint) {
        String pkg = app.optString("p");
        LinearLayout t = Ui.column(a);
        t.setTag(pkg);
        t.setGravity(Gravity.CENTER_HORIZONTAL);
        t.setPadding(Ui.dp(a, 8), Ui.dp(a, 12), Ui.dp(a, 8), Ui.dp(a, 10));
        android.widget.FrameLayout iconBox = new android.widget.FrameLayout(a);
        ImageView icon = new ImageView(a);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setBackground(Ui.round(Ui.LAPIS_700, 14, a));
        int s = Ui.dp(a, 52);
        iconBox.addView(icon, new android.widget.FrameLayout.LayoutParams(s, s));
        TextView badge = Ui.badge(a, "");
        android.widget.FrameLayout.LayoutParams bp = new android.widget.FrameLayout.LayoutParams(Ui.dp(a, 22), Ui.dp(a, 22));
        bp.gravity = Gravity.TOP | Gravity.END;
        badge.setTextSize(11);
        iconBox.addView(badge, bp);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(Ui.dp(a, 60), Ui.dp(a, 58));
        t.addView(iconBox, ip);
        TextView name = Ui.text(a, ar ? app.optString("ar") : app.optString("en"), Ui.PLASTER, 12, Ui.medium());
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(2);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        t.addView(name, Ui.wide(a, 8));
        t.setOnClickListener(v -> {
            if (favourites.contains(pkg)) favourites.remove(pkg);
            else if (favourites.size() < 4) favourites.add(pkg);
            paint.run();
        });
        return t;
    }

    private void paintTile(View t) {
        if (!(t.getTag() instanceof String)) return;
        int slot = favourites.indexOf((String) t.getTag());
        GradientDrawable bg = Ui.round(slot >= 0 ? Ui.GOLD_SOFT : Ui.LAPIS_800, 16, a);
        bg.setStroke(Ui.dp(a, slot >= 0 ? 2 : 1), slot >= 0 ? Ui.GOLD_400 : Ui.LINE_SOFT);
        Ui.pressable(t, bg, 0x33d8b46a);
        TextView badge = (TextView) ((ViewGroup) ((ViewGroup) t).getChildAt(0)).getChildAt(1);
        badge.setVisibility(slot >= 0 ? View.VISIBLE : View.INVISIBLE);
        badge.setText(String.format(Locale.US, "%d", slot + 1));
    }

    private void loadIcons(List<View> tiles) {
        int g = a.gen;
        new Thread(() -> {
            for (View t : tiles) {
                if (g != a.gen) return;
                if (!(t.getTag() instanceof String)) continue;
                String pkg = (String) t.getTag();
                byte[] png = icons.get(pkg);
                try {
                    if (png == null) png = tv.icon(pkg);
                } catch (Exception e) {
                    png = null;
                }
                if (png == null) continue;
                icons.put(pkg, png);
                byte[] b = png;
                a.post(g, () -> {
                    ImageView icon = (ImageView) ((ViewGroup) ((ViewGroup) t).getChildAt(0)).getChildAt(0);
                    icon.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
                    icon.setBackground(null);
                });
            }
        }).start();
    }

    // Remote buttons: press one, pick its app

    private LinearLayout keyList;
    private TextView heard;

    private void buttons() {
        a.frame(MainActivity.PERSONAL, Hero.Mode.REMOTE, a.getString(R.string.keys_title), a.getString(R.string.keys_lead));
        LinearLayout listening = Ui.row(a);
        listening.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(Ui.dp(a, 16), Ui.dp(a, 16));
        sp.setMarginEnd(Ui.dp(a, 10));
        listening.addView(Timeline.spinner(a), sp);
        heard = Ui.text(a, a.getString(R.string.keys_listening), Ui.GOLD_200, 14, Ui.medium());
        listening.addView(heard);
        a.add(listening, 16);
        keyList = Ui.column(a);
        a.add(keyList, 14);
        a.add(Ui.small(a, a.getString(R.string.keys_hint)), 12);
        paintKeys(null);
        int g = a.gen;
        if (tv != null) new Thread(() -> {
            try {
                tv.listen(new Tv.Buttons() {
                    @Override public void pressed(String key) {
                        a.post(g, () -> {
                            heard.setText(a.getString(R.string.keys_heard, Tv.buttonName(key)));
                            if (!keys.containsKey(key)) keys.put(key, "");
                            paintKeys(key);
                            chooseApp(key);
                        });
                    }

                    @Override public void notApp(String key) {
                        a.post(g, () -> heard.setText(a.getString(R.string.keys_not_app, Tv.buttonName(key))));
                    }
                });
            } catch (Exception e) {
                a.post(g, () -> heard.setText(a.getString(R.string.keys_cant_listen)));
            }
        }).start();
        a.foot(Ui.primary(a, a.getString(R.string.continue_), v -> {
            tv.stopListening();
            save(() -> {
                StringBuilder b = new StringBuilder();
                for (Map.Entry<String, String> e : keys.entrySet()) {
                    if (e.getValue().isEmpty()) continue;
                    if (b.length() > 0) b.append(',');
                    b.append(e.getKey()).append('=').append(e.getValue());
                }
                tv.set("keys", b.toString());
            }, this::clean);
        }));
        a.foot(Ui.secondary(a, a.getString(R.string.back), v -> {
            tv.stopListening();
            a.go(this::favourites);
        }));
        a.back = () -> {
            tv.stopListening();
            a.go(this::favourites);
        };
    }

    private void paintKeys(String flash) {
        keyList.removeAllViews();
        for (Map.Entry<String, String> e : keys.entrySet()) {
            String key = e.getKey();
            LinearLayout row = Ui.row(a);
            boolean lit = key.equals(flash);
            GradientDrawable bg = Ui.round(lit ? Ui.GOLD_SOFT : Ui.LAPIS_800, 14, a);
            bg.setStroke(Ui.dp(a, 1), lit ? Ui.GOLD_400 : Ui.LINE_SOFT);
            Ui.pressable(row, bg, 0x33d8b46a);
            row.setPadding(Ui.dp(a, 14), Ui.dp(a, 12), Ui.dp(a, 14), Ui.dp(a, 12));
            TextView button = Ui.latin(Ui.chip(a, Tv.buttonName(key), false));
            row.addView(button);
            TextView arrow = Ui.text(a, "→", Ui.MIST, 16, Ui.medium());
            arrow.setPadding(Ui.dp(a, 10), 0, Ui.dp(a, 10), 0);
            row.addView(arrow);
            String app = e.getValue();
            TextView target = Ui.text(a, app.isEmpty() ? a.getString(R.string.keys_pick) : appName(app),
                    app.isEmpty() ? Ui.GOLD_200 : Ui.PLASTER, 15, Ui.medium());
            target.setSingleLine();
            target.setEllipsize(android.text.TextUtils.TruncateAt.END);
            row.addView(target, Ui.fill(a));
            row.setOnClickListener(v -> chooseApp(key));
            keyList.addView(row, Ui.wide(a, keyList.getChildCount() == 0 ? 0 : 8));
        }
    }

    private void chooseApp(String key) {
        List<String> pkgs = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.optJSONObject(i);
            if (app == null) continue;
            pkgs.add(app.optString("p"));
            names.add(ar ? app.optString("ar") : app.optString("en"));
        }
        names.add(a.getString(R.string.keys_nothing));
        new AlertDialog.Builder(a, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(a.getString(R.string.keys_choose, Tv.buttonName(key)))
                .setItems(names.toArray(new String[0]), (d, which) -> {
                    if (which < pkgs.size()) keys.put(key, pkgs.get(which));
                    else keys.remove(key);
                    paintKeys(key);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Cleanup

    private void clean() {
        a.frame(MainActivity.PERSONAL, Hero.Mode.CLEAN, a.getString(R.string.clean_title), a.getString(R.string.clean_lead));
        View wait = a.waiting(a.getString(R.string.clean_loading));
        a.add(wait, 20);
        a.back = null;
        int g = a.gen;
        a.work.execute(() -> {
            try {
                List<Setup.Bloat> catalogue = Setup.catalogue();
                Set<String> enabled = tv.enabledPackages();
                List<String> earlier = tv.cleaned();
                List<Setup.Bloat> offered = new ArrayList<>();
                for (Setup.Bloat b : catalogue) {
                    // Never the apps just chosen as favourites or for a button.
                    if (enabled.contains(b.pkg) && !favourites.contains(b.pkg) && !keys.containsValue(b.pkg)) offered.add(b);
                }
                a.post(g, () -> showClean(offered, earlier, catalogue.isEmpty()));
            } catch (Exception e) {
                a.post(g, () -> showClean(new ArrayList<>(), new ArrayList<>(), true));
            }
        });
    }

    private static final String[] KINDS = {"tracking", "ads", "demo", "launcher", "apps", "background"};
    private static final int[] KIND_NAMES = {R.string.kind_tracking, R.string.kind_ads, R.string.kind_demo,
            R.string.kind_launcher, R.string.kind_apps, R.string.kind_background};

    private void showClean(List<Setup.Bloat> offered, List<String> earlier, boolean offline) {
        a.frame(MainActivity.PERSONAL, Hero.Mode.CLEAN, a.getString(R.string.clean_title), a.getString(R.string.clean_lead));
        List<String> chosen = new ArrayList<>();
        for (Setup.Bloat b : offered) if (b.suggested()) chosen.add(b.pkg);
        TextView go = Ui.primary(a, "", null);
        Runnable count = () -> {
            go.setText(chosen.isEmpty() ? a.getString(R.string.continue_)
                    : a.getString(R.string.clean_button, String.format(Locale.US, "%d", chosen.size())));
        };

        if (!earlier.isEmpty()) {
            LinearLayout card = Ui.card(a);
            card.addView(Ui.label(a, a.getString(R.string.clean_earlier, String.format(Locale.US, "%d", earlier.size()))));
            TextView undo = Ui.secondary(a, a.getString(R.string.clean_undo), v -> a.go(() -> restore(earlier)));
            card.addView(undo, Ui.wide(a, 12));
            a.add(card, 20);
        }
        if (offered.isEmpty()) {
            LinearLayout card = Ui.card(a);
            card.addView(Ui.small(a, a.getString(offline ? R.string.clean_offline : R.string.clean_none)));
            a.add(card, 20);
        }
        for (int k = 0; k < KINDS.length; k++) {
            List<Setup.Bloat> group = new ArrayList<>();
            for (Setup.Bloat b : offered) if (b.kind.equals(KINDS[k])) group.add(b);
            if (group.isEmpty()) continue;
            a.add(cleanGroup(KIND_NAMES[k], group, chosen, count), 14);
        }
        TextView credit = Ui.small(a, a.getString(R.string.clean_credit));
        credit.setGravity(Gravity.CENTER);
        a.add(credit, 16);

        count.run();
        go.setOnClickListener(v -> {
            if (chosen.isEmpty()) a.go(this::done);
            else a.go(() -> cleaning(new ArrayList<>(chosen)));
        });
        a.foot(go);
        a.foot(Ui.secondary(a, a.getString(R.string.skip), v -> a.go(this::done)));
        a.back = null;
    }

    private View cleanGroup(int title, List<Setup.Bloat> group, List<String> chosen, Runnable count) {
        LinearLayout card = Ui.card(a);
        LinearLayout head = Ui.row(a);
        head.addView(Ui.label(a, a.getString(title)), Ui.fill(a));
        head.addView(Ui.latin(Ui.chip(a, String.format(Locale.US, "%d", group.size()), false)));
        card.addView(head);
        LinearLayout rows = Ui.column(a);
        card.addView(rows, Ui.wide(a, 6));
        int shown = Math.min(group.size(), 6);
        for (int i = 0; i < group.size(); i++) {
            View r = bloatRow(group.get(i), chosen, count);
            if (i >= shown) r.setVisibility(View.GONE);
            rows.addView(r, Ui.wide(a, 4));
        }
        if (group.size() > shown) {
            TextView more = Ui.text(a, a.getString(R.string.clean_more, String.format(Locale.US, "%d", group.size() - shown)),
                    Ui.GOLD_400, 14, Ui.medium());
            more.setPadding(0, Ui.dp(a, 10), 0, 0);
            more.setOnClickListener(v -> {
                for (int i = 0; i < rows.getChildCount(); i++) rows.getChildAt(i).setVisibility(View.VISIBLE);
                more.setVisibility(View.GONE);
            });
            card.addView(more);
        }
        return card;
    }

    private View bloatRow(Setup.Bloat b, List<String> chosen, Runnable count) {
        LinearLayout row = Ui.row(a);
        row.setGravity(Gravity.TOP);
        row.setPadding(0, Ui.dp(a, 8), 0, Ui.dp(a, 4));
        TextView box = Ui.text(a, "", Ui.LAPIS_900, 13, Ui.semibold());
        box.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(Ui.dp(a, 22), Ui.dp(a, 22));
        bp.setMarginEnd(Ui.dp(a, 12));
        bp.topMargin = Ui.dp(a, 1);
        row.addView(box, bp);
        LinearLayout words = Ui.column(a);
        String label = appName(b.pkg);
        words.addView(Ui.text(a, label, Ui.PLASTER, 14, Ui.medium()));
        StringBuilder sub = new StringBuilder();
        if (!label.equals(b.pkg)) sub.append(b.pkg);
        if (!b.note.isEmpty()) sub.append(sub.length() > 0 ? " · " : "").append(b.note);
        if (b.kept > 0) sub.append(sub.length() > 0 ? " · " : "").append(a.getString(R.string.clean_kept));
        if (sub.length() > 0) {
            TextView s = Ui.small(a, sub.toString());
            s.setTextSize(12);
            words.addView(s, Ui.wide(a, 2));
        }
        row.addView(words, Ui.fill(a));
        Runnable paint = () -> {
            boolean on = chosen.contains(b.pkg);
            GradientDrawable g = Ui.round(on ? Ui.GOLD_400 : 0, 6, a);
            g.setStroke(Ui.dp(a, 2), on ? Ui.GOLD_400 : 0x999fb0cc);
            box.setBackground(g);
            box.setText(on ? "✓" : "");
        };
        paint.run();
        row.setClickable(true);
        row.setOnClickListener(v -> {
            if (chosen.contains(b.pkg)) chosen.remove(b.pkg);
            else chosen.add(b.pkg);
            paint.run();
            count.run();
        });
        return row;
    }

    private void cleaning(List<String> pkgs) {
        a.frame(MainActivity.PERSONAL, Hero.Mode.PROGRESS, a.getString(R.string.cleaning_title), "");
        a.back = null;
        int g = a.gen;
        a.work.execute(() -> {
            int done = 0, ok = 0;
            for (String p : pkgs) {
                try {
                    if (tv.disable(p)) ok++;
                } catch (Exception ignored) {
                    // Skip it; the rest still go.
                }
                done++;
                int d = done;
                a.post(g, () -> {
                    a.hero.progress((float) d / pkgs.size());
                    a.leadView.setText(a.getString(R.string.cleaning,
                            String.format(Locale.US, "%d", d), String.format(Locale.US, "%d", pkgs.size())));
                });
            }
            cleanedCount = ok;
            a.post(g, this::done);
        });
    }

    private void restore(List<String> pkgs) {
        a.frame(MainActivity.PERSONAL, Hero.Mode.PROGRESS, a.getString(R.string.restoring_title), "");
        a.back = null;
        int g = a.gen;
        a.work.execute(() -> {
            int done = 0;
            for (String p : pkgs) {
                try {
                    tv.enable(p);
                } catch (Exception ignored) {
                    // Skip it.
                }
                done++;
                int d = done;
                a.post(g, () -> a.hero.progress((float) d / pkgs.size()));
            }
            try {
                tv.forgetCleaned();
            } catch (Exception ignored) {
                // The list stays; restoring again is harmless.
            }
            a.post(g, this::clean);
        });
    }

    // Done

    private void done() {
        List<String> summary = new ArrayList<>();
        if (info != null) {
            JSONObject c = cityById(city);
            if (c != null) summary.add(a.getString(R.string.sum_city, name(c)));
            if (!favourites.isEmpty()) {
                List<String> names = new ArrayList<>();
                for (String p : favourites) names.add(appName(p));
                summary.add(a.getString(R.string.sum_favourites, String.join(ar ? "، " : ", ", names)));
            }
            int mapped = 0;
            for (String v : keys.values()) if (!v.isEmpty()) mapped++;
            if (mapped > 0) summary.add(a.getString(R.string.sum_keys, String.format(Locale.US, "%d", mapped)));
        }
        if (cleanedCount > 0) summary.add(a.getString(R.string.sum_cleaned, String.format(Locale.US, "%d", cleanedCount)));
        a.showDone(summary, info == null);
        // Back to Home, so the TV shows the result.
        a.work.execute(() -> {
            try {
                a.adbOrNull().shell("am start -a android.intent.action.MAIN -c android.intent.category.HOME");
            } catch (Exception ignored) {
                // The user can press Home.
            }
        });
    }

    // Debug builds only: each screen with sample data, for screenshots (MainActivity.showDemo).

    void demo(String screen) {
        try {
            info = new JSONObject().put("version", "0.6").put("city", "cairo");
            cities = new JSONArray();
            String[][] c = {{"cairo", "القاهرة", "Cairo", "مصر", "Egypt"}, {"giza", "الجيزة", "Giza", "مصر", "Egypt"},
                    {"alexandria", "الإسكندرية", "Alexandria", "مصر", "Egypt"},
                    {"makkah", "مكة المكرمة", "Makkah", "السعودية", "Saudi Arabia"},
                    {"madinah", "المدينة المنورة", "Madinah", "السعودية", "Saudi Arabia"},
                    {"riyadh", "الرياض", "Riyadh", "السعودية", "Saudi Arabia"},
                    {"dubai", "دبي", "Dubai", "الإمارات", "UAE"}};
            for (String[] x : c) {
                cities.put(new JSONObject().put("id", x[0]).put("ar", x[1]).put("en", x[2]).put("countryAr", x[3])
                        .put("countryEn", x[4]));
            }
            apps = new JSONArray();
            String[][] p = {{"com.google.android.youtube.tv", "يوتيوب", "YouTube"}, {"net.mbc.shahidTV", "شاهد", "Shahid"},
                    {"dev.ctv.tv", "Claude Code TV", "Claude Code TV"}, {"com.tcl.browser", "المتصفح", "BrowseHere"},
                    {"com.netflix.ninja", "نتفليكس", "Netflix"}, {"com.google.android.youtube.tvmusic", "يوتيوب ميوزك", "YouTube Music"},
                    {"com.android.vending", "متجر Play", "Google Play Store"}, {"com.sami.salaty_tv", "صلاتي", "Salaty"},
                    {"com.alphainventor.filemanager", "مدير الملفات", "File Manager +"}};
            for (String[] x : p) apps.put(new JSONObject().put("p", x[0]).put("ar", x[1]).put("en", x[2]));
        } catch (org.json.JSONException ignored) {
            // Constant data.
        }
        city = "cairo";
        locale = "system";
        adhan = true;
        remind = true;
        favourites.add("com.google.android.youtube.tv");
        favourites.add("net.mbc.shahidTV");
        favourites.add("dev.ctv.tv");
        keys.put("TCL_YOUTUBE", "com.google.android.youtube.tv");
        keys.put("TCL_NETFLIX", "net.mbc.shahidTV");
        keys.put("INFO", "dev.ctv.tv");
        switch (screen) {
            case "city": cityPicker(); break;
            case "favourites": favourites(); break;
            case "keys":
                buttons();
                heard.setText(a.getString(R.string.keys_heard, Tv.buttonName("TCL_NETFLIX")));
                paintKeys("TCL_NETFLIX");
                break;
            case "clean": {
                List<Setup.Bloat> offered = new ArrayList<>();
                offered.add(new Setup.Bloat("tv.samba.ssm.ui", 13, 0, "tracking", "Samba TV ACR viewing tracking"));
                offered.add(new Setup.Bloat("com.tcl.logkit", 26, 0, "tracking", "log capture"));
                offered.add(new Setup.Bloat("com.google.android.feedback", 194, 0, "tracking", "send feedback"));
                offered.add(new Setup.Bloat("com.tcl.ttvs", 17, 1, "tracking", "TCL identifier/awareness services"));
                offered.add(new Setup.Bloat("com.tcl.messagebox", 42, 0, "ads", "pushed messages and promos"));
                offered.add(new Setup.Bloat("com.tcl.partnercustomizer", 39, 0, "ads", "promo rows on the stock home"));
                offered.add(new Setup.Bloat("com.tcl.esticker", 51, 0, "ads", "store-demo sticker"));
                offered.add(new Setup.Bloat("com.tcl.exhibit", 18, 0, "demo", "store demo"));
                offered.add(new Setup.Bloat("com.google.android.apps.tv.launcherx", 29, 0, "launcher", "stock Google TV home"));
                offered.add(new Setup.Bloat("com.netflix.ninja", 25, 0, "apps", "off by choice"));
                offered.add(new Setup.Bloat("com.osn.go", 1, 0, "apps", "OSN+"));
                showClean(offered, new ArrayList<>(), false);
                break;
            }
            default: prayer();
        }
    }

    // Helpers

    /** Saves on the worker thread, then moves on. */
    private void save(Job job, Runnable next) {
        int g = ++a.gen;
        a.work.execute(() -> {
            try {
                job.run();
            } catch (Exception ignored) {
                // A setting that didn't take is not worth stopping for; Afnon's Settings can set it.
            }
            a.post(g, next);
        });
    }

    private interface Job { void run() throws Exception; }

    private JSONObject cityById(String id) {
        if (cities == null) return null;
        for (int i = 0; i < cities.length(); i++) {
            JSONObject c = cities.optJSONObject(i);
            if (c != null && c.optString("id").equals(id)) return c;
        }
        return null;
    }

    private String name(JSONObject c) { return ar ? c.optString("ar") : c.optString("en"); }
    private String country(JSONObject c) { return ar ? c.optString("countryAr") : c.optString("countryEn"); }

    private String appName(String pkg) {
        if (apps != null) {
            for (int i = 0; i < apps.length(); i++) {
                JSONObject app = apps.optJSONObject(i);
                if (app != null && app.optString("p").equals(pkg)) return ar ? app.optString("ar") : app.optString("en");
            }
        }
        return pkg;
    }

    private static String strip(String key) {
        return key.startsWith("KEYCODE_") ? key.substring(8) : key;
    }

    private interface Choice { void on(String value); }
    private interface Flip { void on(boolean value); }

    private View segmented(String[] values, int[] labels, String current, Choice choice) {
        LinearLayout row = Ui.row(a);
        row.setBackground(Ui.round(Ui.LAPIS_900, 14, a));
        row.setPadding(Ui.dp(a, 4), Ui.dp(a, 4), Ui.dp(a, 4), Ui.dp(a, 4));
        List<TextView> parts = new ArrayList<>();
        String[] now = {current};
        Runnable paint = () -> {
            for (int i = 0; i < parts.size(); i++) {
                boolean on = values[i].equals(now[0]);
                parts.get(i).setBackground(on ? Ui.round(Ui.GOLD_400, 10, a) : null);
                parts.get(i).setTextColor(on ? Ui.LAPIS_900 : Ui.PLASTER);
            }
        };
        for (int i = 0; i < values.length; i++) {
            TextView t = Ui.text(a, a.getString(labels[i]), Ui.PLASTER, 14, Ui.medium());
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, Ui.dp(a, 10), 0, Ui.dp(a, 10));
            String v = values[i];
            t.setOnClickListener(x -> {
                now[0] = v;
                choice.on(v);
                paint.run();
            });
            parts.add(t);
            row.addView(t, Ui.fill(a));
        }
        paint.run();
        return row;
    }

    private View toggle(int label, boolean on, Flip flip) {
        LinearLayout row = Ui.row(a);
        row.addView(Ui.text(a, a.getString(label), Ui.PLASTER, 15, Ui.medium()), Ui.fill(a));
        Switch s = new Switch(a);
        s.setChecked(on);
        android.content.res.ColorStateList thumb = new android.content.res.ColorStateList(
                new int[][] {{android.R.attr.state_checked}, {}}, new int[] {Ui.GOLD_400, Ui.MIST});
        android.content.res.ColorStateList track = new android.content.res.ColorStateList(
                new int[][] {{android.R.attr.state_checked}, {}}, new int[] {0x80d8b46a, Ui.LAPIS_700});
        s.setThumbTintList(thumb);
        s.setTrackTintList(track);
        s.setOnCheckedChangeListener((v, checked) -> flip.on(checked));
        row.addView(s);
        return row;
    }
}
