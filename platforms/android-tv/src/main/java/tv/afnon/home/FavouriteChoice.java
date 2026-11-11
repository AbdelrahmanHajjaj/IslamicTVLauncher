package tv.afnon.home;

import java.util.ArrayList;
import java.util.List;
import tv.afnon.core.HomeNavigation;

/** Home favourites keep their order in one or two rows, using the available width. */
final class FavouriteChoice {
    static final int QIBLA = HomeNavigation.TODAY, WIFI = HomeNavigation.NETWORK, SETTINGS = HomeNavigation.SETTINGS,
        F0 = HomeNavigation.FIRST_APP, M0 = HomeNavigation.FIRST_RECENT, ALL = HomeNavigation.ALL_APPS,
        RADIO = HomeNavigation.RADIO, INPUTS = HomeNavigation.INPUTS, NODES = HomeNavigation.NODE_COUNT;
    static final int MAX = HomeNavigation.MAX_APPS;
    static int visibleCount(int count, boolean classic) { return Math.max(0, Math.min(classic ? 4 : MAX, count)); }
    static int columns(int count, boolean classic) { return Math.max(1, classic ? visibleCount(count, true) : (Math.min(MAX, count) + 1) / 2); }
    static int columns(int count, boolean classic, boolean wide) { return !classic && wide ? Math.max(columns(count, false), Math.min(count, 4)) : columns(count, classic); }
    static int move(int slot, int count, int dx, int dy, boolean classic) {
        return move(slot, count, dx, dy, columns(count, classic));
    }
    static int move(int slot, int count, int dx, int dy, int columns) {
        if (count < 1 || count > MAX) return -1;
        slot = Math.max(0, Math.min(count - 1, slot));
        int start = slot / columns * columns, end = Math.min(start + columns - 1, count - 1);
        if (dx != 0) return Math.max(start, Math.min(end, slot + dx));
        if (dy > 0 && slot < columns && count > columns) return Math.min(count - 1, slot + columns);
        if (dy < 0 && slot >= columns) return slot - columns;
        return slot;
    }
    /** Logical directions; MainActivity swaps horizontal keys in Arabic before calling. */
    static int homeMove(int from, int count, int recent, int columns, int lastFav, int dx, int dy) {
        return HomeNavigation.INSTANCE.move(from, count, recent, columns, lastFav, dx, dy, false);
    }

    static List<String> replace(List<String> current, int slot, String app) {
        if (slot < 0 || slot >= current.size() || current.size() > MAX || app == null || app.isEmpty()
            || current.contains(app)) throw new IllegalArgumentException("Invalid favourite replacement");
        List<String> next = new ArrayList<>(current);
        next.set(slot, app);
        return next;
    }
}
